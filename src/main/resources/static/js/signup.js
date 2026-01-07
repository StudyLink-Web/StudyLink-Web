// 회원가입 폼 처리
document.addEventListener('DOMContentLoaded', function() {
    const signupForm = document.getElementById('signupForm');
    const emailInput = document.getElementById('email');
    const nicknameInput = document.getElementById('nickname');
    const errorDiv = document.getElementById('error');

    // 이메일 중복 확인
    emailInput.addEventListener('blur', checkEmailAvailability);

    // 닉네임 중복 확인
    nicknameInput.addEventListener('blur', checkNicknameAvailability);

    // 폼 제출
    signupForm.addEventListener('submit', handleSignup);
});

/**
 * 이메일 중복 확인
 */
async function checkEmailAvailability() {
    const email = document.getElementById('email').value;
    const errorText = document.getElementById('emailError');

    if (!email) {
        errorText.textContent = '';
        return;
    }

    // 이메일 형식 검증
    if (!isValidEmail(email)) {
        errorText.textContent = '올바른 이메일 형식을 입력하세요.';
        return;
    }

    try {
        const response = await axios.post('/api/auth/check-email', { email });

        if (!response.data.available) {
            errorText.textContent = '이미 사용 중인 이메일입니다.';
        } else {
            errorText.textContent = '';
        }
    } catch (error) {
        console.error('이메일 확인 오류:', error);
        errorText.textContent = '이메일 확인 중 오류가 발생했습니다.';
    }
}

/**
 * 닉네임 중복 확인
 */
async function checkNicknameAvailability() {
    const nickname = document.getElementById('nickname').value;
    const errorText = document.getElementById('nicknameError');

    if (!nickname) {
        errorText.textContent = '';
        return;
    }

    if (nickname.length < 2) {
        errorText.textContent = '닉네임은 2자 이상이어야 합니다.';
        return;
    }

    try {
        const response = await axios.post('/api/auth/check-nickname', { nickname });

        if (!response.data.available) {
            errorText.textContent = '이미 사용 중인 닉네임입니다.';
        } else {
            errorText.textContent = '';
        }
    } catch (error) {
        console.error('닉네임 확인 오류:', error);
        errorText.textContent = '닉네임 확인 중 오류가 발생했습니다.';
    }
}

/**
 * 회원가입 처리
 */
async function handleSignup(e) {
    e.preventDefault();

    const errorDiv = document.getElementById('error');
    errorDiv.classList.remove('show');

    // 폼 데이터 수집
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const name = document.getElementById('name').value;
    const nickname = document.getElementById('nickname').value;
    const role = document.getElementById('role').value;

    // 유효성 검증
    if (!isValidEmail(email)) {
        showError('올바른 이메일 형식을 입력하세요.');
        return;
    }

    if (password.length < 8) {
        showError('비밀번호는 8자 이상이어야 합니다.');
        return;
    }

    if (!name.trim()) {
        showError('이름을 입력하세요.');
        return;
    }

    if (nickname.length < 2) {
        showError('닉네임은 2자 이상이어야 합니다.');
        return;
    }

    if (!role) {
        showError('역할을 선택하세요.');
        return;
    }

    // API 호출
    try {
        const response = await axios.post('/api/auth/signup', {
            email,
            password,
            name,
            nickname,
            role
        });

        // 성공 메시지 표시
        alert('회원가입이 완료되었습니다!');

        // 로그인 페이지로 이동
        window.location.href = '/login';
    } catch (error) {
        const message = error.response?.data?.message || error.message;
        showError('회원가입 실패: ' + message);
        console.error('회원가입 오류:', error);
    }
}

/**
 * 이메일 형식 검증
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * 에러 메시지 표시
 */
function showError(message) {
    const errorDiv = document.getElementById('error');
    errorDiv.textContent = message;
    errorDiv.classList.add('show');

    // 5초 후 자동으로 숨기기
    setTimeout(() => {
        errorDiv.classList.remove('show');
    }, 5000);
}
