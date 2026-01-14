function goRegister() {
    if (!window.IS_LOGIN) {
        alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
        location.href = '/user/login';
        return;
    }
    location.href = '/board/register';
}