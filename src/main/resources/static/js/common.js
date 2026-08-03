const Swift = (() => {
    async function api(path, options = {}) {
        const res = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options });
        if (!res.ok) {
            let msg = `Error ${res.status}`;
            try { const b = await res.json(); if (b?.message) msg = b.message; } catch (_) {}
            throw new Error(msg);
        }
        if (res.status === 204) return null;
        const text = await res.text();
        return text ? JSON.parse(text) : null;
    }

    function toast(message, type = 'default') {
        const stack = document.getElementById('toast-stack');
        if (!stack) return;
        const icons = {
            success: 'M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z',
            error: 'M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z',
        };
        const icon = icons[type] || icons.success;
        const el = document.createElement('div');
        el.className = `toast${type !== 'default' ? ' ' + type : ''}`;
        el.innerHTML = `<svg viewBox="0 0 20 20" fill="currentColor" style="width:15px;height:15px;flex-shrink:0"><path fill-rule="evenodd" d="${icon}" clip-rule="evenodd"/></svg><span>${esc(message)}</span>`;
        stack.appendChild(el);
        setTimeout(() => {
            el.style.transition = 'opacity .18s ease';
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 200);
        }, 3200);
    }

    function esc(s) {
        if (s == null) return '';
        return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }

    function badgeCls(value) {
        return `badge badge-${(value || 'low').toString().toLowerCase()}`;
    }

    function priorityCls(priority) {
        return `sort-row p-${(priority || 'low').toString().toLowerCase()}`;
    }

    function humanLabel(value) {
        if (!value) return '—';
        return value.toString().toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
    }

    function timeAgo(iso) {
        if (!iso) return '—';
        const diff = Date.now() - new Date(iso).getTime();
        const m = Math.floor(diff / 60000);
        if (m < 1) return 'just now';
        if (m < 60) return `${m}m ago`;
        const h = Math.floor(m / 60);
        if (h < 24) return `${h}h ago`;
        const d = Math.floor(h / 24);
        if (d < 30) return `${d}d ago`;
        return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
    }

    function initials(name) {
        if (!name) return '?';
        return name.trim().split(/\s+/).map(w => w[0] || '').join('').slice(0, 2).toUpperCase();
    }

    function skeletonRows(cols, rows = 5) {
        return Array.from({ length: rows }, () =>
            `<tr>${Array.from({ length: cols }, () =>
                `<td style="padding:12px 14px"><div class="skeleton" style="height:13px;border-radius:4px"></div></td>`
            ).join('')}</tr>`
        ).join('');
    }

    function toggleDropdown() {
        document.getElementById('nav-dropdown').classList.toggle('hidden');
    }

    return { api, toast, badgeCls, priorityCls, humanLabel, timeAgo, initials, esc, skeletonRows, toggleDropdown };
})();