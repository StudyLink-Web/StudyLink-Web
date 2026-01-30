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
            showAuthMessage('인증 시간이 만료되었습니다.', 'error');
        }
    }, 1000);
}

/* =========================
   🔐 전화번호 인증 상태
========================= */
let phoneAuthVerified = false;

/* =========================
   📱 인증번호 요청
========================= */
async function requestPhoneAuth() {
    const phoneInput = document.getElementById('phone');
    const rawPhone = phoneInput.value.replace(/\D/g, '');

    if (rawPhone.length !== 11) {
        showAuthMessage('전화번호를 정확히 입력해주세요.', 'error');
        return;
    }

    const phoneNumber = '+82' + rawPhone.slice(1);
    console.log('📱 전화번호 인증 요청:', phoneNumber);

    const sendBtn = document.getElementById('sendAuthBtn');
    sendBtn.disabled = true;
    sendBtn.textContent = '발송 중...';

    try {
        await window.sendFirebasePhoneCode(phoneNumber);
        document.getElementById('authCodeSection').style.display = 'block';
        showAuthMessage('인증번호가 발송되었습니다.', 'success');
        startAuthTimer();
    } catch (error) {
        console.error(error);
        showAuthMessage('인증번호 발송에 실패했습니다.', 'error');
    } finally {
        sendBtn.disabled = false;
        sendBtn.textContent = '인증번호 재전송';
    }
}

/* =========================
   🔐 인증번호 확인
========================= */
async function verifyPhoneAuth() {
    const code = document.getElementById('authCode').value;

    if (code.length !== 6) {
        showAuthMessage('인증번호 6자리를 입력해주세요.', 'error');
        return;
    }

    const result = await window.verifyFirebasePhoneCode(code);

    if (result.success) {
        phoneAuthVerified = true;
        showAuthMessage('전화번호 인증이 완료되었습니다.', 'success');

        document.getElementById('phone').disabled = true;
        document.getElementById('sendAuthBtn').disabled = true;
        document.getElementById('authCode').disabled = true;

        clearInterval(authTimer);
    } else {
        showAuthMessage('인증번호가 올바르지 않습니다.', 'error');
    }
}

window.requestPhoneAuth = requestPhoneAuth;
window.verifyPhoneAuth = verifyPhoneAuth;


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

            const contentType = res.headers.get('content-type');

            if (!contentType || !contentType.includes('application/json')) {
                const text = await res.text();
                console.error('❌ JSON 아님, 서버 응답:', text);
                throw new Error('서버가 JSON이 아닌 응답을 반환했습니다.');
            }

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