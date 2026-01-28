let recaptchaVerifier = null;

console.log('📋 mentor-profile.js 로드됨');

// ⭐ 즉시 실행
console.log('🚀 탭 시스템 즉시 초기화');

const tabButtons = document.querySelectorAll('.tab-btn');
console.log('탭 버튼 개수:', tabButtons.length);

tabButtons.forEach(btn => {
    console.log('탭 버튼 등록:', btn.dataset.tab);
    btn.onclick = function (e) {
        console.log('🔵 탭 클릭:', this.dataset.tab);
        e.preventDefault();
        e.stopPropagation();

        const tabName = this.dataset.tab;
        const tabElement = document.getElementById(tabName);

        if (!tabElement) {
            console.error('탭 없음:', tabName);
            return false;
        }

        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

        this.classList.add('active');
        tabElement.classList.add('active');

        console.log('✅ 탭 변경:', tabName);
        return false;
    };
});

console.log('✅ 탭 시스템 준비 완료');

// ================================================
// 🔄 토글 체크박스 시스템 초기화
// ================================================

console.log('📚 토글 체크박스 시스템 로드됨');

document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 토글 체크박스 시스템 초기화 시작');
    initializeCollapsibleSubjects();
});

function initializeCollapsibleSubjects() {
    const mainSubjects = document.querySelectorAll('.main-subject');

    console.log('📋 메인 과목 개수:', mainSubjects.length);

    mainSubjects.forEach((checkbox) => {
        const toggleId = checkbox.dataset.toggle;
        const contentDiv = document.getElementById(toggleId);

        if (!contentDiv) {
            console.warn(`⚠️ 토글 컨테이너를 찾을 수 없음: ${toggleId}`);
            return;
        }

        console.log(`✅ 토글 과목 등록: ${checkbox.id} → ${toggleId}`);

        checkbox.addEventListener('change', (e) => {
            toggleSubjectContent(contentDiv, checkbox.checked);
            console.log(`🔄 ${checkbox.id} 토글됨: ${checkbox.checked ? '펼침' : '접음'}`);
        });

        if (checkbox.checked) {
            contentDiv.classList.add('show');
            console.log(`📂 초기 상태: ${toggleId} 표시됨`);
        }

        syncSubitemCheckboxes(checkbox, contentDiv);
    });

    console.log('✅ 토글 체크박스 시스템 준비 완료');
}

function toggleSubjectContent(contentDiv, show) {
    if (show) {
        contentDiv.classList.add('show');
        contentDiv.style.display = 'flex';
        contentDiv.style.flexDirection = 'column';
    } else {
        contentDiv.classList.remove('show');
        contentDiv.style.display = 'none';
    }
}

function syncSubitemCheckboxes(mainCheckbox, contentDiv) {
    const subCheckboxes = contentDiv.querySelectorAll('.checkbox-input');

    console.log(`🔗 세부과목 개수 (${mainCheckbox.id}): ${subCheckboxes.length}`);

    subCheckboxes.forEach((subCheckbox) => {
        subCheckbox.addEventListener('change', () => {
            const hasCheckedSubitem = Array.from(subCheckboxes).some(
                (cb) => cb.checked
            );

            if (mainCheckbox.checked !== hasCheckedSubitem) {
                mainCheckbox.checked = hasCheckedSubitem;
                console.log(
                    `🔄 메인 체크박스 동기화: ${mainCheckbox.id} = ${hasCheckedSubitem}`
                );
            }
        });
    });

    const hasCheckedSubitem = Array.from(subCheckboxes).some((cb) => cb.checked);
    if (hasCheckedSubitem && !mainCheckbox.checked) {
        mainCheckbox.checked = true;
        console.log(`📌 초기 동기화: ${mainCheckbox.id} 자동 체크됨`);
    }
}

// Firebase 변수 선언
let confirmationResult = null;
let authTimer = null;
let authTimeRemaining = 300;
let phoneAuthVerified = false;

