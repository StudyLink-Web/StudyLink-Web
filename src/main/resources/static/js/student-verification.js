/* ===========================
   Student Verification JavaScript
   =========================== */

document.addEventListener('DOMContentLoaded', function() {
    // ⭐ 인증 완료 여부 확인
    const verifiedElement = document.querySelector('.alert-success-verified');

    if (verifiedElement) {
        // 인증 완료 상태 - 폼 관련 함수 실행 안 함
        console.log('✅ 대학생 인증이 이미 완료되었습니다');
    } else {
        // 인증 미완료 상태 - 폼 설정
        setupEmailCheckButton();
        setupFormSubmit();
        setupEmailInput();
        // ⭐⭐⭐ 새로 추가됨: 페이지 로드 시 쿨다운 확인
        checkResendCooldown();
    }
});

/**
 * 중복확인 버튼 이벤트
 */
function setupEmailCheckButton() {
    const checkEmailBtn = document.getElementById('checkEmailBtn');
    if (!checkEmailBtn) return;

    checkEmailBtn.addEventListener('click', async () => {
        const email = document.getElementById('schoolEmail').value.trim();
        const statusEl = document.getElementById('emailStatus');

        if (!email) {
            statusEl.textContent = '❌ 이메일을 입력하세요';
            statusEl.className = 'form-text error';
            return;
        }

        // ⭐ 이메일 형식 검증
        if (!isValidEmail(email)) {
            statusEl.textContent = '❌ 올바른 이메일 형식을 입력하세요';
            statusEl.className = 'form-text error';
            return;
        }

        try {
            const response = await fetch('/auth/student-verification/check-email?email=' + encodeURIComponent(email));
            const data = await response.json();

            if (data.available) {
                statusEl.textContent = '✅ ' + data.message;
                statusEl.className = 'form-text success';
                document.getElementById('submitBtn').disabled = false;
            } else {
                statusEl.textContent = '❌ ' + data.message;
                statusEl.className = 'form-text error';
                document.getElementById('submitBtn').disabled = true;
            }
        } catch (error) {
            console.error('❌ 중복확인 오류:', error);
            statusEl.textContent = '❌ 확인 중 오류가 발생했습니다';
            statusEl.className = 'form-text error';
        }
    });
}

/**
 * 폼 제출 이벤트
 */
function setupFormSubmit() {
    const verificationForm = document.getElementById('verificationForm');
    if (!verificationForm) return;

    verificationForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('schoolEmail').value.trim();
        const submitBtn = document.getElementById('submitBtn');
        const originalText = submitBtn.textContent;

        // ⭐ 최종 검증
        if (!isValidEmail(email)) {
            showError('올바른 이메일 형식을 입력하세요');
            return;
        }

        // 로딩 상태
        submitBtn.disabled = true;
        submitBtn.textContent = '전송 중...';

        try {
            // ⭐ CSRF 토큰 추가
            const csrfToken = document.querySelector('input[name="_csrf"]').value;

            const response = await fetch('/auth/student-verification/request-verification', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify({ email })
            });

            const data = await response.json();

            if (data.success) {
                // ✅ 성공
                hideForm();
                showSuccess();
                // ⭐⭐⭐ 새로 추가됨: 성공 후 쿨다운 정보 표시
                if (data.remainingSeconds) {
                    startCooldownTimer(data.remainingSeconds);
                }
                console.log('✅ 인증 이메일 전송 성공');
            } else if (data.code === 'COOLDOWN_ACTIVE') {
                // ⭐⭐⭐ 새로 추가됨: 쿨다운 중인 경우 처리
                showCooldownWarning(data.remainingMinutes, data.remainingSeconds);
                submitBtn.disabled = false;
                submitBtn.textContent = originalText;
                console.log('⏳ 이메일 재전송 쿨다운 진행 중:', data.message);
            } else {
                // ❌ 실패
                showError(data.message || '인증 요청에 실패했습니다');
                submitBtn.disabled = false;
                submitBtn.textContent = originalText;
                console.log('❌ 인증 이메일 전송 실패:', data.message);
            }
        } catch (error) {
            console.error('❌ 폼 제출 오류:', error);
            showError('서버 오류가 발생했습니다');
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    });
}

