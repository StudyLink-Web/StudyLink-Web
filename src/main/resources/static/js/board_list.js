async function fetchMyAuth() {
    try {
        const res = await fetch('/api/auth/me', {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });

        if (!res.ok) return { isLogin: false, isMentor: false, role: null };

        const data = await res.json();
        return {
            isLogin: !!data.isLogin,
            isMentor: !!data.isMentor,
            role: data.role || null
        };
    } catch (e) {
        return { isLogin: false, isMentor: false, role: null };
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    const registerBtn = document.querySelector('.register_campus_link');
    if (!registerBtn) return;

    registerBtn.style.display = 'none';

    const { isLogin, isMentor } = await fetchMyAuth();
    if (isLogin && isMentor) {
        registerBtn.style.display = '';
    }
});

async function goRegister() {
    const { isLogin, isMentor } = await fetchMyAuth();

    if (!isLogin) {
        alert('로그인이 필요합니다.');
        location.href = '/login';
        return;
    }

    if (!isMentor) {
        alert('해당 기능은 권한이 없습니다.');
        return;
    }

    location.href = '/board/register';
}
