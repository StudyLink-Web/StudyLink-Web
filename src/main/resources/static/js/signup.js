// ============================================
// StudyLink 회원가입 JavaScript
// ============================================

// API 기본 URL
//const API_BASE_URL = 'http://localhost:8088';
//const API_BASE_URL = '';
const API_BASE_URL = window.location.origin;  // 현재 도메인 자동 사용

// ============================================
// DOM 초기화
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    const signupForm = document.getElementById('signupForm');
    const emailInput = document.getElementById('email');
    const nicknameInput = document.getElementById('nickname');
    const passwordInput = document.getElementById('password');

    // 이벤트 리스너 등록
    if (emailInput) emailInput.addEventListener('blur', checkEmailAvailability);
    if (nicknameInput) nicknameInput.addEventListener('blur', checkNicknameAvailability);
    if (passwordInput) passwordInput.addEventListener('input', validatePassword);

    // ✅ 폼 제출 이벤트
    if (signupForm) {
        signupForm.addEventListener('submit', handleSignup);
    }

    // ✅ 비밀번호 토글 버튼 초기화
    initPasswordToggle();

    console.log('✅ 회원가입 폼 초기화 완료');

    // ⭐ 드롭다운 강제 닫기 (시간을 1000ms로 늘림)
    setTimeout(() => {
        const dropdownMenu = document.querySelector('.dropdown-menu');
        if (dropdownMenu) {
            dropdownMenu.classList.remove('show');
            dropdownMenu.classList.remove('active');  // ← active도 제거
            console.log('✅ 로그인 페이지: 드롭다운 닫음');
        }
    }, 1000);  // ← 500ms → 1000ms로 변경
});

// ============================================
// 비밀번호 표시/숨김 기능
// ============================================

function initPasswordToggle() {
    const passwordInput = document.getElementById('password');
    const toggleBtn = document.querySelector('.password-toggle');

    if (!passwordInput || !toggleBtn) return;

    toggleBtn.addEventListener('click', function(e) {
        e.preventDefault();

        const isPassword = passwordInput.type === 'password';
        passwordInput.type = isPassword ? 'text' : 'password';

        // SVG 아이콘 토글 (수정됨!)
        const svgs = toggleBtn.querySelectorAll('svg');
        if (svgs.length >= 2) {
            if (isPassword) {
                svgs[0].style.display = 'none';    // ✅ eye closed 숨김
                svgs[1].style.display = 'block';   // ✅ eye open 표시
            } else {
                svgs[0].style.display = 'block';   // ✅ eye closed 표시
                svgs[1].style.display = 'none';    // ✅ eye open 숨김
            }
        }
    });
}

// ============================================
// 이메일 중복 확인
// ============================================

async function checkEmailAvailability() {
    const email = document.getElementById('email').value;
    const emailError = document.getElementById('emailError');

    if (!emailError) return;

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
        // ✅ fetch로 변경 (axios 제거)
        const response = await fetch(`${API_BASE_URL}/api/auth/check-email`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email })
        });

        const data = await response.json();

        if (!data.available) {
            emailError.textContent = '❌ 이미 사용 중인 이메일입니다.';
            emailError.style.color = '#dc2626';
        } else {
            emailError.textContent = '✅ 사용 가능한 이메일입니다.';
            emailError.style.color = '#16a34a';
        }
    } catch (error) {
        console.error('이메일 확인 오류:', error);
        emailError.textContent = '⚠️ 이메일 확인 중 오류가 발생했습니다.';
        emailError.style.color = '#dc2626';
    }
}

// ============================================
// 닉네임 중복 확인
// ============================================

async function checkNicknameAvailability() {
    const nickname = document.getElementById('nickname').value;
    const nicknameError = document.getElementById('nicknameError');

    if (!nicknameError) return;

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
        // ✅ fetch로 변경 (axios 제거)
        const response = await fetch(`${API_BASE_URL}/api/auth/check-nickname`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ nickname })
        });

        const data = await response.json();

        if (!data.available) {
            nicknameError.textContent = '❌ 이미 사용 중인 닉네임입니다.';
            nicknameError.style.color = '#dc2626';
        } else {
            nicknameError.textContent = '✅ 사용 가능한 닉네임입니다.';
            nicknameError.style.color = '#16a34a';
        }
    } catch (error) {
        console.error('닉네임 확인 오류:', error);
        nicknameError.textContent = '⚠️ 닉네임 확인 중 오류가 발생했습니다.';
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
    if (errorDiv) {
        errorDiv.classList.remove('show');
    }

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
        // ✅ fetch로 변경 (axios 제거)
        const response = await fetch(`${API_BASE_URL}/api/auth/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email,
                password,
                name: name.trim(),
                nickname,
                role
            })
        });

        const data = await response.json();

        if (response.ok) {
            // 성공 처리
            showSuccess('✅ 회원가입이 완료되었습니다! 로그인 페이지로 이동합니다...');

            // 2초 후 로그인 페이지로 이동
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        } else {
            // ✅ 에러 응답 처리
            let errorMessage = data.message || '회원가입 실패';

            if (data.error === 'INVALID_EMAIL') {
                showError('❌ ' + errorMessage);
            } else if (data.error === 'INVALID_PASSWORD') {
                showError('❌ ' + errorMessage);
            } else if (data.error === 'INVALID_ROLE') {
                showError('❌ ' + errorMessage);
            } else if (data.error === 'INVALID_NICKNAME') {
                showError('❌ ' + errorMessage);
            } else if (data.error === 'INVALID_NAME') {
                showError('❌ ' + errorMessage);
            } else if (data.error === 'SIGNUP_ERROR') {
                showError('❌ ' + errorMessage);
            } else {
                showError('❌ 회원가입 실패: ' + errorMessage);
            }

            // 버튼 복원
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }

    } catch (error) {
        console.error('회원가입 오류:', error);
        showError('❌ 회원가입 중 오류가 발생했습니다.');

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
    errorDiv.style.display = 'block';

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
    errorDiv.style.display = 'block';

    // 3초 후 자동으로 숨기기
    setTimeout(() => {
        errorDiv.classList.remove('show');
    }, 3000);

    // 페이지 최상단으로 스크롤
    window.scrollTo({ top: 0, behavior: 'smooth' });
}
