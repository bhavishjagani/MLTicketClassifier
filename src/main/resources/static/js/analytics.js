document.addEventListener('DOMContentLoaded', async () => {
    const el = document.getElementById('analytics-cards');
    if (!el) return;
    el.innerHTML = [1,2,3,4].map(() => `<div class="stat-card"><div class="skeleton" style="height:11px;width:80px;border-radius:4px;margin-bottom:14px"></div><div class="skeleton" style="height:30px;width:70px;border-radius:6px"></div></div>`).join('');
    try {
        const o = await Swift.api('/api/analytics/overview');
        el.innerHTML = [
            { label: 'Avg resolution', value: `${(o.avgResolutionHours||0).toFixed(1)}h`, color: 'var(--teal)' },
            { label: 'Reopen rate',    value: `${((o.reopenRate||0)*100).toFixed(1)}%`,  color: 'var(--blue)' },
            { label: 'Negative sentiment', value: `${((o.negativePercent||0)*100).toFixed(1)}%`, color: 'var(--coral)' },
            { label: 'SLA breaches',   value: o.slaBreaches ?? 0, color: 'var(--amber)' },
        ].map(c => `<div class="stat-card"><div class="stat-label">${c.label}</div><div class="stat-value" style="color:${c.color}">${c.value}</div></div>`).join('');
    } catch (e) {
        el.innerHTML = `<div class="card" style="color:var(--coral);font-size:13px">${Swift.esc(e.message)}</div>`;
    }

    loadCategoryChart();
    loadPriorityChart();
    loadSentimentChart();
    loadTicketTrendChart();
});

async function loadCategoryChart() {
    const ctx = document.getElementById('categoryChart')?.getContext('2d');
    if (!ctx) return;
    try {
        const data = await Swift.api('/api/analytics/category-distribution');
        const labels = Object.keys(data);
        const values = Object.values(data);
        if (labels.length === 0) { document.getElementById('categoryChart').parentElement.innerHTML = '<p class="text-gray-400">No data</p>'; return; }
        new Chart(ctx, {
            type: 'pie',
            data: { labels, datasets: [{ data: values, backgroundColor: ['#3b6fd4','#0f9b8e','#d97706','#e5483a','#7c3aed','#059669','#6b7280'] }] },
            options: { responsive: true, maintainAspectRatio: true, plugins: { legend: { position: 'right' } } }
        });
    } catch (_) {}
}

async function loadPriorityChart() {
    const ctx = document.getElementById('priorityChart')?.getContext('2d');
    if (!ctx) return;
    try {
        const data = await Swift.api('/api/analytics/priority-distribution');
        const labels = Object.keys(data);
        const values = Object.values(data);
        if (labels.length === 0) { document.getElementById('priorityChart').parentElement.innerHTML = '<p class="text-gray-400">No data</p>'; return; }
        new Chart(ctx, {
            type: 'doughnut',
            data: { labels, datasets: [{ data: values, backgroundColor: ['#6b7280','#3b6fd4','#d97706','#e5483a'] }] },
            options: { responsive: true, maintainAspectRatio: true }
        });
    } catch (_) {}
}

async function loadSentimentChart() {
    const ctx = document.getElementById('sentimentTrendChart')?.getContext('2d');
    if (!ctx) return;
    try {
        const data = await Swift.api('/api/analytics/sentiment-trend');
        if (!data || data.length === 0) { document.getElementById('sentimentTrendChart').parentElement.innerHTML = '<p class="text-gray-400">No data</p>'; return; }
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
            options: { responsive: true, maintainAspectRatio: true }
        });
    } catch (_) {}
}

async function loadTicketTrendChart() {
    const ctx = document.getElementById('ticketTrendChart')?.getContext('2d');
    if (!ctx) return;
    try {
        const data = await Swift.api('/api/analytics/ticket-trend');
        if (!data || data.length === 0) { document.getElementById('ticketTrendChart').parentElement.innerHTML = '<p class="text-gray-400">No data</p>'; return; }
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.map(d => d.date),
                datasets: [{ label: 'Tickets', data: data.map(d => d.count), backgroundColor: '#3b6fd4' }]
            },
            options: { responsive: true, maintainAspectRatio: true }
        });
    } catch (_) {}
}