// ============================================
// StudyLink 회원가입 JavaScript
// ============================================

// API 기본 URL
const API_BASE_URL = 'http://localhost:8088';

// ============================================
// DOM 초기화
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    const signupForm = document.getElementById('signupForm');
    const emailInput = document.getElementById('email');
    const nicknameInput = document.getElementById('nickname');
    const passwordInput = document.getElementById('password');
    const passwordToggle = document.getElementById('passwordToggle');

    // 이벤트 리스너 등록
    emailInput.addEventListener('blur', checkEmailAvailability);
    nicknameInput.addEventListener('blur', checkNicknameAvailability);
    passwordInput.addEventListener('input', validatePassword);
    passwordToggle.addEventListener('click', togglePasswordVisibility);
    signupForm.addEventListener('submit', handleSignup);

    console.log('✅ 회원가입 폼 초기화 완료');
});

// ============================================
// 비밀번호 표시/숨김 기능
// ============================================

function togglePasswordVisibility() {
    const passwordInput = document.getElementById('password');
    const passwordToggle = document.getElementById('passwordToggle');
    const eyeIcon = passwordToggle.querySelector('.eye-icon');

    // 입력 타입 전환
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';

        // SVG를 눈 닫힘 아이콘으로 변경
        eyeIcon.innerHTML = `
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
            <line x1="1" y1="1" x2="23" y2="23"></line>
        `;
        passwordInput.focus();
    } else {
        passwordInput.type = 'password';

        // SVG를 눈 열림 아이콘으로 변경
        eyeIcon.innerHTML = `
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
        `;
        passwordInput.focus();
    }
}

// ============================================
// 이메일 중복 확인
// ============================================

async function checkEmailAvailability() {
    const email = document.getElementById('email').value;
    const emailError = document.getElementById('emailError');

    // 입력값이 없으면 메시지 제거
    if (!email) {
        emailError.textContent = '';
        emailError.style.color = '';
        return;
    }

    // 이메일 형식 검증
    if (!isValidEmail(email)) {
        emailError.textContent = '❌ 올바른 이메일 형식을 입력하세요.';
        emailError.style.color = '#dc2626';
        return;
    }

    try {
        // API 호출
        const response = await axios.post(`${API_BASE_URL}/api/auth/check-email`, { email });

        if (!response.data.available) {
            emailError.textContent = '❌ 이미 사용 중인 이메일입니다.';
            emailError.style.color = '#dc2626';
        } else {
            emailError.textContent = '✅ 사용 가능한 이메일입니다.';
            emailError.style.color = '#16a34a';
        }
    } catch (error) {
        console.error('이메일 확인 오류:', error);

        // 오류 응답 처리
        if (error.response?.status === 409) {
            emailError.textContent = '❌ 이미 사용 중인 이메일입니다.';
        } else {
            emailError.textContent = '⚠️ 이메일 확인 중 오류가 발생했습니다.';
        }
        emailError.style.color = '#dc2626';
    }
}

// ============================================
// 닉네임 중복 확인
// ============================================

async function checkNicknameAvailability() {
    const nickname = document.getElementById('nickname').value;
    const nicknameError = document.getElementById('nicknameError');

    // 입력값이 없으면 메시지 제거
    if (!nickname) {
        nicknameError.textContent = '';
        nicknameError.style.color = '';
        return;
    }

    // 닉네임 길이 검증
    if (nickname.length < 2) {
        nicknameError.textContent = '⚠️ 닉네임은 2자 이상이어야 합니다.';
        nicknameError.style.color = '#ea580c';
        return;
    }

    if (nickname.length > 20) {
        nicknameError.textContent = '⚠️ 닉네임은 20자 이하여야 합니다.';
        nicknameError.style.color = '#ea580c';
        return;
    }

    try {
        // API 호출
        const response = await axios.post(`${API_BASE_URL}/api/auth/check-nickname`, { nickname });

        if (!response.data.available) {
            nicknameError.textContent = '❌ 이미 사용 중인 닉네임입니다.';
            nicknameError.style.color = '#dc2626';
        } else {
            nicknameError.textContent = '✅ 사용 가능한 닉네임입니다.';
            nicknameError.style.color = '#16a34a';
        }
    } catch (error) {
        console.error('닉네임 확인 오류:', error);

        // 오류 응답 처리
        if (error.response?.status === 409) {
            nicknameError.textContent = '❌ 이미 사용 중인 닉네임입니다.';
        } else {
            nicknameError.textContent = '⚠️ 닉네임 확인 중 오류가 발생했습니다.';
        }
        nicknameError.style.color = '#dc2626';
    }
}

// ============================================
// 비밀번호 유효성 검증
// ============================================

