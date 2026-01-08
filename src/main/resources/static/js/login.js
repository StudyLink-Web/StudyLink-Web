/**
 * 로그인 페이지 JavaScript (통합 버전)
 * ================================================
 * 기존 기능 (100% 유지):
 * - 비밀번호 표시/숨김 (eye 아이콘)
 * - 이메일 기억하기 (localStorage)
 * - 유효성 검사
 * - Spring Security formLogin 통합
 * - 에러 메시지 처리
 * - 소셜 로그인 버튼
 *
 * 추가 기능:
 * - CSRF 토큰 자동 추가 (Spring Security)
 * - Loading State 관리
 * - URL 파라미터 정리
 * ================================================
 */

document.addEventListener('DOMContentLoaded', function () {
    console.log('✅ 로그인 페이지 초기화 시작');
    initializeLoginPage();

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

/**
 * 초기화 함수
 */
function initializeLoginPage() {
    const loginForm = document.getElementById('loginForm');

    if (loginForm) {
        console.log('✅ 로그인 폼 찾음');

        // ✅ 폼 제출 이벤트 (기존 기능 유지)
        loginForm.addEventListener('submit', function(event) {
            // 유효성 검사 수행 (기존 코드)
            const emailInput = document.getElementById('email');
            const passwordInput = document.getElementById('password');
            const email = emailInput.value.trim();
            const password = passwordInput.value;

            if (!validateLoginForm(email, password)) {
                event.preventDefault();
                return false;
            }

            // 이메일 기억하기 (기존 코드)
            const rememberCheckbox = document.getElementById('remember');
            if (rememberCheckbox && rememberCheckbox.checked) {
                localStorage.setItem('savedEmail', email);
                console.log('💾 이메일 저장됨');
            } else {
                localStorage.removeItem('savedEmail');
                console.log('🗑️ 저장된 이메일 삭제됨');
            }

            // ✅ 추가: 로딩 상태 표시
            showLoadingState();

            // ✅ 추가: CSRF 토큰 자동 추가
            addCsrfTokenIfNeeded(this);

            console.log('📤 로그인 폼 제출 - /loginProc로 이동');
            // preventDefault() 하지 않음 → 폼 자동 제출 (Spring Security 처리)
        });

        // 기존 기능들 초기화
        restoreSavedEmail();
        setupPasswordToggle();
        checkLoginError();
        setupSocialLoginButtons(); // 기존 코드
    } else {
        console.warn('❌ 로그인 폼을 �을 수 없습니다');
    }
}

// ============================================
// ✅ 추가 기능 1: CSRF 토큰 자동 추가
// ============================================

/**
 * CSRF 토큰 추가 (Spring Security)
 * Thymeleaf 또는 meta 태그에서 토큰 가져와 폼에 추가
 * ⭐ IMPORTANT: Spring Security CSRF 보호를 위해 필수
 */
function addCsrfTokenIfNeeded(form) {
    // 1. 이미 _csrf 필드가 있으면 스킵
    if (form.querySelector('input[name="_csrf"]')) {
        console.log('✅ CSRF 토큰 이미 존재');
        return;
    }

    // 2. meta 태그에서 토큰 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');

    if (csrfToken && csrfToken.getAttribute('content')) {
        const tokenInput = document.createElement('input');
        tokenInput.type = 'hidden';
        tokenInput.name = '_csrf';
        tokenInput.value = csrfToken.getAttribute('content');

        form.appendChild(tokenInput);
        console.log('✅ CSRF 토큰 자동 추가됨');
    }
}

// ============================================
// ✅ 추가 기능 2: Loading State 관리
// ============================================

/**
 * 로딩 상태 표시
 * 기존 기능 유지
 */
function showLoadingState() {
    const submitBtn = document.querySelector('.btn-login');

    if (!submitBtn) return;

    const originalText = submitBtn.textContent;
    submitBtn.textContent = '로그인 중...';
    submitBtn.disabled = true;

    // 안전장치: 서버 응답 없을 경우 10초 후 복원
    setTimeout(() => {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }, 10000);

    console.log('⏳ 로딩 상태 표시');
}

// ============================================
// 기존 기능들 (100% 유지)
// ============================================

/**
 * 비밀번호 표시 / 숨김 토글 (eye 아이콘)
 * 기존 기능 100% 유지
 */
function setupPasswordToggle() {
    const toggleBtn = document.getElementById('togglePasswordBtn');
    const passwordInput = document.getElementById('password');
    const eyeOpen = document.getElementById('eyeOpen');
    const eyeClosed = document.getElementById('eyeClosed');

    if (!toggleBtn || !passwordInput || !eyeOpen || !eyeClosed) {
        console.warn('⚠️ 비밀번호 토글 요소 중 일부 없음');
        return;
    }

    toggleBtn.addEventListener('click', function (e) {
        e.preventDefault();

        const isHidden = passwordInput.type === 'password';

        if (isHidden) {
            // 비밀번호 표시
            passwordInput.type = 'text';
            eyeOpen.style.display = 'inline';
            eyeClosed.style.display = 'none';
            console.log('👁️ 비밀번호 표시');
        } else {
            // 비밀번호 숨김
            passwordInput.type = 'password';
            eyeOpen.style.display = 'none';
            eyeClosed.style.display = 'inline';
            console.log('👁️‍🗨️ 비밀번호 숨김');
        }

        // 포커스 유지
        passwordInput.focus();
    });
}

/**
 * 로그인 폼 유효성 검사
 * 기존 기능 100% 유지
 */
function validateLoginForm(email, password) {
    // 이메일 확인
    if (!email) {
        showError('이메일을 입력해주세요.');
        return false;
    }

    // 이메일 형식 검증
    if (!/^\S+@\S+\.\S+$/.test(email)) {
        showError('이메일 형식이 올바르지 않습니다.');
        return false;
    }

    // 비밀번호 확인
    if (!password) {
        showError('비밀번호를 입력해주세요.');
        return false;
    }

    // 비밀번호 길이 검증
    if (password.length < 6) {
        showError('비밀번호는 6자 이상이어야 합니다.');
        return false;
    }

    console.log('✅ 유효성 검사 통과');
    return true;
}

/**
 * 저장된 이메일 복원
 * 기존 기능 100% 유지
 */
function restoreSavedEmail() {
    const savedEmail = localStorage.getItem('savedEmail');
    const emailInput = document.getElementById('email');
    const rememberCheckbox = document.getElementById('remember');

    if (savedEmail && emailInput) {
        emailInput.value = savedEmail;

        if (rememberCheckbox) {
            rememberCheckbox.checked = true;
        }

        console.log('📧 저장된 이메일 복원됨');
    }
}

/**
 * 에러 메시지 표시
 * 기존 기능 100% 유지
 */
function showError(message) {
    const container = document.querySelector('.login-card');
    const existingAlert = container.querySelector('.alert');

    // 기존 알림 제거
    if (existingAlert) {
        existingAlert.remove();
    }

    // 새 알림 생성
    const alert = document.createElement('div');
    alert.className = 'alert alert-danger alert-dismissible fade show';
    alert.innerHTML = `
        <div style="display: flex; gap: 8px; align-items: center;">
            <span>❌ ${message}</span>
            <button type="button" style="background: none; border: none; cursor: pointer; color: #666;">✕</button>
        </div>
    `;

    container.insertBefore(alert, container.firstChild);

    // 닫기 버튼 이벤트
    const closeBtn = alert.querySelector('button');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => alert.remove());
    }

    // 5초 후 자동 제거
    setTimeout(() => {
        if (alert.parentElement) {
            alert.remove();
        }
    }, 5000);

    console.warn('❌ 에러:', message);
}

