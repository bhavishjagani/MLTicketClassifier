let currentPage = 0;
const PAGE_SIZE = 10;

async function loadTickets(page) {
    if (page !== undefined) currentPage = page;
    const body = document.getElementById('ticket-table-body');
    if (!body) return;
    body.innerHTML = Swift.skeletonRows(7, 7);

    const p = new URLSearchParams({ page: currentPage, size: PAGE_SIZE });
    const search = document.getElementById('search-input')?.value.trim();
    const status = document.getElementById('status-filter')?.value;
    const priority = document.getElementById('priority-filter')?.value;
    if (search) p.set('search', search);
    if (status) p.set('status', status);
    if (priority) p.set('priority', priority);

    try {
        const result = await Swift.api(`/tickets/api/tickets?${p}`);
        const items = result.content ?? result;
        if (!items?.length) {
            body.innerHTML = `<tr><td colspan="7"><div class="empty-state">
        <h3>No tickets found</h3><p>Try adjusting your search or filters</p>
      </div></td></tr>`;
            renderPagination(null);
            return;
        }
        body.innerHTML = items.map(t => `
      <tr class="${Swift.priorityCls(t.priority)}" onclick="window.location='/tickets/${t.id}'" style="cursor:pointer">
        <td style="padding:12px 14px;font-family:'IBM Plex Mono',monospace;color:var(--text-faint);font-size:12px">#${t.id}</td>
        <td style="padding:12px 14px;max-width:240px">
          <div style="font-weight:500;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${Swift.esc(t.subject || '(no subject)')}</div>
          ${t.product ? `<div style="font-size:11.5px;color:var(--text-faint);margin-top:2px">${Swift.esc(t.product)}</div>` : ''}
        </td>
        <td style="padding:12px 14px"><span class="${Swift.badgeCls(t.status)}">${Swift.humanLabel(t.status)}</span></td>
        <td style="padding:12px 14px"><span class="${Swift.badgeCls(t.priority)}">${Swift.humanLabel(t.priority)}</span></td>
        <td style="padding:12px 14px"><span class="${Swift.badgeCls(t.sentiment)}">${Swift.humanLabel(t.sentiment) || '—'}</span></td>
        <td style="padding:12px 14px;color:var(--text-dim);font-size:13px">${Swift.esc(t.assignedAgent) || '<span style="color:var(--text-faint)">Unassigned</span>'}</td>
        <td style="padding:12px 14px;color:var(--text-faint);font-size:12.5px;white-space:nowrap">${Swift.timeAgo(t.createdAt)}</td>
      </tr>`).join('');
        renderPagination(result);
    } catch (e) {
        body.innerHTML = `<tr><td colspan="7" style="padding:16px;color:var(--coral);font-size:13px">${Swift.esc(e.message)}</td></tr>`;
    }
}

function renderPagination(page) {
    const el = document.getElementById('pagination');
    if (!el) return;
    if (!page || page.totalPages == null) { el.innerHTML = ''; return; }
    const { number: n, totalPages, totalElements } = page;
    el.innerHTML = `
    <span style="font-size:13px;color:var(--text-faint)">${totalElements ?? 0} tickets</span>
    <div style="display:flex;align-items:center;gap:8px">
      <span style="font-size:13px;color:var(--text-dim)">Page ${n + 1} of ${Math.max(totalPages,1)}</span>
      <button class="btn btn-secondary btn-sm" ${n<=0?'disabled':''} onclick="loadTickets(${n-1})">Previous</button>
      <button class="btn btn-secondary btn-sm" ${n+1>=totalPages?'disabled':''} onclick="loadTickets(${n+1})">Next</button>
    </div>`;
}

function wireNewTicketForm() {
    const form = document.getElementById('new-ticket-form');
    if (!form) return;
    form.addEventListener('submit', async e => {
        e.preventDefault();
        const btn = form.querySelector('button[type="submit"]');
        btn.disabled = true;
        btn.textContent = 'Submitting…';
        const data = Object.fromEntries(new FormData(form));
        Object.keys(data).forEach(k => { if (!data[k]) delete data[k]; });
        try {
            const t = await Swift.api('/tickets/api/tickets', { method: 'POST', body: JSON.stringify(data) });
            Swift.toast('Ticket created', 'success');
            window.location.href = `/tickets/${t.id}`;
        } catch (err) {
            Swift.toast(err.message, 'error');
            btn.disabled = false;
            btn.textContent = 'Submit Ticket';
        }
    });
}

function ticketId() {
    const m = window.location.pathname.match(/\/tickets\/(\d+)/);
    return m ? m[1] : null;
}

