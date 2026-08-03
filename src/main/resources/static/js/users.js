async function loadUsers() {
    const body = document.getElementById('users-table-body');
    if (!body) return;
    body.innerHTML = Swift.skeletonRows(5, 5);
    try {
        const users = await Swift.api('/api/users');
        if (!users?.length) {
            body.innerHTML = `<tr><td colspan="5"><div class="empty-state"><h3>No users yet</h3></div></td></tr>`;
            return;
        }
        body.innerHTML = users.map(u => `
      <tr>
        <td style="padding:12px 14px">
          <div style="display:flex;align-items:center;gap:10px">
            <div class="avatar">${Swift.initials(u.name)}</div>
            <span style="font-weight:500">${Swift.esc(u.name)}</span>
          </div>
        </td>
        <td style="padding:12px 14px;color:var(--text-dim)">${Swift.esc(u.email)}</td>
        <td style="padding:12px 14px"><span class="${Swift.badgeCls(u.role)}">${Swift.humanLabel(u.role)}</span></td>
        <td style="padding:12px 14px"><span class="badge ${u.active ? 'badge-resolved' : 'badge-closed'}">${u.active ? 'Active' : 'Inactive'}</span></td>
        <td style="padding:12px 14px">
          <button class="btn btn-secondary btn-sm" onclick="toggleUser(${u.id}, this)">${u.active ? 'Deactivate' : 'Activate'}</button>
        </td>
      </tr>`).join('');
    } catch (e) {
        body.innerHTML = `<tr><td colspan="5" style="padding:16px;color:var(--coral);font-size:13px">${Swift.esc(e.message)}</td></tr>`;
    }
}

async function toggleUser(id, btn) {
    btn.disabled = true;
    try {
        await Swift.api(`/api/users/${id}/toggle`, { method: 'PATCH' });
        Swift.toast('User updated', 'success');
        await loadUsers();
    } catch (e) {
        Swift.toast(e.message, 'error');
        btn.disabled = false;
    }
}

document.addEventListener('DOMContentLoaded', loadUsers);