/**
 * 로그인 에러 파라미터 확인 (Spring Security)
 * 기존 기능 100% 유지
 *
 * Spring Security가 로그인 실패 시 다음과 같이 파라미터 전달:
 * - /login?error=true → 로그인 실패
 * - /login?expired=true → 세션 만료
 */
function checkLoginError() {
    const params = new URLSearchParams(window.location.search);

    // 로그인 실패
    if (params.has('error')) {
        showError('이메일 또는 비밀번호가 올바르지 않습니다.');

        // ✅ 추가: URL에서 error 파라미터 제거
        window.history.replaceState({}, document.title, '/login');

        // 이메일 필드에 포커스
        setTimeout(() => {
            document.getElementById('email').focus();
        }, 100);
    }

    // 세션 만료
    if (params.has('expired')) {
        showError('세션이 만료되었습니다. 다시 로그인해주세요.');

        // ✅ 추가: URL에서 expired 파라미터 제거
        window.history.replaceState({}, document.title, '/login');

        // 저장된 이메일만 유지하고 비밀번호는 초기화
        document.getElementById('password').value = '';
        document.getElementById('email').focus();
    }
}

/**
 * 로그인 성공 시 페이지 새로고침 (헤더 업데이트용)
 * Spring Security의 formLogin 처리 후 리다이렉트되면 실행
 */
function checkLoginSuccess() {
    const params = new URLSearchParams(window.location.search);

    // 로그인 성공 후 dashboard로 리다이렉트되었는지 확인
    // (현재 경로가 /login이 아니면 = 로그인 성공했다는 뜻)
    const currentPath = window.location.pathname;

    // login 페이지가 아니면 이 함수는 실행되지 않음 (정상)
    if (currentPath !== '/login') {
        console.log('✅ 로그인 페이지가 아님 (로그인 성공함)');
    }
}


/**
 * 소셜 로그인 버튼 (추후 구현)
 * 기존 기능 100% 유지
 */
function setupSocialLoginButtons() {
    const socialButtons = document.querySelectorAll('.btn-social');

    socialButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('🔗 소셜 로그인 클릭:', this.className);

            let message = '';
            if (this.classList.contains('btn-kakao')) {
                message = '🟡 카카오 로그인은 준비 중입니다.';
            } else if (this.classList.contains('btn-naver')) {
                message = '🟢 네이버 로그인은 준비 중입니다.';
            } else if (this.classList.contains('btn-google')) {
                message = '🔵 구글 로그인은 준비 중입니다.';
            }

            if (message) {
                alert(message);
            }
        });
    });
}