async function loadTicketDetails() {
    const idEl = document.getElementById('ticket-id');
    if (!idEl) return;
    const id = ticketId();
    if (!id) return;

    try {
        const t = await Swift.api(`/tickets/api/tickets/${id}`);
        idEl.textContent = t.id;
        document.getElementById('ticket-subject').textContent = t.subject || '(no subject)';
        document.getElementById('ticket-message').textContent = t.message || '';
        document.getElementById('ticket-category').textContent = Swift.humanLabel(t.category);
        document.getElementById('ticket-source').textContent = Swift.humanLabel(t.source);
        document.getElementById('ticket-product').textContent = t.product || '—';
        document.getElementById('ticket-sentiment').textContent = Swift.humanLabel(t.sentiment);
        document.getElementById('ticket-urgency').textContent = Swift.humanLabel(t.urgency);
        document.getElementById('ticket-confidence').textContent = t.confidence != null ? `${Math.round(t.confidence * 100)}%` : '—';
        document.getElementById('suggested-response').textContent = t.suggestedResponse || 'No suggestion generated yet.';
        document.getElementById('ticket-summary').textContent = t.summary || 'No summary available.';
        document.getElementById('ticket-customer').textContent = t.customerReference || '—';
        document.getElementById('ticket-created').textContent = t.createdAt ? new Date(t.createdAt).toLocaleString() : '—';

        const statusSel = document.getElementById('status-select');
        const prioritySel = document.getElementById('priority-select');
        if (statusSel && t.status) statusSel.value = t.status;
        if (prioritySel && t.priority) prioritySel.value = t.priority;

        await loadAgents(t.assignedAgentId);
        await loadComments(id);
    } catch (e) {
        Swift.toast(e.message, 'error');
    }
}

async function loadAgents(selectedId) {
    const sel = document.getElementById('agent-select');
    if (!sel) return;
    try {
        const agents = await Swift.api('/api/users/agents');
        sel.innerHTML = `<option value="">— Unassigned —</option>` +
            agents.map(a => `<option value="${a.id}" ${a.id === selectedId ? 'selected' : ''}>${Swift.esc(a.name)}</option>`).join('');
    } catch (_) {
        sel.innerHTML = `<option value="">Could not load agents</option>`;
    }
}

async function loadComments(id) {
    const list = document.getElementById('comments-list');
    if (!list) return;
    try {
        const comments = await Swift.api(`/tickets/api/tickets/${id}/comments`);
        if (!comments?.length) {
            list.innerHTML = `<p style="font-size:13px;color:var(--text-faint)">No internal notes yet.</p>`;
            return;
        }
        list.innerHTML = comments.map(c => `
      <div style="border-left:2px solid var(--line);padding:8px 12px;border-radius:0 6px 6px 0">
        <p style="font-size:13.5px">${Swift.esc(c.text)}</p>
        <p style="font-size:11.5px;color:var(--text-faint);margin-top:4px">${Swift.timeAgo(c.createdAt)}</p>
      </div>`).join('');
    } catch (_) {
        list.innerHTML = `<p style="font-size:13px;color:var(--coral)">Could not load notes.</p>`;
    }
}

async function updateStatus() {
    const id = ticketId();
    const status = document.getElementById('status-select').value;
    try {
        await Swift.api(`/tickets/api/tickets/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) });
        Swift.toast('Status updated', 'success');
    } catch (e) { Swift.toast(e.message, 'error'); }
}

async function updatePriority() {
    const id = ticketId();
    const priority = document.getElementById('priority-select').value;
    try {
        await Swift.api(`/tickets/api/tickets/${id}/priority`, { method: 'PATCH', body: JSON.stringify({ priority }) });
        Swift.toast('Priority updated', 'success');
    } catch (e) { Swift.toast(e.message, 'error'); }
}

async function assignAgent() {
    const id = ticketId();
    const agentId = document.getElementById('agent-select').value || null;
    try {
        await Swift.api(`/tickets/api/tickets/${id}/assign`, { method: 'PATCH', body: JSON.stringify({ agentId }) });
        Swift.toast('Assigned', 'success');
    } catch (e) { Swift.toast(e.message, 'error'); }
}

async function approveResponse() {
    const id = ticketId();
    const response = document.getElementById('suggested-response').textContent;
    if (!response || response === 'No suggestion generated yet.') return;
    try {
        await Swift.api(`/tickets/api/tickets/${id}/response`, { method: 'POST', body: JSON.stringify({ response }) });
        Swift.toast('Response sent', 'success');
    } catch (e) { Swift.toast(e.message, 'error'); }
}

async function addComment() {
    const id = ticketId();
    const input = document.getElementById('comment-input');
    const text = input?.value.trim();
    if (!text) return;
    try {
        await Swift.api(`/tickets/api/tickets/${id}/comments`, { method: 'POST', body: JSON.stringify({ text }) });
        input.value = '';
        await loadComments(id);
        Swift.toast('Note added', 'success');
    } catch (e) { Swift.toast(e.message, 'error'); }
}

document.addEventListener('DOMContentLoaded', () => {
    const searchParam = new URLSearchParams(window.location.search).get('search');
    const searchInput = document.getElementById('search-input');
    if (searchInput && searchParam) searchInput.value = searchParam;
    searchInput?.addEventListener('keydown', e => { if (e.key === 'Enter') loadTickets(0); });

    if (document.getElementById('ticket-table-body')) {
        loadTickets(0);
    }
    if (document.getElementById('new-ticket-form')) {
        wireNewTicketForm();
    }
    if (document.getElementById('ticket-id')) {
        loadTicketDetails();
    }
});