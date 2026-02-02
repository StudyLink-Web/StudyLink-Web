document.addEventListener('DOMContentLoaded', () => {
    const registerBtn = document.querySelector('.register_campus_link');
    if (!registerBtn) return;

    // 비로그인 → 숨김
    if (!window.IS_LOGIN) {
        registerBtn.style.display = 'none';
        return;
    }

    const role = (window.USER_ROLE || '').toUpperCase();

    // MENTOR 또는 ADMIN만 허용
    const hasPermission =
        role === 'MENTOR' ||
        role === 'ROLE_MENTOR' ||
        role === 'ADMIN' ||
        role === 'ROLE_ADMIN';

    if (!hasPermission) {
        registerBtn.style.display = 'none';
    }
});

function goRegister() {
    if (!window.IS_LOGIN) {
        alert('로그인이 필요합니다.');
        location.href = '/login';
        return;
    }

    const role = (window.USER_ROLE || '').toUpperCase();

    const hasPermission =
        role === 'MENTOR' ||
        role === 'ROLE_MENTOR' ||
        role === 'ADMIN' ||
        role === 'ROLE_ADMIN';

    if (!hasPermission) {
        alert('해당 기능은 권한이 없습니다.');
        return;
    }

    location.href = '/board/register';
}
