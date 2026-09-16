'use strict';

/**
 * api.js — single fetch helper for the entire app.
 *
 * Usage (every feature page):
 *   const data  = await API.get('/api/products');
 *   const saved = await API.post('/api/orders', { ... });
 *
 * On 401 the helper clears the token and calls window.__showView('auth').
 * On any other non-2xx it throws an Error whose .message is already human-readable:
 *   - ErrorResponse          → err.message = response.message field
 *   - ValidationErrorResponse → err.message = "field: msg; field: msg" (one per line)
 * The raw response body is on err.data if you need it.
 */
const API = (() => {
  function _getToken() {
    return localStorage.getItem('jwtToken');
  }

  function _handleUnauthorized() {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('username');
    if (typeof window.__showView === 'function') window.__showView('auth');
  }

  function _extractMessage(data, status) {
    if (!data) return `Request failed (${status})`;

    // ValidationErrorResponse: { errors: { fieldName: "message", ... } }
    if (data.errors && typeof data.errors === 'object') {
      return Object.entries(data.errors).map(([f, m]) => `${f}: ${m}`).join('\n');
    }

    // ErrorResponse: { status, error, message, path, timestamp }
    if (data.message) return data.message;

    return `Request failed (${status})`;
  }

  async function _request(method, path, body) {
    const headers = { 'Content-Type': 'application/json' };
    const token = _getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const opts = { method, headers };
    if (body !== undefined) opts.body = JSON.stringify(body);

    let res;
    try {
      res = await fetch(path, opts);
    } catch {
      // fetch() throws a TypeError on network failure (offline, DNS, refused connection)
      const err = new Error('Network error. Please check your connection.');
      err.status = 0;
      throw err;
    }

    if (res.status === 401) {
      _handleUnauthorized();
      const err = new Error('Session expired. Please log in again.');
      err.status = 401;
      throw err;
    }

    if (res.status === 204) return null;

    const text = await res.text();
    let data = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        // Non-JSON body (e.g., HTML error page from the servlet container).
        // Fall through with data = null so _extractMessage returns a generic message.
      }
    }

    if (!res.ok) {
      const err = new Error(_extractMessage(data, res.status));
      err.status = res.status;
      err.data = data;
      throw err;
    }

    return data;
  }

  return {
    get:    (path)        => _request('GET',    path),
    post:   (path, body)  => _request('POST',   path, body),
    put:    (path, body)  => _request('PUT',    path, body),
    patch:  (path, body)  => _request('PATCH',  path, body),
    delete: (path)        => _request('DELETE', path),
  };
})();
