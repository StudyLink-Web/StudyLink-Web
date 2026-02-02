console.log('📋 mentor-profile.js 로드됨');

/* =========================
   📷 프로필 사진 미리보기
========================= */
console.log('📷 프로필 사진 미리보기 시스템 로드됨');

document.addEventListener('DOMContentLoaded', () => {
    const avatarUpload = document.getElementById('avatarUpload');
    const avatarPreview = document.getElementById('avatarPreview');

    if (!avatarUpload) {
        console.warn('⚠️ 사진 업로드 필드를 찾을 수 없습니다');
        return;
    }

    // 📷 파일 선택 시 즉시 미리보기
    avatarUpload.addEventListener('change', (e) => {
        const file = e.target.files[0];

        if (!file) {
            console.log('❌ 파일이 선택되지 않음');
            return;
        }

        // 파일 형식 검증
        if (!file.type.startsWith('image/')) {
            alert('⚠️ 이미지 파일만 업로드 가능합니다');
            avatarUpload.value = ''; // 입력값 초기화
            return;
        }

        // 파일 크기 검증 (10MB 제한)
        const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        if (file.size > MAX_FILE_SIZE) {
            alert('⚠️ 파일 크기는 10MB 이하여야 합니다');
            avatarUpload.value = '';
            return;
        }

        // FileReader를 사용한 즉시 미리보기
        const reader = new FileReader();

        reader.onload = (event) => {
            const imageUrl = event.target.result;
            avatarPreview.src = imageUrl;
            console.log('✅ 프로필 사진 미리보기 완료');
        };

        reader.onerror = () => {
            console.error('❌ 파일 읽기 실패');
            alert('파일을 읽을 수 없습니다. 다시 시도해주세요.');
        };

        reader.readAsDataURL(file);
    });
});


// ⭐ 즉시 실행
console.log('🚀 탭 시스템 즉시 초기화');

let tabButtons = document.querySelectorAll('.tab-btn');
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
// 🔄 토글 라벨 시스템 초기화 (새로운 구조)
// ================================================

console.log('📚 토글 라벨 시스템 로드됨');

document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 토글 라벨 시스템 초기화 시작');
    initializeCollapsibleSubjects();
});

function initializeCollapsibleSubjects() {
    const mainSubjectLabels = document.querySelectorAll('.main-subject-label[data-toggle]');

    console.log('📋 메인 과목 라벨 개수:', mainSubjectLabels.length);

    mainSubjectLabels.forEach((label) => {
        const toggleId = label.getAttribute('data-toggle');
        const contentDiv = document.getElementById(toggleId);

        if (!contentDiv) {
            console.warn(`⚠️ 토글 컨테이너를 찾을 수 없음: ${toggleId}`);
            return;
        }

        console.log(`✅ 토글 과목 라벨 등록: ${toggleId}`);

        label.addEventListener('click', (e) => {
            e.preventDefault();
            toggleSubjectContent(contentDiv, label);
            console.log(`🔄 ${toggleId} 토글됨`);
        });

        // 초기 상태: 세부과목에 체크된 항목이 있으면 펼침
        const subCheckboxes = contentDiv.querySelectorAll('.checkbox-input');
        const hasCheckedSubitem = Array.from(subCheckboxes).some(cb => cb.checked);

        if (hasCheckedSubitem) {
            contentDiv.classList.add('open');
            label.classList.add('active');
            console.log(`📂 초기 상태: ${toggleId} 표시됨`);
        }
    });

    // 세부과목 체크박스 동기화
    initializeSubitemCheckboxes();

    console.log('✅ 토글 라벨 시스템 준비 완료');
}

function toggleSubjectContent(contentDiv, label) {
    contentDiv.classList.toggle('open');
    label.classList.toggle('active');
}

function initializeSubitemCheckboxes() {
    const allSubCheckboxes = document.querySelectorAll('.collapsible-content .checkbox-input');

    console.log(`🔗 세부과목 체크박스 총 개수: ${allSubCheckboxes.length}`);

    allSubCheckboxes.forEach((subCheckbox) => {
        subCheckbox.addEventListener('change', () => {
            console.log(`🔄 세부과목 변경: ${subCheckbox.value}`);
        });
    });
}

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
let authTimer = null;
let authTimeRemaining = 300;

