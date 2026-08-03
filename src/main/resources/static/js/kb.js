async function searchKB() {
    const q = document.getElementById('kb-search')?.value.trim();
    const results = document.getElementById('kb-results');
    if (!results) return;
    if (!q) { results.innerHTML = `<div class="empty-state"><p>Enter a search term above</p></div>`; return; }
    results.innerHTML = [1,2,3].map(() => `<div class="card" style="margin-bottom:12px"><div class="skeleton" style="height:14px;width:40%;margin-bottom:8px;border-radius:4px"></div><div class="skeleton" style="height:11px;width:100%;border-radius:4px"></div></div>`).join('');
    try {
        const items = await Swift.api(`/api/kb/search?q=${encodeURIComponent(q)}`);
        if (!items?.length) {
            results.innerHTML = `<div class="empty-state"><svg class="empty-state-icon" viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="21" cy="21" r="13"/><path d="M36 36l6 6" stroke-linecap="round"/></svg><h3>No results</h3><p>Try a different search term</p></div>`;
            return;
        }
        results.innerHTML = items.map(r => `
      <div class="card" style="margin-bottom:12px">
        <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:12px;margin-bottom:8px">
          <h3 style="font-size:14px;font-weight:600">${Swift.esc(r.title)}</h3>
          ${r.documentRef ? `<span style="font-size:11.5px;color:var(--text-faint);white-space:nowrap;font-family:'IBM Plex Mono',monospace">${Swift.esc(r.documentRef)}</span>` : ''}
        </div>
        <p style="font-size:13.5px;color:var(--text-dim);line-height:1.6">${Swift.esc(r.snippet || '')}</p>
      </div>`).join('');
    } catch (e) {
        results.innerHTML = `<div style="font-size:13px;color:var(--coral)">${Swift.esc(e.message)}</div>`;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('kb-search')?.addEventListener('keydown', e => { if (e.key === 'Enter') searchKB(); });
});