/* =========================
   🔔 인증 메시지 표시 함수 (누락 보완)
========================= */
function showAuthMessage(message, type) {
    const msgEl = document.getElementById('authMessage');
    if (!msgEl) return;

    msgEl.textContent = message;
    msgEl.className = 'auth-message ' + type;
}

/* =========================
   ⏱ 인증 타이머
========================= */
function startAuthTimer() {
    authTimeRemaining = 300;
    const timerEl = document.querySelector('#authTimer span');

    if (authTimer) clearInterval(authTimer);

    authTimer = setInterval(() => {
        authTimeRemaining--;

        const min = String(Math.floor(authTimeRemaining / 60)).padStart(2, '0');
        const sec = String(authTimeRemaining % 60).padStart(2, '0');

        if (timerEl) timerEl.textContent = `${min}:${sec}`;

        if (authTimeRemaining <= 0) {
            clearInterval(authTimer);
            showAuthMessage('인증 시간이 만료되었습니다. 다시 요청해주세요.', 'error');
        }
    }, 1000);
}

/* =========================
   📱 전화번호 인증 요청
========================= */
function requestPhoneAuth() {
    window.firebaseInitPromise
        .then(async () => {
            const phoneInput = document.getElementById('phone');
            const phone = phoneInput.value.replace(/\D/g, '');

            if (!phone || phone.length !== 11) {
                showAuthMessage('올바른 전화번호를 입력해주세요 (01X-XXXX-XXXX)', 'error');
                return;
            }

            const formattedPhone = '+82' + phone.slice(1);

            const sendBtn = document.getElementById('sendAuthBtn');
            sendBtn.disabled = true;
            sendBtn.textContent = '발송 중...';

            console.log('📱 전화번호 인증 요청:', formattedPhone);

            const auth = window.firebaseAuth;

            // ✅ Firebase v9 RecaptchaVerifier (정상 방식)
            if (!recaptchaVerifier) {
                const { RecaptchaVerifier } = await import(
                    'https://www.gstatic.com/firebasejs/9.23.0/firebase-auth.js'
                    );

                recaptchaVerifier = new RecaptchaVerifier(
                    'recaptcha-container',
                    {
                        size: 'invisible',
                        callback: () => console.log('✅ reCAPTCHA 완료'),
                        'expired-callback': () => console.log('⚠️ reCAPTCHA 만료')
                    },
                    auth
                );

                await recaptchaVerifier.render();
                console.log('🧩 reCAPTCHA 위젯 생성 완료');
            }

            const { signInWithPhoneNumber } = await import(
                'https://www.gstatic.com/firebasejs/9.23.0/firebase-auth.js'
                );

            confirmationResult = await signInWithPhoneNumber(
                auth,
                formattedPhone,
                recaptchaVerifier
            );

            console.log('✅ SMS 발송 성공');

            document.getElementById('authCodeSection').style.display = 'block';
            document.getElementById('authCode').focus();

            showAuthMessage('인증번호가 발송되었습니다. 문자를 확인해주세요.', 'success');
            startAuthTimer();

            sendBtn.disabled = false;
            sendBtn.textContent = '인증번호 재전송';
        })
        .catch(error => {
            console.error('❌ SMS 발송 실패:', error);
            showAuthMessage('인증번호 발송에 실패했습니다.', 'error');

            const sendBtn = document.getElementById('sendAuthBtn');
            sendBtn.disabled = false;
            sendBtn.textContent = '인증번호 받기';
        });
}

/* =========================
   🔐 인증번호 확인
========================= */
function verifyPhoneAuth() {
    window.firebaseInitPromise
        .then(async () => {
            const code = document.getElementById('authCode').value;

            if (!code || code.length !== 6) {
                showAuthMessage('인증번호 6자리를 입력해주세요', 'error');
                return;
            }

            if (!confirmationResult) {
                showAuthMessage('먼저 인증번호를 요청해주세요', 'error');
                return;
            }

            console.log('🔐 인증번호 확인:', code);

            await confirmationResult.confirm(code);

            console.log('✅ 전화번호 인증 성공!');

            phoneAuthVerified = true;
            showAuthMessage('✓ 전화번호 인증이 완료되었습니다', 'success');

            document.getElementById('phone').disabled = true;
            document.getElementById('sendAuthBtn').disabled = true;
            document.getElementById('authCode').disabled = true;
            document.querySelector('#authCodeSection button').disabled = true;

            if (authTimer) clearInterval(authTimer);
        })
        .catch(error => {
            console.error('❌ 인증 실패:', error);
            showAuthMessage('인증번호가 올바르지 않거나 만료되었습니다.', 'error');
            document.getElementById('authCode').value = '';
        });
}