function startAuthTimer() {
    authTimeRemaining = 300; // 5분
    const timerEl = document.querySelector('#authTimer span');

    if (authTimer) clearInterval(authTimer);

    authTimer = setInterval(() => {
        authTimeRemaining--;

        const min = String(Math.floor(authTimeRemaining / 60)).padStart(2, '0');
        const sec = String(authTimeRemaining % 60).padStart(2, '0');

        if (timerEl) {
            timerEl.textContent = `${min}:${sec}`;
        }

        // ⏰ 인증 시간 만료
        if (authTimeRemaining <= 0) {
            clearInterval(authTimer);
            authTimer = null;

            showAuthMessage('인증 시간이 만료되었습니다. 인증번호를 다시 받아주세요.', 'error');

            const sendBtn = document.getElementById('sendAuthBtn');

            // ✅ 재전송 쿨다운이 끝났을 때만 버튼 활성화
            if (sendBtn && typeof resendRemaining !== 'undefined' && resendRemaining <= 0) {
                sendBtn.disabled = false;
                sendBtn.textContent = '인증번호 재전송';
            }
        }
    }, 1000);
}

/* =========================
   🔐 전화번호 인증 상태
========================= */
let phoneAuthVerified = false;

/* =========================
   🔁 재전송 쿨다운(버튼 연타 방지)
========================= */
let resendInterval = null;
let resendRemaining = 0;

function startResendCooldown(seconds) {
    const sendBtn = document.getElementById('sendAuthBtn');
    if (!sendBtn) return;

    if (resendInterval) clearInterval(resendInterval);

    resendRemaining = seconds;
    sendBtn.disabled = true;
    sendBtn.textContent = `재전송 (${resendRemaining}s)`;

    resendInterval = setInterval(() => {
        resendRemaining--;

        if (resendRemaining <= 0) {
            clearInterval(resendInterval);
            resendInterval = null;
            sendBtn.disabled = false;
            sendBtn.textContent = '인증번호 재전송';
            return;
        }
        sendBtn.textContent = `재전송 (${resendRemaining}s)`;
    }, 1000);
}


/* =========================
   📱 인증번호 요청
========================= */
async function requestPhoneAuth() {

    if (phoneAuthVerified) {
        showAuthMessage('이미 전화번호 인증이 완료되었습니다.', 'success');
        return;
    }

    const phoneInput = document.getElementById('phone');
    const rawPhone = phoneInput.value.replace(/\D/g, '');

    if (rawPhone.length !== 11) {
        showAuthMessage('전화번호를 정확히 입력해주세요.', 'error');
        return;
    }

    const phoneNumber = '+82' + rawPhone.slice(1);
    console.log('📱 전화번호 인증 요청:', phoneNumber);

    const sendBtn = document.getElementById('sendAuthBtn');

    // ✅ 쿨다운 중이면 무시
    if (sendBtn.disabled && resendRemaining > 0) {
        showAuthMessage(`잠시만요! ${resendRemaining}초 후 재전송할 수 있어요.`, 'error');
        return;
    }

    sendBtn.disabled = true;
    sendBtn.textContent = '발송 중...';

    try {
        await window.sendFirebasePhoneCode(phoneNumber);

        document.getElementById('authCodeSection').style.display = 'block';
        showAuthMessage('인증번호가 발송되었습니다.', 'success');

        startAuthTimer();

        // ✅ 정상 발송 후 최소 60초는 재전송 막기
        startResendCooldown(60);

    } catch (error) {
        console.error(error);

        if (error?.code === 'auth/too-many-requests') {
            showAuthMessage('요청이 너무 많아 잠시 차단되었습니다. 5분 후 다시 시도해주세요.', 'error');
            startResendCooldown(300);
        } else {
            showAuthMessage('인증번호 발송에 실패했습니다. 잠시 후 다시 시도해주세요.', 'error');
            startResendCooldown(60);
        }
    }
}


/* =========================
   🔐 인증번호 확인 (중복 클릭 방지 강화)
========================= */
let isVerifyingPhoneCode = false;