function validatePassword() {
    const password = document.getElementById('password').value;
    const passwordError = document.getElementById('passwordError');

    if (!passwordError) return;

    if (!password) {
        passwordError.textContent = '';
        return;
    }

    let messages = [];

    // 8자 이상 확인
    if (password.length < 8) {
        messages.push('8자 이상');
    }

    // 대문자 확인 (선택사항)
    if (!/[A-Z]/.test(password)) {
        messages.push('대문자');
    }

    // 소문자 확인 (선택사항)
    if (!/[a-z]/.test(password)) {
        messages.push('소문자');
    }

    // 숫자 확인 (선택사항)
    if (!/[0-9]/.test(password)) {
        messages.push('숫자');
    }

    // 특수문자 확인 (선택사항)
    if (!/[!@#$%^&*]/.test(password)) {
        messages.push('특수문자(!@#$%^&*)');
    }

    if (messages.length > 0) {
        passwordError.textContent = `⚠️ 권장: ${messages.join(', ')}를 포함하세요.`;
        passwordError.style.color = '#ea580c';
    } else {
        passwordError.textContent = '✅ 강력한 비밀번호입니다.';
        passwordError.style.color = '#16a34a';
    }
}

// ============================================
// 회원가입 처리
// ============================================

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

    // ===== 입력값 검증 =====

    if (!isValidEmail(email)) {
        showError('❌ 올바른 이메일 형식을 입력하세요.');
        return;
    }

    if (password.length < 8) {
        showError('❌ 비밀번호는 8자 이상이어야 합니다.');
        return;
    }

    if (!name.trim()) {
        showError('❌ 이름을 입력하세요.');
        return;
    }

    if (name.trim().length > 50) {
        showError('❌ 이름은 50자 이하여야 합니다.');
        return;
    }

    if (nickname.length < 2) {
        showError('❌ 닉네임은 2자 이상이어야 합니다.');
        return;
    }

    if (nickname.length > 20) {
        showError('❌ 닉네임은 20자 이하여야 합니다.');
        return;
    }

    if (!role) {
        showError('❌ 역할을 선택하세요.');
        return;
    }

    // ===== 중복 확인 상태 검증 =====

    const emailError = document.getElementById('emailError')?.textContent || '';
    const nicknameError = document.getElementById('nicknameError')?.textContent || '';

    if (emailError.includes('사용 중')) {
        showError('❌ 이미 사용 중인 이메일입니다.');
        return;
    }

    if (nicknameError.includes('사용 중')) {
        showError('❌ 이미 사용 중인 닉네임입니다.');
        return;
    }

    // ===== 로딩 상태 =====

    const submitBtn = document.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = '⏳ 가입 중...';

    try {
        // API 호출
        const response = await axios.post(`${API_BASE_URL}/api/auth/signup`, {
            email,
            password,
            name: name.trim(),
            nickname,
            role
        });

        // 성공 처리
        showSuccess('✅ 회원가입이 완료되었습니다! 로그인 페이지로 이동합니다...');

        // 2초 후 로그인 페이지로 이동
        setTimeout(() => {
            window.location.href = '/login';
        }, 2000);

    } catch (error) {
        console.error('회원가입 오류:', error);

        // 오류 메시지 처리
        let errorMessage = '회원가입 실패: ';

        if (error.response?.data?.message) {
            errorMessage += error.response.data.message;
        } else if (error.response?.status === 409) {
            errorMessage += '이미 사용 중인 이메일 또는 닉네임입니다.';
        } else if (error.response?.status === 400) {
            errorMessage += '입력값이 올바르지 않습니다.';
        } else if (error.response?.status === 500) {
            errorMessage += '서버 오류가 발생했습니다.';
        } else {
            errorMessage += error.message || '알 수 없는 오류가 발생했습니다.';
        }

        showError('❌ ' + errorMessage);

        // 버튼 복원
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }
}

// ============================================
// 유틸리티 함수
// ============================================

/**
 * 이메일 형식 검증
 * @param {string} email - 검증할 이메일
 * @returns {boolean} - 유효한 이메일 형식 여부
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * 에러 메시지 표시
 * @param {string} message - 표시할 메시지
 */
function showError(message) {
    const errorDiv = document.getElementById('error');

    if (!errorDiv) {
        console.error('error 요소를 찾을 수 없습니다.');
        alert(message);
        return;
    }

    errorDiv.textContent = message;
    errorDiv.style.color = '#dc2626';
    errorDiv.style.backgroundColor = '#fee2e2';
    errorDiv.style.padding = '12px';
    errorDiv.style.borderRadius = '6px';
    errorDiv.style.marginBottom = '16px';
    errorDiv.style.border = '1px solid #fecaca';
    errorDiv.classList.add('show');

    // 5초 후 자동으로 숨기기
    setTimeout(() => {
        errorDiv.classList.remove('show');
    }, 5000);

    // 페이지 최상단으로 스크롤
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * 성공 메시지 표시
 * @param {string} message - 표시할 메시지
 */
function showSuccess(message) {
    const errorDiv = document.getElementById('error');

    if (!errorDiv) {
        console.error('error 요소를 찾을 수 없습니다.');
        alert(message);
        return;
    }

    errorDiv.textContent = message;
    errorDiv.style.color = '#16a34a';
    errorDiv.style.backgroundColor = '#f0fdf4';
    errorDiv.style.padding = '12px';
    errorDiv.style.borderRadius = '6px';
    errorDiv.style.marginBottom = '16px';
    errorDiv.style.border = '1px solid #bbf7d0';
    errorDiv.classList.add('show');

    // 3초 후 자동으로 숨기기
    setTimeout(() => {
        errorDiv.classList.remove('show');
    }, 3000);

    // 페이지 최상단으로 스크롤
    window.scrollTo({ top: 0, behavior: 'smooth' });
}