/* =========================
   ✅ 프로필 저장 전 필수 입력 검증
   - 전화번호 인증은 현재 필수 아님
========================= */
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('profileForm');
    if (!form) return;

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const requiredFields = [
            { id: 'firstName', name: '이름' },
            { id: 'nickname', name: '닉네임' },
            { id: 'university', name: '대학교' },
            { id: 'major', name: '전공' },
            { id: 'entranceYear', name: '입학년도' },
            { id: 'graduationYear', name: '졸업년도' },
            { id: 'pricePerHour', name: '시간당 수업료' }
        ];

        for (const field of requiredFields) {
            const el = document.getElementById(field.id);
            if (!el || !el.value.trim()) {
                alert(`❗ ${field.name}을(를) 입력해주세요.`);
                el?.focus();
                return;
            }
        }

        const allSubjects = document.querySelectorAll(
            '.checkbox-group input[type="checkbox"][name="subjects"]'
        );
        const checkedSubjects = Array.from(allSubjects).filter((cb) => cb.checked);

        if (checkedSubjects.length === 0) {
            alert('❗ 최소 1개 이상의 과목을 선택해주세요.');
            document.getElementById('teaching').scrollIntoView({ behavior: 'smooth' });
            return;
        }

        console.log('✅ 선택된 과목:', checkedSubjects.map(cb => cb.value));

        /*
        const gradesChecked = document.querySelectorAll(
            '#teaching input[type="checkbox"][id^="grade"]:checked'
        );
        if (gradesChecked.length === 0) {
            alert('❗ 수업 대상 학년을 최소 1개 선택해주세요.');
            return;
        }
        */

        const lessonType = document.querySelector('input[name="lessonType"]:checked');
        if (!lessonType) {
            alert('❗ 수업 방식을 선택해주세요.');
            return;
        }

        /*
        // 🔒 전화번호 인증을 다시 필수로 만들 경우 사용
        if (!phoneAuthVerified) {
            alert('❗ 전화번호 인증을 완료해주세요.');
            return;
        }
        */

        // ✅ 전화번호 인증 여부는 검사하지 않음
        try {
            const formData = new FormData(form);

            console.log('📤 프로필 저장 중...');
            console.log('📋 선택된 과목:', checkedSubjects.map(cb => cb.value));

            const res = await fetch(form.action, {
                method: 'POST',
                body: formData
            });

            const data = await res.json();

            alert(data.message || '✅ 프로필이 저장되었습니다.');

            // ✅ 저장된 값이 반영된 멘토 페이지 다시 로드
            location.reload();

        } catch (err) {
            console.error('❌ 프로필 저장 실패:', err);
            alert('프로필 저장 중 오류가 발생했습니다.');
        }
    });
});

// 전역 함수로 노출
window.debugSubjects = {
    validate: function() {
        const allSubjects = document.querySelectorAll('.checkbox-group input[type="checkbox"][name="subjects"]');
        const checkedSubjects = Array.from(allSubjects).filter((cb) => cb.checked);
        return checkedSubjects.length > 0;
    },
    log: function() {
        const allSubjects = document.querySelectorAll('.checkbox-group input[type="checkbox"][name="subjects"]:checked');
        const selectedSubjects = Array.from(allSubjects).map((cb) => cb.value);
        console.log('📝 선택된 과목:', selectedSubjects);
        return selectedSubjects;
    },
    init: function() {
        console.log('🚀 토글 시스템 재초기화');
        initializeCollapsibleSubjects();
    }
};

console.log('🎯 디버깅 명령어: debugSubjects.validate(), debugSubjects.log(), debugSubjects.init()');