async function verifyPhoneAuth() {
    // ✅ 이미 인증 완료면 더 이상 실행 안 함
    if (phoneAuthVerified) {
        showAuthMessage('이미 전화번호 인증이 완료되었습니다.', 'success');
        return;
    }

    // ✅ 인증 처리 중 연타 방지
    if (isVerifyingPhoneCode) return;

    const codeEl = document.getElementById('authCode');
    const verifyBtn = document.getElementById('verifyAuthBtn');
    const sendBtn = document.getElementById('sendAuthBtn');
    const phoneEl = document.getElementById('phone');
    const authSection = document.getElementById('authCodeSection');
    const phoneVerifiedEl = document.getElementById('phoneVerified');

    const code = (codeEl?.value || '').trim();

    if (code.length !== 6) {
        showAuthMessage('인증번호 6자리를 입력해주세요.', 'error');
        return;
    }

    try {
        isVerifyingPhoneCode = true;

        // UI 잠금 (연타 방지)
        if (verifyBtn) {
            verifyBtn.disabled = true;
            verifyBtn.textContent = '확인 중...';
        }

        const result = await window.verifyFirebasePhoneCode(code);

        if (result?.success) {
            phoneAuthVerified = true;
            showAuthMessage('전화번호 인증이 완료되었습니다. 저장하기를 눌러야 최종 저장이 됩니다', 'success');

            // ✅ (안전) 필요한 엘리먼트 다시 조회 (변수 미선언으로 인한 에러 방지)
            const phoneEl = document.getElementById('phone');
            const phoneVerifiedEl = document.getElementById('phoneVerified');
            const codeEl = document.getElementById('authCode');
            const verifyBtn = document.getElementById('verifyAuthBtn');
            const sendBtn = document.getElementById('sendAuthBtn');
            const authSection = document.getElementById('authCodeSection');

            // ✅ 서버로 "인증완료" 값을 같이 보내기 (hidden input)
            if (phoneVerifiedEl) phoneVerifiedEl.value = 'true';

            // ✅ phone은 disabled ❌ / readOnly ✅ (FormData에 포함되게)
            if (phoneEl) phoneEl.readOnly = true;

            // ✅ 인증번호 입력칸 잠금
            if (codeEl) codeEl.disabled = true;

            // ✅ 인증하기 버튼: 숨김(연타 방지 + UX 깔끔)
            if (verifyBtn) {
                verifyBtn.disabled = true;
                verifyBtn.textContent = '인증 완료';
                verifyBtn.style.display = 'none';
            }

            // ✅ 인증번호 받기 버튼도 더 이상 필요 없으면 숨김/잠금
            if (sendBtn) {
                sendBtn.disabled = true;
                sendBtn.style.display = 'none';
            }

            // ✅ 인증 영역 전체를 접고 싶으면(선택) - 기본은 유지
            // if (authSection) authSection.style.display = 'none';

            // ✅ 타이머 정리 (네 원래 기능 유지)
            if (authTimer) {
                clearInterval(authTimer);
                authTimer = null;
            }

    } else {
            showAuthMessage('인증번호가 올바르지 않습니다.', 'error');

            // 실패면 다시 입력/시도 가능하게 복구
            if (verifyBtn) {
                verifyBtn.disabled = false;
                verifyBtn.textContent = '인증하기';
            }
        }

    } catch (error) {
        console.error('❌ 인증 실패:', error);

        // Firebase 에러별 메시지
        if (error?.code === 'auth/code-expired') {
            showAuthMessage('인증번호가 만료되었습니다. 인증번호를 다시 받아주세요.', 'error');
        } else if (error?.code === 'auth/invalid-verification-code') {
            showAuthMessage('인증번호가 올바르지 않습니다.', 'error');
        } else {
            showAuthMessage('인증에 실패했습니다. 잠시 후 다시 시도해주세요.', 'error');
        }

        // 에러면 다시 시도 가능하게 복구
        if (!phoneAuthVerified && verifyBtn) {
            verifyBtn.disabled = false;
            verifyBtn.textContent = '인증하기';
        }

    } finally {
        isVerifyingPhoneCode = false;
    }
}


/* =========================
  새로고침/재접속 시 DB 번호가 있으면 인증 UI가 “자동으로 막힘 + 안내문”
========================= */

