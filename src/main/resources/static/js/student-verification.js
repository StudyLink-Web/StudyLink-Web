/* ===========================
   Student Verification JavaScript
   =========================== */

document.addEventListener('DOMContentLoaded', function() {
    setupEmailCheckButton();
    setupFormSubmit();
    setupEmailInput();
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
                // 성공
                document.getElementById('verificationForm').style.display = 'none';
                document.getElementById('successMessage').style.display = 'block';
                document.getElementById('errorMessage').style.display = 'none';
                console.log('✅ 인증 이메일 전송 성공');
            } else {
                // 실패
                document.getElementById('verificationForm').style.display = 'block';
                document.getElementById('successMessage').style.display = 'none';
                document.getElementById('errorMessage').style.display = 'block';
                document.getElementById('errorText').textContent = data.message || '인증 요청에 실패했습니다';
                submitBtn.disabled = false;
                submitBtn.textContent = originalText;
                console.log('❌ 인증 이메일 전송 실패:', data.message);
            }
        } catch (error) {
            console.error('❌ 폼 제출 오류:', error);
            document.getElementById('verificationForm').style.display = 'block';
            document.getElementById('errorMessage').style.display = 'block';
            document.getElementById('errorText').textContent = '서버 오류가 발생했습니다';
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