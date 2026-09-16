'use strict';

const Events = (() => {
  let _section;
  let _editingId = null; // null = create mode, number = edit mode
  let _isAdmin = false;

  function _esc(s) {
    return String(s ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  function _setError(msg) {
    const el = document.getElementById('events-error');
    if (el) el.textContent = msg || '';
  }

  function _setFormError(msg) {
    const el = document.getElementById('event-form-error');
    if (el) el.textContent = msg || '';
  }

  function _typeBadgeClass(type) {
    if (type === 'CRAFT_FAIRS') return 'badge-blue';
    if (type === 'PROMOTIONAL_CAMPAIGNS') return 'badge-yellow';
    return 'badge-green';
  }

  function _fmtDate(iso) {
    if (!iso) return '—';
    return String(iso).replace('T', ' ').substring(0, 16);
  }

  // "2024-01-15T10:30:00" → "2024-01-15T10:30" (datetime-local value format)
  function _toInputDt(iso) {
    if (!iso) return '';
    return String(iso).substring(0, 16);
  }

  function _renderSkeleton() {
    _section.innerHTML = `
<div class="section-header">
  <span class="section-title">Events</span>
  <button id="btn-new-event" class="btn btn-primary btn-sm">+ Create Event</button>
</div>

<div class="card" style="margin-bottom:20px">
  <div class="form-row" style="align-items:flex-end;gap:12px">
    <div class="form-group" style="margin-bottom:0">
      <label for="filter-type">Type</label>
      <select id="filter-type">
        <option value="">All types</option>
        <option value="CRAFT_FAIRS">Craft Fairs</option>
        <option value="PROMOTIONAL_CAMPAIGNS">Promotional Campaigns</option>
        <option value="STORYTELLING_FEATURES">Storytelling Features</option>
      </select>
    </div>
    <div class="form-group" style="margin-bottom:0">
      <label for="filter-active">Status</label>
      <select id="filter-active">
        <option value="">Any status</option>
        <option value="true">Active only</option>
        <option value="false">Inactive only</option>
      </select>
    </div>
    <button id="btn-filter" class="btn btn-secondary btn-sm" style="align-self:flex-end">Apply</button>
  </div>
</div>

<p class="form-error" id="events-error"></p>
<div id="events-list"></div>

<div id="event-form-panel" class="card" hidden style="margin-top:24px">
  <div class="section-header" style="margin-bottom:16px">
    <span class="card-title" id="event-form-title">Create Event</span>
    <button id="btn-cancel-form" class="btn btn-secondary btn-sm">Cancel</button>
  </div>

  <form id="event-form" novalidate>
    <div class="form-row">
      <div class="form-group">
        <label for="ef-title">Title *</label>
        <input id="ef-title" type="text" maxlength="255" required>
      </div>
      <div class="form-group">
        <label for="ef-type">Type *</label>
        <select id="ef-type" required>
          <option value="">— select type —</option>
          <option value="CRAFT_FAIRS">Craft Fair</option>
          <option value="PROMOTIONAL_CAMPAIGNS">Promotional Campaign</option>
          <option value="STORYTELLING_FEATURES">Storytelling Feature</option>
        </select>
      </div>
    </div>

    <div class="form-group">
      <label for="ef-description">Description</label>
      <textarea id="ef-description" maxlength="1000"></textarea>
    </div>

    <div class="form-group">
      <label style="display:flex;align-items:center;gap:8px;cursor:pointer">
        <input id="ef-active" type="checkbox">
        Active
      </label>
    </div>

    <!-- CRAFT_FAIRS fields -->
    <div id="ef-group-craft" hidden>
      <div class="form-group">
        <label for="ef-location">Location *</label>
        <input id="ef-location" type="text">
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="ef-start">Start Date *</label>
          <input id="ef-start" type="datetime-local">
        </div>
        <div class="form-group">
          <label for="ef-end">End Date *</label>
          <input id="ef-end" type="datetime-local">
        </div>
      </div>
    </div>

    <!-- PROMOTIONAL_CAMPAIGNS fields -->
    <div id="ef-group-promo" hidden>
      <div class="form-row">
        <div class="form-group">
          <label for="ef-discount-type">Discount Type *</label>
          <select id="ef-discount-type">
            <option value="">— select —</option>
            <option value="PERCENTAGE">Percentage</option>
            <option value="FIXED_AMOUNT">Fixed Amount</option>
            <option value="FREE_SHIPPING">Free Shipping</option>
          </select>
        </div>
        <div class="form-group">
          <label for="ef-discount-value">Discount Value *</label>
          <input id="ef-discount-value" type="number" min="0">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label for="ef-promo-start">Start Date *</label>
          <input id="ef-promo-start" type="datetime-local">
        </div>
        <div class="form-group">
          <label for="ef-promo-end">End Date *</label>
          <input id="ef-promo-end" type="datetime-local">
        </div>
      </div>
    </div>

    <!-- STORYTELLING_FEATURES fields -->
    <div id="ef-group-story" hidden>
      <div class="form-group">
        <label for="ef-content">Content *</label>
        <textarea id="ef-content" style="min-height:120px"></textarea>
      </div>
    </div>

    <p class="form-error" id="event-form-error"></p>
    <button type="submit" id="btn-submit-event" class="btn btn-primary">Save</button>
  </form>
</div>`;
  }

  function _onTypeChange() {
    const type = document.getElementById('ef-type').value;
    document.getElementById('ef-group-craft').hidden = (type !== 'CRAFT_FAIRS');
    document.getElementById('ef-group-promo').hidden = (type !== 'PROMOTIONAL_CAMPAIGNS');
    document.getElementById('ef-group-story').hidden = (type !== 'STORYTELLING_FEATURES');
  }

  async function _loadEvents() {
    _setError('');
    const me     = localStorage.getItem('username');
    const type   = document.getElementById('filter-type').value;
    const active = document.getElementById('filter-active').value;
    let url = '/api/events?size=100';
    if (type)   url += `&type=${encodeURIComponent(type)}`;
    if (active) url += `&active=${encodeURIComponent(active)}`;
    try {
      const [page, isAdmin] = await Promise.all([
        API.get(url),
        me ? API.get(`/api/users/${encodeURIComponent(me)}/is-admin`).catch(() => false) : Promise.resolve(false),
      ]);
      _isAdmin = !!isAdmin;
      _renderList(page.content || []);
    } catch (err) {
      _setError(err.message);
    }
  }

  function _renderList(events) {
    const el = document.getElementById('events-list');
    if (events.length === 0) {
      el.innerHTML = '<div class="empty-state">No events found.</div>';
      return;
    }
    const me = localStorage.getItem('username');
    el.innerHTML = events.map(ev => _eventCard(ev, me, _isAdmin)).join('');
    el.querySelectorAll('[data-event-edit]').forEach(btn =>
      btn.addEventListener('click', () => _openEdit(btn, parseInt(btn.dataset.eventEdit, 10)))
    );
    el.querySelectorAll('[data-event-delete]').forEach(btn =>
      btn.addEventListener('click', () => _onDelete(btn, parseInt(btn.dataset.eventDelete, 10)))
    );
  }

  function _eventCard(ev, me, isAdmin) {
    const isOwner = ev.ownerUsername === me || isAdmin;
    const activeBadge = ev.active
      ? '<span class="badge badge-green">Active</span>'
      : '<span class="badge badge-gray">Inactive</span>';
    const typeBadge = `<span class="badge ${_typeBadgeClass(ev.type)}">${_esc(ev.type.replace(/_/g, ' '))}</span>`;

    let extra = '';
    if (ev.type === 'CRAFT_FAIRS' && ev.location) {
      extra = `<div style="font-size:.83rem;color:var(--color-muted);margin-top:4px">&#128205; ${_esc(ev.location)} &nbsp;|&nbsp; ${_fmtDate(ev.startDate)} &#8594; ${_fmtDate(ev.endDate)}</div>`;
    } else if (ev.type === 'PROMOTIONAL_CAMPAIGNS') {
      const rawVal = ev.discountType === 'PERCENTAGE' ? `${ev.discountValue}%`
        : ev.discountType === 'FIXED_AMOUNT' ? `$${ev.discountValue}`
        : 'Free Shipping';
      extra = `<div style="font-size:.83rem;color:var(--color-muted);margin-top:4px">Discount: ${_esc(rawVal)} &nbsp;|&nbsp; ${_fmtDate(ev.startDate)} &#8594; ${_fmtDate(ev.endDate)}</div>`;
    } else if (ev.type === 'STORYTELLING_FEATURES' && ev.content) {
      const preview = ev.content.length > 120 ? ev.content.substring(0, 120) + '…' : ev.content;
      extra = `<div style="font-size:.83rem;color:var(--color-muted);margin-top:4px">${_esc(preview)}</div>`;
    }

    const actions = isOwner ? `
  <div style="display:flex;gap:8px;margin-top:12px">
    <button class="btn btn-secondary btn-sm" data-event-edit="${ev.id}">Edit</button>
    <button class="btn btn-danger btn-sm" data-event-delete="${ev.id}">Delete</button>
  </div>` : '';

    return `
<div class="card" style="margin-bottom:12px">
  <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:12px;flex-wrap:wrap">
    <div>
      <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin-bottom:4px">
        <strong>${_esc(ev.title)}</strong>
        ${typeBadge}
        ${activeBadge}
      </div>
      ${ev.description ? `<div style="font-size:.875rem;margin-bottom:2px">${_esc(ev.description)}</div>` : ''}
      ${extra}
      <div style="font-size:.78rem;color:var(--color-muted);margin-top:6px">by ${_esc(ev.ownerUsername)}</div>
    </div>
  </div>
  ${actions}
</div>`;
  }

  function _openCreate() {
    _editingId = null;
    document.getElementById('event-form-title').textContent = 'Create Event';
    document.getElementById('btn-submit-event').textContent = 'Create';
    document.getElementById('ef-type').disabled = false;
    _clearForm();
    _showForm();
  }

  async function _openEdit(btn, id) {
    btn.disabled = true;
    _setError('');
    try {
      const ev = await API.get(`/api/events/${id}`);
      _editingId = id;
      document.getElementById('event-form-title').textContent = 'Edit Event';
      document.getElementById('btn-submit-event').textContent = 'Save Changes';
      document.getElementById('ef-type').disabled = true;
      _populateForm(ev);
      _showForm();
    } catch (err) {
      _setError(err.message);
    } finally {
      btn.disabled = false;
    }
  }

  function _showForm() {
    _setFormError('');
    document.getElementById('event-form-panel').hidden = false;
    document.getElementById('event-form-panel').scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  function _hideForm() {
    document.getElementById('event-form-panel').hidden = true;
    document.getElementById('ef-type').disabled = false;
    _editingId = null;
  }

  function _clearForm() {
    document.getElementById('ef-title').value = '';
    document.getElementById('ef-type').value = '';
    document.getElementById('ef-description').value = '';
    document.getElementById('ef-active').checked = false;
    document.getElementById('ef-location').value = '';
    document.getElementById('ef-start').value = '';
    document.getElementById('ef-end').value = '';
    document.getElementById('ef-discount-type').value = '';
    document.getElementById('ef-discount-value').value = '';
    document.getElementById('ef-promo-start').value = '';
    document.getElementById('ef-promo-end').value = '';
    document.getElementById('ef-content').value = '';
    _onTypeChange();
  }

  function _populateForm(ev) {
    document.getElementById('ef-title').value = ev.title || '';
    document.getElementById('ef-type').value = ev.type || '';
    document.getElementById('ef-description').value = ev.description || '';
    document.getElementById('ef-active').checked = !!ev.active;

    if (ev.type === 'CRAFT_FAIRS') {
      document.getElementById('ef-location').value = ev.location || '';
      document.getElementById('ef-start').value = _toInputDt(ev.startDate);
      document.getElementById('ef-end').value   = _toInputDt(ev.endDate);
    } else if (ev.type === 'PROMOTIONAL_CAMPAIGNS') {
      document.getElementById('ef-discount-type').value  = ev.discountType || '';
      document.getElementById('ef-discount-value').value = ev.discountValue ?? '';
      document.getElementById('ef-promo-start').value = _toInputDt(ev.startDate);
      document.getElementById('ef-promo-end').value   = _toInputDt(ev.endDate);
    } else if (ev.type === 'STORYTELLING_FEATURES') {
      document.getElementById('ef-content').value = ev.content || '';
    }
    _onTypeChange();
  }

  async function _onSubmit(e) {
    e.preventDefault();
    const btn = document.getElementById('btn-submit-event');
    btn.disabled = true;
    _setFormError('');

    try {
      const type = document.getElementById('ef-type').value;
      if (!type) { _setFormError('Please select an event type.'); return; }

      const base = {
        title:       document.getElementById('ef-title').value.trim(),
        description: document.getElementById('ef-description').value.trim() || null,
        active:      document.getElementById('ef-active').checked,
      };

      if (type === 'CRAFT_FAIRS') {
        base.location  = document.getElementById('ef-location').value.trim() || null;
        base.startDate = document.getElementById('ef-start').value || null;
        base.endDate   = document.getElementById('ef-end').value   || null;
      } else if (type === 'PROMOTIONAL_CAMPAIGNS') {
        base.discountType  = document.getElementById('ef-discount-type').value || null;
        const dv = document.getElementById('ef-discount-value').value;
        base.discountValue = dv !== '' ? parseInt(dv, 10) : null;
        base.startDate = document.getElementById('ef-promo-start').value || null;
        base.endDate   = document.getElementById('ef-promo-end').value   || null;
      } else if (type === 'STORYTELLING_FEATURES') {
        base.content = document.getElementById('ef-content').value.trim() || null;
      }

      if (_editingId !== null) {
        await API.put(`/api/events/${_editingId}`, base);
      } else {
        await API.post('/api/events', { ...base, type });
      }

      _hideForm();
      await _loadEvents();
    } catch (err) {
      _setFormError(err.message);
    } finally {
      btn.disabled = false;
    }
  }

  async function _onDelete(btn, id) {
    if (!confirm('Delete this event?')) return;
    btn.disabled = true;
    _setError('');
    try {
      await API.delete(`/api/events/${id}`);
      await _loadEvents();
    } catch (err) {
      _setError(err.message);
      btn.disabled = false;
    }
  }

  function init() {
    _section = document.getElementById('view-events');
    _renderSkeleton();

    document.getElementById('btn-new-event').addEventListener('click', _openCreate);
    document.getElementById('btn-cancel-form').addEventListener('click', _hideForm);
    document.getElementById('btn-filter').addEventListener('click', _loadEvents);
    document.getElementById('ef-type').addEventListener('change', _onTypeChange);
    document.getElementById('event-form').addEventListener('submit', _onSubmit);

    const link = document.querySelector('[data-view="events"]');
    if (link) link.addEventListener('click', _loadEvents);
  }

  return { init };
})();