document.addEventListener('DOMContentLoaded', () => {
    const phoneEl = document.getElementById('phone');
    if (!phoneEl) return;

    const hasPhoneFromDB = (phoneEl.value || '').trim().length > 0;

    const sendBtn = document.getElementById('sendAuthBtn');
    const verifyBtn = document.getElementById('verifyAuthBtn');
    const authSection = document.getElementById('authCodeSection');
    const recaptcha = document.getElementById('recaptcha-container');

    // ✅ 안내문을 "전화번호 입력 영역 아래"에 달기 위한 기준점(가까운 form-group)
    const phoneFormGroup = phoneEl.closest('.form-group');

    // 안내문 엘리먼트 생성/재사용
    const hintId = 'phoneSettingHint';
    let hintEl = document.getElementById(hintId);

    function showSettingHint() {
        if (!phoneFormGroup) return;

        if (!hintEl) {
            hintEl = document.createElement('div');
            hintEl.id = hintId;
            hintEl.className = 'form-hint';
            hintEl.textContent = '전화번호는 환경설정에서 변경할 수 있습니다';
            // 전화번호 입력칸 바로 아래에 붙이기
            phoneFormGroup.appendChild(hintEl);
        } else {
            hintEl.style.display = 'block';
        }
    }

    function hideSettingHint() {
        if (hintEl) hintEl.style.display = 'none';
    }

    if (hasPhoneFromDB) {
        // ✅ DB에 전화번호 있으면: 여기선 변경 불가 UX
        phoneEl.readOnly = true;     // disabled ❌ / readOnly ✅ (값 전송 유지)

        // 인증 UI 전부 숨김
        if (sendBtn) sendBtn.style.display = 'none';
        if (verifyBtn) verifyBtn.style.display = 'none';
        if (authSection) authSection.style.display = 'none';
        if (recaptcha) recaptcha.style.display = 'none';

        // 안내문 표시
        showSettingHint();

    } else {
        // ✅ DB에 번호 없으면: 인증 UI 사용 가능
        phoneEl.readOnly = false;

        if (sendBtn) sendBtn.style.display = '';
        // verifyBtn은 authSection 안에 있으니 authSection이 열릴 때 보이게 됨
        if (recaptcha) recaptcha.style.display = '';

        hideSettingHint();
    }
});

document.addEventListener('DOMContentLoaded', () => {
    const phoneEl = document.getElementById('phone');
    if (!phoneEl) return;

    const hasPhoneFromDB = (phoneEl.value || '').trim().length > 0;

    const sendBtn = document.getElementById('sendAuthBtn');
    const verifyBtn = document.getElementById('verifyAuthBtn');
    const authSection = document.getElementById('authCodeSection');
    const recaptcha = document.getElementById('recaptcha-container');

    const phoneFormGroup = phoneEl.closest('.form-group');

    // 안내문 생성/재사용
    const hintId = 'phoneSettingHint';
    let hintEl = document.getElementById(hintId);

    const showSettingHint = () => {
        if (!phoneFormGroup) return;

        if (!hintEl) {
            hintEl = document.createElement('div');
            hintEl.id = hintId;
            hintEl.className = 'form-hint';
            hintEl.textContent = '전화번호는 환경설정에서 변경할 수 있습니다';
            phoneFormGroup.appendChild(hintEl);
        } else {
            hintEl.style.display = 'block';
        }
    };

    const hideSettingHint = () => {
        if (hintEl) hintEl.style.display = 'none';
    };

    if (hasPhoneFromDB) {
        // ✅ DB 값이 있으면: 이 페이지에서는 변경/인증 불가
        phoneEl.readOnly = true; // disabled ❌

        if (sendBtn) sendBtn.style.display = 'none';
        if (verifyBtn) verifyBtn.style.display = 'none';
        if (authSection) authSection.style.display = 'none';
        if (recaptcha) recaptcha.style.display = 'none';

        // 인증상태도 "이미 완료"처럼 처리(UX용)
        phoneAuthVerified = true;

        showSettingHint();
    } else {
        // ✅ DB 값이 없으면: 인증 가능
        phoneEl.readOnly = false;

        if (sendBtn) sendBtn.style.display = '';
        if (recaptcha) recaptcha.style.display = '';

        hideSettingHint();
    }
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


/* =========================
   ✅ 저장하기: 기본 submit 막고(fetch) 저장 후 새로고침
========================= */
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('profileForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();      // ✅ 페이지 이동 막기 (핵심)
        e.stopPropagation();

        // (선택) 버튼 연타 방지
        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = '저장 중...';
        }

        try {
            const formData = new FormData(form);

            const res = await fetch(form.action, {
                method: 'POST',
                body: formData,
                headers: {
                    'Accept': 'application/json',
                    'X-Requested-With': 'fetch'
                }
            });

            // 서버가 JSON이 아니면 에러 처리
            const contentType = res.headers.get('content-type') || '';
            if (!contentType.includes('application/json')) {
                const text = await res.text();
                console.error('❌ JSON 아님, 서버 응답:', text);
                alert('서버 응답이 올바르지 않습니다.');
                return;
            }

            const data = await res.json();

            alert(data.message || '✅ 프로필이 저장되었습니다.');

            // ✅ 저장된 값 화면에 반영
            location.reload();

        } catch (err) {
            console.error('❌ 프로필 저장 실패:', err);
            alert('프로필 저장 중 오류가 발생했습니다.');
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = '✓ 저장하기';
            }
        }
    });
});
