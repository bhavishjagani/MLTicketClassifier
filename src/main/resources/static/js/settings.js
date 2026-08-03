document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('password-form');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const current = document.getElementById('current-password').value;
            const newPw = document.getElementById('new-password').value;
            const confirm = document.getElementById('confirm-password').value;
            if (newPw !== confirm) { Swift.toast('Passwords do not match', 'error'); return; }
            if (newPw.length < 8) { Swift.toast('Password must be at least 8 characters', 'error'); return; }
            try {
                await Swift.api('/api/change-password', { method: 'POST', body: JSON.stringify({ currentPassword: current, newPassword: newPw }) });
                Swift.toast('Password changed', 'success');
                form.reset();
            } catch (err) { Swift.toast(err.message, 'error'); }
        });
    }
    const toggle = document.getElementById('notif-toggle');
    if (toggle) {
        const saved = localStorage.getItem('notifications') === 'true';
        toggle.checked = saved;
        toggle.addEventListener('change', () => {
            localStorage.setItem('notifications', toggle.checked);
            Swift.toast('Preference saved', 'success');
        });
    }
});