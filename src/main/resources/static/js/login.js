// 로그인 페이지 초기화
function initializeLogin() {
    console.log('StudyLink 로그인 페이지 로드 완료');
    const params = new URLSearchParams(window.location.search);
    if (params.has('error')) {
        showError('로그인에 실패했습니다. 다시 시도해주세요.');
    }
}

// OAuth 버튼 클릭 처리
function handleOAuthClick(event, provider) {
    if (!checkLoginAttempts()) {
        event.preventDefault();
        return;
    }
    console.log(`${provider.toUpperCase()} 로그인 시도...`);
    showLoadingState(event.target.closest('a'));
    logLoginAttempt(provider);
}

// 로그인 시도 제한 (보안)
function checkLoginAttempts() {
    const now = Date.now();
    if (LOGIN_ATTEMPTS.count >= LOGIN_ATTEMPTS.max) {
        const remainingTime = Math.ceil((LOGIN_ATTEMPTS.resetTime - (now - LOGIN_ATTEMPTS.lastAttempt)) / 1000 / 60);
        showError(`너무 많은 시도가 있었습니다. ${remainingTime}분 후에 다시 시도해주세요.`);
        return false;
    }
    LOGIN_ATTEMPTS.count++;
    return true;
}

// 에러 메시지 표시
function showError(message) {
    const errorElement = document.getElementById('errorMessage');
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
        setTimeout(() => fadeOut(errorElement), 8000);
    }
}
