document.addEventListener('DOMContentLoaded', async () => {
    const form = document.getElementById('profile-form');
    if (!form) return;

    try {
        const user = await Swift.api('/api/users/me');
        document.getElementById('profile-name').value = user.name || '';
        document.getElementById('profile-email').value = user.email || '';
        if (user.profilePicture) {
            document.getElementById('profile-picture-preview').src = user.profilePicture;
        }
    } catch (_) {}

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('profile-name').value.trim();
        if (!name) { Swift.toast('Name is required', 'error'); return; }
        try {
            await Swift.api('/api/profile', { method: 'POST', body: JSON.stringify({ name }) });
            Swift.toast('Profile updated', 'success');
            const user = await Swift.api('/api/users/me');
            document.getElementById('avatar-initials').textContent = Swift.initials(user.name);
        } catch (err) {
            Swift.toast(err.message, 'error');
        }
    });

    document.getElementById('upload-picture-btn').addEventListener('click', async () => {
        const input = document.getElementById('profile-picture-input');
        const file = input.files[0];
        if (!file) { Swift.toast('Select an image first', 'error'); return; }
        const formData = new FormData();
        formData.append('file', file);
        try {
            const res = await fetch('/api/profile/picture', { method: 'POST', body: formData });
            if (!res.ok) throw new Error('Upload failed');
            Swift.toast('Picture uploaded', 'success');
            const user = await Swift.api('/api/users/me');
            document.getElementById('profile-picture-preview').src = user.profilePicture;
        } catch (err) {
            Swift.toast(err.message, 'error');
        }
    });
});