/**
 * 이메일 입력 시 상태 초기화
 */
function setupEmailInput() {
    const schoolEmailInput = document.getElementById('schoolEmail');
    if (!schoolEmailInput) return;

    schoolEmailInput.addEventListener('input', () => {
        const statusEl = document.getElementById('emailStatus');
        statusEl.textContent = '';
        statusEl.className = 'form-text';

        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = false;
    });
}

/**
 * ⭐ 이메일 형식 검증
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * ⭐ 폼 숨기기
 */
function hideForm() {
    const form = document.getElementById('verificationForm');
    const infoBox = document.querySelector('.info-box');
    const logoutSection = document.querySelector('.logout-section');

    if (form) form.style.display = 'none';
    if (infoBox) infoBox.style.display = 'none';
    if (logoutSection) logoutSection.style.display = 'none';
}

/**
 * ⭐ 성공 메시지 표시
 */
function showSuccess() {
    document.getElementById('verificationForm').style.display = 'none';
    document.getElementById('successMessage').style.display = 'block';
    document.getElementById('errorMessage').style.display = 'none';
}

/**
 * ⭐ 에러 메시지 표시
 */
function showError(message) {
    document.getElementById('verificationForm').style.display = 'block';
    document.getElementById('successMessage').style.display = 'none';
    document.getElementById('errorMessage').style.display = 'block';
    document.getElementById('errorText').textContent = message;
}

/**
 * 페이지 로드 시 쿨다운 상태 확인
 */
function checkResendCooldown() {
    fetch('/auth/student-verification/resend-cooldown')
        .then(res => res.json())
        .then(data => {
            if (!data.canResend) {
                // 쿨다운 진행 중
                showCooldownWarning(data.remainingMinutes, data.remainingSeconds);
                startCooldownTimer(data.remainingSeconds);
            } else {
                // 쿨다운 종료
                hideCooldownWarning();
            }
        })
        .catch(error => console.error('❌ 쿨다운 확인 오류:', error));
}

/**
 * 쿨다운 경고 메시지 표시
 */
function showCooldownWarning(minutes, seconds) {
    const cooldownWarning = document.getElementById('cooldownWarning');
    const cooldownMinutes = document.getElementById('cooldownMinutes');
    const submitBtn = document.getElementById('submitBtn');

    if (cooldownWarning && cooldownMinutes) {
        cooldownMinutes.textContent = minutes;
        cooldownWarning.style.display = 'block';
        submitBtn.disabled = true;
    }
}

/**
 * 쿨다운 경고 메시지 숨기기
 */
function hideCooldownWarning() {
    const cooldownWarning = document.getElementById('cooldownWarning');
    const submitBtn = document.getElementById('submitBtn');

    if (cooldownWarning) {
        cooldownWarning.style.display = 'none';
    }
    if (submitBtn) {
        submitBtn.disabled = false;
    }
}

/**
 * 실시간 쿨다운 타이머 시작
 */
function startCooldownTimer(remainingSeconds) {
    const cooldownTimer = document.getElementById('cooldownTimer');
    const cooldownWarning = document.getElementById('cooldownWarning');
    const submitBtn = document.getElementById('submitBtn');
    let remaining = remainingSeconds;

    if (!cooldownTimer) return;

    // 타이머 업데이트 함수
    const updateTimer = () => {
        if (remaining <= 0) {
            // 쿨다운 종료
            hideCooldownWarning();
            if (cooldownTimer) {
                cooldownTimer.textContent = '';
            }
            return;
        }

        // 시간:분:초 계산
        const hours = Math.floor(remaining / 3600);
        const minutes = Math.floor((remaining % 3600) / 60);
        const seconds = remaining % 60;

        // 포맷팅 (MM:SS 또는 HH:MM:SS)
        let timeString;
        if (hours > 0) {
            timeString = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        } else {
            timeString = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        }

        cooldownTimer.textContent = `⏳ ${timeString} 후에 다시 시도하세요`;
        remaining--;

        // 1초마다 업데이트
        setTimeout(updateTimer, 1000);
    };

    // 첫 번째 업데이트
    updateTimer();
}