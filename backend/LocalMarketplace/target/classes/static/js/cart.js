'use strict';

const Cart = (() => {
  let _section;

  function _fmt(cents) {
    return '$' + (cents / 100).toFixed(2);
  }

  function _setError(msg) {
    const el = document.getElementById('cart-error');
    if (el) el.textContent = msg || '';
  }

  function _setAddError(msg) {
    const el = document.getElementById('cart-add-error');
    if (el) el.textContent = msg || '';
  }

  function _esc(s) {
    return String(s ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  function _renderSkeleton() {
    _section.innerHTML = `
<div class="section-header">
  <span class="section-title">My Cart</span>
  <button id="btn-clear-cart" class="btn btn-danger btn-sm">Clear Cart</button>
</div>
<p class="form-error" id="cart-error"></p>
<div id="cart-body"></div>

<hr style="margin:28px 0;border:none;border-top:1px solid var(--color-border)">

<div class="card">
  <div class="card-title">Add Item</div>
  <div class="form-row" style="margin-bottom:12px">
    <div class="form-group" style="margin-bottom:0">
      <label for="picker-type">Filter by type</label>
      <select id="picker-type">
        <option value="">All types</option>
        <option>JEWELRY</option><option>CLOTHING</option><option>ACCESSORIES</option>
        <option>HOME_DECOR</option><option>ART</option><option>CERAMICS</option>
        <option>WOODWORK</option><option>CANDLES</option><option>COSMETICS</option>
        <option>TOYS</option>
      </select>
    </div>
    <div class="form-group" style="margin-bottom:0">
      <label for="picker-maker">Filter by maker</label>
      <input id="picker-maker" type="text" placeholder="maker username">
    </div>
  </div>
  <button id="btn-picker-search" class="btn btn-secondary btn-sm" style="margin-bottom:14px">Search Products</button>
  <div class="form-row">
    <div class="form-group" style="margin-bottom:0">
      <label for="picker-product">Product</label>
      <select id="picker-product"><option value="">— search first —</option></select>
    </div>
    <div class="form-group" style="margin-bottom:0">
      <label for="picker-qty">Quantity</label>
      <input id="picker-qty" type="number" min="1" value="1">
    </div>
  </div>
  <p class="form-error" id="cart-add-error"></p>
  <button id="btn-add-item" class="btn btn-primary" style="margin-top:10px">Add to Cart</button>
</div>`;
  }

  function _renderCart(cart) {
    const body = document.getElementById('cart-body');
    const clearBtn = document.getElementById('btn-clear-cart');

    if (!cart.items || cart.items.length === 0) {
      body.innerHTML = '<div class="empty-state">Your cart is empty.</div>';
      clearBtn.disabled = true;
      return;
    }

    clearBtn.disabled = false;

    const rows = cart.items.map(item => `
<tr>
  <td>${_esc(item.product.name)}</td>
  <td><span class="badge badge-gray">${_esc(item.product.productType)}</span></td>
  <td>${_fmt(item.product.price)}</td>
  <td>
    <div style="display:flex;align-items:center;gap:6px">
      <button class="btn btn-secondary btn-sm" data-action="dec" data-id="${item.id}" data-qty="${item.quantity}">−</button>
      <span>${item.quantity}</span>
      <button class="btn btn-secondary btn-sm" data-action="inc" data-id="${item.id}" data-qty="${item.quantity}">+</button>
    </div>
  </td>
  <td>${_fmt(item.subtotal)}</td>
  <td><button class="btn btn-danger btn-sm" data-action="remove" data-id="${item.id}">Remove</button></td>
</tr>`).join('');

    body.innerHTML = `
<div class="table-wrapper">
  <table>
    <thead><tr>
      <th>Product</th><th>Type</th><th>Price</th><th>Qty</th><th>Subtotal</th><th></th>
    </tr></thead>
    <tbody>${rows}</tbody>
  </table>
</div>
<div style="display:flex;justify-content:flex-end;align-items:center;gap:16px;margin-top:16px">
  <strong>Total: ${_fmt(cart.total)}</strong>
  <button id="btn-checkout" class="btn btn-primary">Checkout &#8594;</button>
</div>`;

    document.getElementById('btn-checkout').addEventListener('click', () => {
      window.__showView('checkout');
    });

    body.querySelectorAll('[data-action]').forEach(btn => {
      btn.addEventListener('click', () => _onTableAction(btn));
    });
  }

  async function _load() {
    _setError('');
    try {
      const cart = await API.get('/api/users/me/cart');
      _renderCart(cart);
    } catch (err) {
      _setError(err.message);
    }
  }

  async function _onTableAction(btn) {
    const id = btn.dataset.id;
    const action = btn.dataset.action;
    btn.disabled = true;
    _setError('');
    try {
      if (action === 'remove') {
        await API.delete(`/api/users/me/cart/items/${id}`);
        await _load();
      } else if (action === 'inc') {
        const newQty = parseInt(btn.dataset.qty, 10) + 1;
        const cart = await API.put(`/api/users/me/cart/items/${id}`, { quantity: newQty });
        _renderCart(cart);
      } else if (action === 'dec') {
        const cur = parseInt(btn.dataset.qty, 10);
        if (cur <= 1) { btn.disabled = false; return; }
        const cart = await API.put(`/api/users/me/cart/items/${id}`, { quantity: cur - 1 });
        _renderCart(cart);
      }
    } catch (err) {
      _setError(err.message);
      btn.disabled = false;
    }
  }

  async function _onClearCart() {
    const btn = document.getElementById('btn-clear-cart');
    btn.disabled = true;
    _setError('');
    try {
      await API.delete('/api/users/me/cart');
      await _load();
    } catch (err) {
      _setError(err.message);
      btn.disabled = false;
    }
  }

  async function _onPickerSearch() {
    const btn = document.getElementById('btn-picker-search');
    btn.disabled = true;
    _setAddError('');
    try {
      const type = document.getElementById('picker-type').value;
      const maker = document.getElementById('picker-maker').value.trim();
      let url = '/api/products?size=100';
      if (type) url += `&product_type=${encodeURIComponent(type)}`;
      if (maker) url += `&maker_username=${encodeURIComponent(maker)}`;
      const page = await API.get(url);
      const products = page.content || [];
      const sel = document.getElementById('picker-product');
      if (products.length === 0) {
        sel.innerHTML = '<option value="">No products found</option>';
      } else {
        sel.innerHTML = products.map(p =>
          `<option value="${p.id}">${_esc(p.name)} — ${_fmt(p.price)} (stock: ${p.quantity})</option>`
        ).join('');
      }
    } catch (err) {
      _setAddError(err.message);
    } finally {
      btn.disabled = false;
    }
  }

  async function _onAddItem() {
    const btn = document.getElementById('btn-add-item');
    btn.disabled = true;
    _setAddError('');
    try {
      const productId = parseInt(document.getElementById('picker-product').value, 10);
      const quantity = parseInt(document.getElementById('picker-qty').value, 10);
      if (!productId) { _setAddError('Select a product first.'); return; }
      if (!quantity || quantity < 1) { _setAddError('Quantity must be at least 1.'); return; }
      const cart = await API.post('/api/users/me/cart/items', { productId, quantity });
      _renderCart(cart);
      document.getElementById('picker-qty').value = '1';
    } catch (err) {
      _setAddError(err.message);
    } finally {
      btn.disabled = false;
    }
  }

  function init() {
    _section = document.getElementById('view-cart');
    _renderSkeleton();

    document.getElementById('btn-clear-cart').addEventListener('click', _onClearCart);
    document.getElementById('btn-picker-search').addEventListener('click', _onPickerSearch);
    document.getElementById('btn-add-item').addEventListener('click', _onAddItem);

    const link = document.querySelector('[data-view="cart"]');
    if (link) link.addEventListener('click', _load);
  }

  return { init };
})();
