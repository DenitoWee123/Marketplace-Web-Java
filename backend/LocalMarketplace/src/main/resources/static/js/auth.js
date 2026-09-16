'use strict';

/**
 * auth.js — wires all four /auth endpoints.
 *
 * POST /auth/register     → CreateUserDTO  → 201 UserDetailsDTO (no token; go to login)
 * POST /auth/login        → LoginRequestDTO → 200 AuthTokenDTO   (store JWT, go to products)
 * POST /auth/logout       → (stateless: clear token; also calls the endpoint)
 * POST /auth/forgot-password → ForgotPasswordRequestDTO → 200 { message }
 */
const Auth = (() => {

  function _setMsg(id, msg, type = 'error') {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = msg;
    el.className = msg ? (type === 'success' ? 'form-success' : 'form-error') : '';
  }

  function _clear(...ids) { ids.forEach(id => _setMsg(id, '')); }

  function _render() {
    document.getElementById('view-auth').innerHTML = `
<div class="auth-container">
  <h1 class="brand">LocalMarketplace</h1>

  <div class="auth-tabs" role="tablist">
    <button class="tab-btn active" data-tab="login">Sign In</button>
    <button class="tab-btn" data-tab="register">Register</button>
    <button class="tab-btn" data-tab="forgot">Forgot Password</button>
  </div>

  <!-- ── Login ── -->
  <div id="tab-login" class="tab-panel">
    <form id="form-login" novalidate>
      <div class="form-group">
        <label for="login-username">Username</label>
        <input id="login-username" type="text" name="username"
               autocomplete="username" required>
      </div>
      <div class="form-group">
        <label for="login-password">Password</label>
        <input id="login-password" type="password" name="password"
               autocomplete="current-password" required>
      </div>
      <p class="form-error" id="login-error"></p>
      <button type="submit" class="btn btn-primary btn-full">Sign In</button>
    </form>
  </div>

  <!-- ── Register ── -->
  <div id="tab-register" class="tab-panel" hidden>
    <form id="form-register" novalidate>
      <div class="form-row">
        <div class="form-group">
          <label for="reg-first">First Name</label>
          <input id="reg-first" type="text" name="firstName" maxlength="50" required>
        </div>
        <div class="form-group">
          <label for="reg-last">Last Name</label>
          <input id="reg-last" type="text" name="lastName" maxlength="50" required>
        </div>
      </div>
      <div class="form-group">
        <label for="reg-username">Username</label>
        <input id="reg-username" type="text" name="username"
               maxlength="50" autocomplete="username" required>
      </div>
      <div class="form-group">
        <label for="reg-email">Email</label>
        <input id="reg-email" type="email" name="email"
               maxlength="255" autocomplete="email" required>
      </div>
      <div class="form-group">
        <label for="reg-phone">Phone</label>
        <input id="reg-phone" type="tel" name="phone"
               placeholder="+359123456789" required>
      </div>
      <div class="form-group">
        <label for="reg-password">Password</label>
        <input id="reg-password" type="password" name="password"
               minlength="6" maxlength="50" autocomplete="new-password" required>
      </div>
      <p class="form-error"   id="register-error"></p>
      <p class="form-success" id="register-success"></p>
      <button type="submit" class="btn btn-primary btn-full">Create Account</button>
    </form>
  </div>

  <!-- ── Forgot password ── -->
  <div id="tab-forgot" class="tab-panel" hidden>
    <form id="form-forgot" novalidate>
      <div class="form-group">
        <label for="forgot-email">Email address</label>
        <input id="forgot-email" type="email" name="email"
               autocomplete="email" required>
      </div>
      <p class="form-error"   id="forgot-error"></p>
      <p class="form-success" id="forgot-success"></p>
      <button type="submit" class="btn btn-primary btn-full">Send Reset Link</button>
    </form>
  </div>
</div>`;

    document.querySelectorAll('#view-auth .tab-btn').forEach(btn => {
      btn.addEventListener('click', () => _switchTab(btn.dataset.tab));
    });

    document.getElementById('form-login').addEventListener('submit', _onLogin);
    document.getElementById('form-register').addEventListener('submit', _onRegister);
    document.getElementById('form-forgot').addEventListener('submit', _onForgot);
  }

  function _switchTab(name) {
    document.querySelectorAll('#view-auth .tab-btn').forEach(b =>
      b.classList.toggle('active', b.dataset.tab === name));
    document.querySelectorAll('#view-auth .tab-panel').forEach(p => p.hidden = true);
    document.getElementById('tab-' + name).hidden = false;
  }

  async function _onLogin(e) {
    e.preventDefault();
    const f = e.target;
    _clear('login-error');
    const btn = f.querySelector('[type=submit]');
    btn.disabled = true;
    try {
      // POST /auth/login — LoginRequestDTO { username, password }
      // Returns AuthTokenDTO { token, username, tokenType }
      const data = await API.post('/auth/login', {
        username: f.username.value.trim(),
        password: f.password.value,
      });
      localStorage.setItem('jwtToken', data.token);
      localStorage.setItem('username', data.username);
      window.__showView('products');
    } catch (err) {
      _setMsg('login-error', err.message);
    } finally {
      btn.disabled = false;
    }
  }

  async function _onRegister(e) {
    e.preventDefault();
    const f = e.target;
    _clear('register-error', 'register-success');
    const btn = f.querySelector('[type=submit]');
    btn.disabled = true;
    try {
      // POST /auth/register — CreateUserDTO { username, firstName, lastName, password, email, phone }
      // Returns 201 UserDetailsDTO — no token; redirect to login tab
      await API.post('/auth/register', {
        username:  f.username.value.trim(),
        firstName: f.firstName.value.trim(),
        lastName:  f.lastName.value.trim(),
        email:     f.email.value.trim(),
        phone:     f.phone.value.trim(),
        password:  f.password.value,
      });
      _setMsg('register-success', 'Account created! Please sign in.', 'success');
      f.reset();
      setTimeout(() => _switchTab('login'), 1500);
    } catch (err) {
      _setMsg('register-error', err.message);
    } finally {
      btn.disabled = false;
    }
  }

  async function _onForgot(e) {
    e.preventDefault();
    const f = e.target;
    _clear('forgot-error', 'forgot-success');
    const btn = f.querySelector('[type=submit]');
    btn.disabled = true;
    try {
      // POST /auth/forgot-password — ForgotPasswordRequestDTO { email }
      // Returns 200 { message: "If an account with that email exists, a reset link will be sent." }
      const data = await API.post('/auth/forgot-password', { email: f.email.value.trim() });
      _setMsg('forgot-success', data.message, 'success');
      f.reset();
    } catch (err) {
      _setMsg('forgot-error', err.message);
    } finally {
      btn.disabled = false;
    }
  }

  function _onLogout() {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('username');
    window.__showView('auth');
    API.post('/auth/logout').catch(() => {});
  }

  function init() {
    _render();
    document.getElementById('btn-logout').addEventListener('click', _onLogout);
  }

  return { init };
})();
