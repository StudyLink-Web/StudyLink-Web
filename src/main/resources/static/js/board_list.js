function goRegister() {
    // 비로그인
    if (!window.IS_LOGIN) {
        alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
        location.href = '/user/login';
        return;
    }

    // STUDENT 또는 ROLE_USER → 차단
    if (window.USER_ROLE === 'STUDENT' || window.USER_ROLE === 'ROLE_USER') {
        alert('해당 기능은 권한이 없습니다.');
        return;
    }

    // 허용
    location.href = '/board/register';
}