document.addEventListener('DOMContentLoaded', async () => {
    const statsEl = document.getElementById('stats-container');
    const recentEl = document.getElementById('recent-tickets');

    if (statsEl) {
        statsEl.innerHTML = [1,2,3].map(() => `
            <div class="stat-card">
                <div class="skeleton" style="height:11px;width:80px;border-radius:4px;margin-bottom:14px"></div>
                <div class="skeleton" style="height:32px;width:60px;border-radius:6px"></div>
            </div>`).join('');
        try {
            const s = await Swift.api('/api/dashboard/stats');
            statsEl.innerHTML = [
                { label: 'Total tickets', value: s.totalTickets, color: 'var(--teal)' },
                { label: 'Open tickets',  value: s.openTickets,  color: 'var(--blue)' },
                { label: 'Critical',      value: s.criticalTickets, color: 'var(--coral)' },
            ].map(c => `
                <div class="stat-card">
                    <div class="stat-label">${c.label}</div>
                    <div class="stat-value" style="color:${c.color}">${c.value ?? 0}</div>
                </div>`).join('');
        } catch (e) {
            statsEl.innerHTML = `<div class="card" style="color:var(--coral);font-size:13px">${Swift.esc(e.message)}</div>`;
        }
    }

    if (recentEl) {
        try {
            const tickets = await Swift.api('/api/dashboard/recent?limit=6');
            if (!tickets?.length) {
                recentEl.innerHTML = `<div class="empty-state"><p>No tickets yet</p></div>`;
            } else {
                recentEl.innerHTML = tickets.map(t => `
                    <a href="/tickets/${t.id}" class="${Swift.priorityCls(t.priority)}" 
                       style="display:flex;align-items:center;justify-content:space-between;padding:10px 12px;border-radius:8px;transition:background .1s;text-decoration:none;color:inherit" 
                       onmouseover="this.style.background='#f9fafb'" onmouseout="this.style.background=''">
                        <div style="min-width:0;padding-left:8px">
                            <div style="font-size:13.5px;font-weight:500;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${Swift.esc(t.subject || '(no subject)')}</div>
                            <div style="font-size:12px;color:var(--text-faint);margin-top:2px;font-family:'IBM Plex Mono',monospace">#${t.id} &middot; ${Swift.timeAgo(t.createdAt)}</div>
                        </div>
                        <span class="${Swift.badgeCls(t.status)}" style="flex-shrink:0;margin-left:12px">${Swift.humanLabel(t.status)}</span>
                    </a>`).join('');
            }
        } catch (e) {
            recentEl.innerHTML = `<div style="font-size:13px;color:var(--coral)">${Swift.esc(e.message)}</div>`;
        }
    }

    const ctx = document.getElementById('sentimentChart')?.getContext('2d');
    if (ctx) {
        try {
            const data = await Swift.api('/api/analytics/sentiment-trend');
            if (data && data.length > 0) {
                new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: data.map(d => d.week),
                        datasets: [
                            { label: 'Positive', data: data.map(d => d.POSITIVE || 0), borderColor: '#059669', tension: 0.2 },
                            { label: 'Negative', data: data.map(d => d.NEGATIVE || 0), borderColor: '#e5483a', tension: 0.2 },
                            { label: 'Neutral', data: data.map(d => d.NEUTRAL || 0), borderColor: '#6b7280', tension: 0.2 }
                        ]
                    },
                    options: { responsive: true, plugins: { legend: { position: 'top' } } }
                });
            } else {
                document.getElementById('sentimentChart').parentElement.innerHTML = '<div class="text-gray-400">No sentiment data</div>';
            }
        } catch (_) {
        }
    }
});