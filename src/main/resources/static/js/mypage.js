/**
 * mypage.js - 마이페이지 JavaScript
 * 마이페이지의 모든 기능을 관리하는 메인 스크립트
 */
console.log('🔥 mypage.js 로드됨 - PW_FIX_VERSION_001');

// ========== 전역 설정 ==========

const API_BASE = '/api';
const TOAST_DURATION = 3000; // 3초

// ========== 초기화 ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ 마이페이지 초기화 시작');

    initializeEventListeners();
    loadInitialData();
    initPasswordRulesLive_B();
});

// ========== 이벤트 리스너 초기화 ==========

function initializeEventListeners() {
    console.log('🔍 이벤트 리스너 초기화 시작');

    // 탭 네비게이션 (네비게이션 링크만 선택)
    const tabLinks = document.querySelectorAll('.nav-link[data-tab]:not(.logout-btn)');
    console.log(`📍 찾은 탭 링크 개수: ${tabLinks.length}`);

    tabLinks.forEach(link => {
        const tabName = link.getAttribute('data-tab');
        console.log(`📌 탭 링크 바인딩: ${tabName}`);

        link.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const clickedTab = this.getAttribute('data-tab');
            console.log(`🔄 클릭된 탭: ${clickedTab}`);

            switchTab(clickedTab);
        });
    });



    // 프로필 탭
    document.getElementById('basic-info-form')?.addEventListener('submit', handleBasicInfoSubmit);
    document.getElementById('upload-profile-btn')?.addEventListener('click', handleUploadProfileClick);
    document.getElementById('delete-profile-btn')?.addEventListener('click', handleDeleteProfile);
    document.getElementById('profile-image-input')?.addEventListener('change', handleProfileImageChange);
    document.getElementById('check-nickname-btn')?.addEventListener('click', handleCheckNickname);

    // 계정 탭
    // ========== 비밀번호 변경 submit (이벤트 위임) ==========
    document.addEventListener('submit', function (e) {
        const form = e.target;

        if (form && form.id === 'change-password-form') {
            handleChangePassword(e);
        }
    });
    document.getElementById('change-email-form')?.addEventListener('submit', handleChangeEmail);
    document.getElementById('change-phone-form')?.addEventListener('submit', handleChangePhone);
    document.getElementById('delete-account-btn')?.addEventListener('click', handleDeleteAccountClick);
    document.getElementById('confirm-delete-btn')?.addEventListener('click', handleConfirmDelete);

    // 알림 설정 탭
    document.getElementById('notifications-enabled')?.addEventListener('change', handleNotificationsToggle);
    document.getElementById('email-notifications')?.addEventListener('change', handleEmailNotifications);
    document.getElementById('push-notifications')?.addEventListener('change', handlePushNotifications);
    document.getElementById('sms-notifications')?.addEventListener('change', handleSmsNotifications);
    document.querySelectorAll('.notification-type')?.forEach(checkbox => {
        checkbox.addEventListener('change', handleNotificationType);
    });

    // 설정 탭
    document.querySelectorAll('input[name="theme"]').forEach(radio => {
        radio.addEventListener('change', handleThemeChange);
    });
    document.querySelectorAll('input[name="language"]').forEach(radio => {
        radio.addEventListener('change', handleLanguageChange);
    });
    document.getElementById('profile-public')?.addEventListener('change', handleProfileVisibility);
    document.getElementById('privacy-policy-agree')?.addEventListener('change', handlePrivacyPolicyAgree);
    document.getElementById('terms-agree')?.addEventListener('change', handleTermsAgree);
    document.getElementById('marketing-agree')?.addEventListener('change', handleMarketingAgree);
    document.getElementById('reset-settings-btn')?.addEventListener('click', handleResetSettings);
}

/**
 * ⭐ 수정: 탭 전환 통합 함수
 */
function switchTab(tabName) {
    if (!tabName) return;

    console.log(`🔄 탭 전환: ${tabName}`);

    // 1️⃣ 모든 탭 콘텐츠 숨기기
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    // 2️⃣ 모든 네비게이션 링크 비활성화
    document.querySelectorAll('.nav-link[data-tab]').forEach(link => {
        link.classList.remove('active');
    });

    // 3️⃣ 해당 탭 콘텐츠 활성화
    const tabContent = document.querySelector(`#${tabName}-tab`);
    if (tabContent) {
        tabContent.classList.add('active');
        console.log(`✅ 탭 콘텐츠 활성화: #${tabName}-tab`);
    } else {
        console.error(`❌ 탭 콘텐츠를 찾을 수 없습니다: #${tabName}-tab`);
        return;
    }

    // 4️⃣ 해당 네비게이션 링크 활성화
    const navLink = document.querySelector(`.nav-link[data-tab="${tabName}"]`);
    if (navLink) {
        navLink.classList.add('active');
        console.log(`✅ 탭 링크 활성화: ${tabName}`);
    } else {
        console.error(`❌ 탭 링크를 찾을 수 없습니다: [data-tab="${tabName}"]`);
    }
}

// ========== 초기 데이터 로드 ==========

function loadInitialData() {
    console.log('📂 초기 데이터 로드');

    // 첫 번째 탭 활성화
    switchTab('account');
}


// ========== 계정 탭 함수 ==========

/**
 * 비밀번호 변경
 */
function handleChangePassword(e) {
    console.log('🔥 비밀번호 변경 submit 발생');
    e.preventDefault();

    const currentPassword = document.getElementById('current-password').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    // 검증
    if (!currentPassword || !newPassword || !confirmPassword) {
        const currentHint = document.getElementById('current-password-hint');
        const confirmHint = document.getElementById('confirm-password-hint');

        if (!currentPassword && currentHint) {
            setHint(currentHint, '현재 비밀번호를 입력해 주세요.', 'error');
        }

        if ((!confirmPassword || newPassword !== confirmPassword) && confirmHint) {
            setHint(confirmHint, '새 비밀번호를 한 번 더 정확히 입력해 주세요.', 'error');
        }


        return;
    }

    if (newPassword.length < 8) {
        showToast('새 비밀번호는 8자 이상이어야 합니다', 'warning');
        return;
    }

    if (newPassword !== confirmPassword) {
        const confirmHint = document.getElementById('confirm-password-hint');
        if (confirmHint) {
            setHint(confirmHint, '새 비밀번호가 일치하지 않습니다.', 'error');
        }
        return;
    }


    showLoading();

    fetch(`${API_BASE}/account/change-password`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            currentPassword,
            newPassword,
            confirmPassword
        })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                document.getElementById('change-password-form').reset();

                // reset 후 안내 초기화
                document.getElementById('current-password-hint').textContent = '';
                document.getElementById('confirm-password-hint').textContent = '';
                document.getElementById('pw-strength').textContent = '';
                document.querySelectorAll('#pw-rules li').forEach(li => li.classList.remove('ok'));

                showToast('비밀번호가 변경되었습니다', 'success');
            } else {
                // ✅ 서버 메시지를 안내문에도 출력
                const hint = document.getElementById('current-password-hint');
                if (hint) {
                    setHint(hint, data.message || '변경 실패', 'error');
                }
                showToast(data.message || '변경 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 비밀번호 변경 오류:', error);
            showToast('변경 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 이메일 변경
 */
function handleChangeEmail(e) {
    e.preventDefault();

    const newEmail = document.getElementById('new-email').value.trim();
    const password = document.getElementById('email-password').value;

    // 검증
    if (!newEmail || !password) {
        showToast('모든 항목을 입력하세요', 'warning');
        return;
    }

    if (!isValidEmail(newEmail)) {
        showToast('올바른 이메일 형식이 아닙니다', 'warning');
        return;
    }

    showLoading();

    fetch(`${API_BASE}/account/change-email`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            newEmail,
            password
        })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                document.getElementById('change-email-form').reset();
                showToast('이메일이 변경되었습니다. 새 이메일로 확인 메시지가 발송되었습니다', 'success');
            } else {
                showToast(data.message || '변경 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 이메일 변경 오류:', error);
            showToast('변경 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 휴대폰 번호 변경
 */
function handleChangePhone(e) {
    e.preventDefault();

    const newPhone = document.getElementById('new-phone').value.trim();
    const password = document.getElementById('phone-password').value;

    // 검증
    if (!newPhone || !password) {
        showToast('모든 항목을 입력하세요', 'warning');
        return;
    }

    if (!isValidPhone(newPhone)) {
        showToast('올바른 휴대폰 번호 형식이 아닙니다', 'warning');
        return;
    }

    showLoading();

    fetch(`${API_BASE}/account/change-phone`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            newPhone,
            password
        })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                document.getElementById('change-phone-form').reset();
                showToast('휴대폰 번호가 변경되었습니다', 'success');
            } else {
                showToast(data.message || '변경 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 휴대폰 번호 변경 오류:', error);
            showToast('변경 중 오류가 발생했습니다', 'error');
        });
}



/**
 * 계정 삭제 클릭
 */
function handleDeleteAccountClick() {
    $('#deleteAccountModal').modal('show');
}

/**
 * 계정 삭제 확인
 */
function handleConfirmDelete() {
    const password = document.getElementById('delete-account-password').value;

    if (!password) {
        showToast('비밀번호를 입력하세요', 'warning');
        return;
    }

    showLoading();

    fetch(`${API_BASE}/account`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ password })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast('계정이 삭제되었습니다. 로그아웃됩니다', 'success');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 2000);
            } else {
                showToast(data.message || '삭제 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 계정 삭제 오류:', error);
            showToast('삭제 중 오류가 발생했습니다', 'error');
        });
}

// ========== 알림 설정 탭 함수 ==========

/**
 * 전체 알림 토글
 */
function handleNotificationsToggle(e) {
    const enabled = e.target.checked;

    showLoading();

    fetch(`${API_BASE}/settings/notifications`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ enabled })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast(enabled ? '알림이 활성화되었습니다' : '알림이 비활성화되었습니다', 'success');
            } else {
                showToast(data.message || '설정 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 알림 토글 오류:', error);
            showToast('설정 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 이메일 알림 설정
 */
function handleEmailNotifications(e) {
    const enabled = e.target.checked;

    showLoading();

    fetch(`${API_BASE}/settings/email-notifications`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ enabled })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast(enabled ? '이메일 알림이 활성화되었습니다' : '이메일 알림이 비활성화되었습니다', 'success');
            } else {
                showToast(data.message || '설정 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 이메일 알림 설정 오류:', error);
            showToast('설정 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 푸시 알림 설정
 */
function handlePushNotifications(e) {
    const enabled = e.target.checked;

    showLoading();

    fetch(`${API_BASE}/settings/push-notifications`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ enabled })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast(enabled ? '푸시 알림이 활성화되었습니다' : '푸시 알림이 비활성화되었습니다', 'success');
            } else {
                showToast(data.message || '설정 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 푸시 알림 설정 오류:', error);
            showToast('설정 중 오류가 발생했습니다', 'error');
        });
}

/**
 * SMS 알림 설정
 */
function handleSmsNotifications(e) {
    const enabled = e.target.checked;

    showLoading();

    fetch(`${API_BASE}/settings/sms-notifications`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ enabled })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast(enabled ? 'SMS 알림이 활성화되었습니다' : 'SMS 알림이 비활성화되었습니다', 'success');
            } else {
                showToast(data.message || '설정 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ SMS 알림 설정 오류:', error);
            showToast('설정 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 알림 유형별 설정
 */
function handleNotificationType(e) {
    const notificationType = e.target.getAttribute('data-type');
    const enabled = e.target.checked;

    showLoading();

    fetch(`${API_BASE}/settings/notification-type`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            notificationType,
            enabled
        })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast('알림 유형이 설정되었습니다', 'success');
            } else {
                showToast(data.message || '설정 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 알림 유형 설정 오류:', error);
            showToast('설정 중 오류가 발생했습니다', 'error');
        });
}

// ========== 설정 탭 함수 ==========

/**
 * 테마 변경
 */
function handleThemeChange(e) {
    const themeMode = e.target.value;

    showLoading();

    fetch(`${API_BASE}/settings/theme`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ themeMode })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast(`테마가 ${themeMode === 'DARK' ? '다크 모드' : '라이트 모드'}로 변경되었습니다`, 'success');
                // 실제 테마 적용 (선택사항)
                applyTheme(themeMode);
            } else {
                showToast(data.message || '변경 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 테마 변경 오류:', error);
            showToast('변경 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 언어 변경
 */
function handleLanguageChange(e) {
    const language = e.target.value;

    showLoading();

    fetch(`${API_BASE}/settings/language`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ language })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast('언어가 변경되었습니다', 'success');
                // 페이지 새로고침 (선택사항)
                // location.reload();
            } else {
                showToast(data.message || '변경 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 언어 변경 오류:', error);
            showToast('변경 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 프로필 공개 설정
 */
function handleProfileVisibility(e) {
    const isPublic = e.target.checked;

    showLoading();

    fetch(`${API_BASE}/settings/profile-visibility`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ isPublic })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast(isPublic ? '프로필이 공개되었습니다' : '프로필이 비공개되었습니다', 'success');
            } else {
                showToast(data.message || '설정 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 프로필 공개 설정 오류:', error);
            showToast('설정 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 개인정보 처리방침 동의
 */
function handlePrivacyPolicyAgree(e) {
    const agree = e.target.checked;

    showLoading();

    fetch(`${API_BASE}/settings/privacy-policy`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ agree })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast(agree ? '개인정보 처리방침에 동의했습니다' : '개인정보 처리방침 동의를 철회했습니다', 'success');
            } else {
                showToast(data.message || '설정 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 개인정보 처리방침 동의 오류:', error);
            showToast('설정 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 서비스 이용약관 동의
 */
function handleTermsAgree(e) {
    const agree = e.target.checked;

    showLoading();

    fetch(`${API_BASE}/settings/terms-of-service`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ agree })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast(agree ? '서비스 이용약관에 동의했습니다' : '서비스 이용약관 동의를 철회했습니다', 'success');
            } else {
                showToast(data.message || '설정 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 서비스 이용약관 동의 오류:', error);
            showToast('설정 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 마케팅 정보 수신 동의
 */
function handleMarketingAgree(e) {
    const agree = e.target.checked;

    showLoading();

    fetch(`${API_BASE}/settings/marketing`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ agree })
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast(agree ? '마케팅 정보 수신에 동의했습니다' : '마케팅 정보 수신 동의를 철회했습니다', 'success');
            } else {
                showToast(data.message || '설정 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 마케팅 정보 수신 동의 오류:', error);
            showToast('설정 중 오류가 발생했습니다', 'error');
        });
}

/**
 * 설정 초기화
 */
function handleResetSettings() {
    if (!confirm('모든 설정을 기본값으로 복구하시겠습니까?')) return;

    showLoading();

    fetch(`${API_BASE}/settings/reset`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.success) {
                showToast('설정이 초기화되었습니다. 페이지를 새로고침합니다', 'success');
                setTimeout(() => {
                    location.reload();
                }, 2000);
            } else {
                showToast(data.message || '초기화 실패', 'error');
            }
        })
        .catch(error => {
            hideLoading();
            console.error('❌ 설정 초기화 오류:', error);
            showToast('초기화 중 오류가 발생했습니다', 'error');
        });
}

// ========== 유틸리티 함수 ==========

function setHint(el, message = '', state = '') {
    if (!el) return;

    if (!message) {
        // 메시지 없으면 완전 숨김
        el.className = 'field-hint';
        el.textContent = '';
        return;
    }

    // 메시지 있으면 노출 + 상태 반영
    el.className = `field-hint has-message ${state}`.trim();
    el.textContent = message;
}

function clearHint(el) {
    setHint(el, '', '');
}


/**
 * 토스트 알림 표시
 */
function showToast(message, type = 'info', duration = TOAST_DURATION) {
    let container = document.getElementById('toast-container');

    // ✅ 없으면 만들어서 body에 붙임 (이렇게 하면 어디서 호출해도 안 터짐)
    if (!container) {
        // toast-container 없으면 화면에라도 알려주기
        console.warn('toast-container 없음. message:', message);
        return;
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('removing');
        setTimeout(() => toast.remove(), 300);
    }, duration);
}


/**
 * 로딩 상태 표시
 */
function showLoading() {
    let loader = document.getElementById('loading-spinner');
    if (!loader) {
        loader = document.createElement('div');
        loader.id = 'loading-spinner';
        loader.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            z-index: 9998;
        `;
        document.body.appendChild(loader);
    }
    loader.innerHTML = '<div class="loading"></div>';
    loader.style.display = 'block';
}

/**
 * 로딩 상태 숨기기
 */
function hideLoading() {
    const loader = document.getElementById('loading-spinner');
    if (loader) {
        loader.style.display = 'none';
    }
}

/**
 * 이메일 검증
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * 휴대폰 번호 검증
 */
function isValidPhone(phone) {
    const phoneRegex = /^010-\d{4}-\d{4}$|^\d{10,11}$/;
    return phoneRegex.test(phone);
}

/**
 * 테마 적용 (선택사항)
 */
function applyTheme(theme) {
    if (theme === 'DARK') {
        document.documentElement.setAttribute('data-theme', 'dark');
    } else {
        document.documentElement.setAttribute('data-theme', 'light');
    }
}

// ========== 에러 핸들링 ==========

window.addEventListener('error', function(event) {
    console.error('❌ 전역 오류:', event.error);
    // 필요에 따라 사용자에게 알림
});

// ========== 초기화 완료 ==========

console.log('✅ mypage.js 로드 완료');


// ========== 비번 눈 열고 닫기 ==========
// ========== 비번 눈 열고 닫기 (이벤트 위임 + 자동 주입) ==========
(() => {
    const EYE_CLOSED_SVG = `
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
    width="20" height="20" fill="none" stroke="#666"
    stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20
             c-5 0-9.27-3.11-11-7.5
             a11.05 11.05 0 0 1 5.17-5.81"/>
    <path d="M1 1l22 22"/>
    <path d="M9.53 9.53A3.5 3.5 0 0 0 12 15.5
             a3.5 3.5 0 0 0 2.47-5.97"/>
  </svg>`;

    const EYE_OPEN_SVG = `
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
    width="20" height="20" fill="none" stroke="#666"
    stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
    <circle cx="12" cy="12" r="3"/>
  </svg>`;

    function ensureIcons() {
        document.querySelectorAll('.pw-toggle').forEach((btn) => {
            btn.setAttribute('type', 'button');

            let icon = btn.querySelector('.eye-icon');
            if (!icon) {
                icon = document.createElement('span');
                icon.className = 'eye-icon';
                btn.appendChild(icon);
            }

            // 다른 스크립트가 비워버려도 다시 채움
            if (!icon.innerHTML || icon.innerHTML.trim().length === 0) {
                icon.innerHTML = EYE_CLOSED_SVG;
            }
        });
    }

    // 1) DOM 준비되면 한 번 채우기
    document.addEventListener('DOMContentLoaded', () => {
        ensureIcons();

        // 2) 다른 JS가 나중에 DOM 갈아치우는 경우 대비(한 박자 뒤에도 재주입)
        setTimeout(ensureIcons, 0);
        setTimeout(ensureIcons, 200);
    });

    // 3) “이벤트 위임”: 버튼이 나중에 생기거나 DOM이 교체되어도 클릭은 항상 잡힘
    document.addEventListener('click', (e) => {
        const btn = e.target.closest('.pw-toggle');
        if (!btn) return;

        e.preventDefault();
        e.stopPropagation();

        const targetId = btn.getAttribute('data-target');
        const input = document.getElementById(targetId);
        if (!input) return;

        // 클릭 직전에 아이콘이 비어있으면 다시 채움
        ensureIcons();

        const icon = btn.querySelector('.eye-icon');
        const isHidden = input.type === 'password';
        input.type = isHidden ? 'text' : 'password';
        icon.innerHTML = isHidden ? EYE_OPEN_SVG : EYE_CLOSED_SVG;
    });

    // 4) DOM이 통째로 바뀌는 경우(탭 렌더 등)도 자동 재주입
    const mo = new MutationObserver(() => ensureIcons());
    mo.observe(document.documentElement, { childList: true, subtree: true });
})();

// ========== PASSWORD 알람 ==========
// ========== 비밀번호 알람 ==========
function initPasswordRulesLive_B() {
    const currentEl = document.getElementById('current-password');
    const newEl = document.getElementById('new-password');
    const confirmEl = document.getElementById('confirm-password');

    const rulesBox = document.getElementById('pw-rules-box');
    const rules = document.getElementById('pw-rules');
    const strengthEl = document.getElementById('pw-strength');
    const confirmHint = document.getElementById('confirm-password-hint');

    if (!currentEl || !newEl || !confirmEl || !rulesBox || !rules || !strengthEl) return;

    const ruleLen = rules.querySelector('[data-rule="len"]');
    const ruleMix = rules.querySelector('[data-rule="mix"]');
    const ruleSame = rules.querySelector('[data-rule="same"]');

    function hasLetter(s) { return /[A-Za-z]/.test(s); }
    function hasDigit(s) { return /\d/.test(s); }

    function computeStrength(pw) {
        let score = 0;
        if (pw.length >= 8) score++;
        if (pw.length >= 12) score++;
        if (hasLetter(pw) && hasDigit(pw)) score++;
        if (/[!@#$%^&*()_\-+={}[\]:;"'<>,.?/\\|~`]/.test(pw)) score++;

        if (pw.length === 0) return { cls: '', text: '' };
        if (score <= 1) return { cls: 'weak', text: '보안 강도: 약함' };
        if (score <= 3) return { cls: 'medium', text: '보안 강도: 보통' };
        return { cls: 'strong', text: '보안 강도: 강함' };
    }

    // 룰 박스 숨기기/보이기 상태를 “사용자 입력 흐름”대로 제어하기 위한 플래그
    let userStartedTyping = false;

    function render() {
        const cur = currentEl.value || '';
        const pw = newEl.value || '';
        const cf = confirmEl.value || '';

        // 입력 시작 여부
        if (pw.length > 0) userStartedTyping = true;

        // ✅ 룰 체크
        const okLen = pw.length >= 8 && pw.length <= 100;
        const okMix = hasLetter(pw) && hasDigit(pw);
        const okSame = pw.length > 0 && cur.length > 0 && pw !== cur;
        const okConfirm = pw.length > 0 && cf.length > 0 && pw === cf;

        ruleLen?.classList.toggle('ok', okLen);
        ruleMix?.classList.toggle('ok', okMix);
        ruleSame?.classList.toggle('ok', okSame);

        // ✅ 강도
        const s = computeStrength(pw);
        strengthEl.className = `pw-strength ${s.cls}`;
        strengthEl.textContent = s.text;

        // ✅ 확인 비밀번호 실시간 안내
        if (confirmHint) {
            if (cf.length === 0) {
                confirmHint.className = 'field-hint info';
                confirmHint.textContent = '';
            } else if (pw !== cf) {
                confirmHint.className = 'field-hint error';
                confirmHint.textContent = '비밀번호가 일치하지 않습니다.';
            } else {
                confirmHint.className = 'field-hint success';
                confirmHint.textContent = '비밀번호가 일치합니다.';
            }
        }

        // ✅ B안 핵심: 입력 시작하면 박스 표시, 다 만족하면 자동으로 접힘
        const allOk = okLen && okMix && okSame && okConfirm;

        if (!userStartedTyping || pw.length === 0) {
            // 아직 입력 전/비운 상태면 숨김
            rulesBox.classList.remove('show');
        } else if (allOk) {
            // 조건 다 맞으면 자동으로 접기
            // rulesBox.classList.remove('show');
        } else {
            // 입력 중인데 아직 조건 미달이면 보여줌
            rulesBox.classList.add('show');
        }
    }

    currentEl.addEventListener('input', render);
    newEl.addEventListener('input', render);
    confirmEl.addEventListener('input', render);

    // 포커스 들어오면(이미 입력이 조금이라도 있으면) 박스 보여주는 UX도 흔함
    newEl.addEventListener('focus', () => {
        if ((newEl.value || '').length > 0) rulesBox.classList.add('show');
    });

    // 초기 렌더
    render();
}



// ========== [FIX] change-password-form 이벤트 강제 바인딩 (캡처링) ==========
(function bindChangePasswordFormHard() {

    function log(...args) { console.log('[PW-FIX]', ...args); }

    // 1) submit 이벤트를 "캡처링 단계"에서 잡음 (누가 stopPropagation 해도 잡힘)
    document.addEventListener('submit', function (e) {
        const form = e.target;
        if (form && form.id === 'change-password-form') {
            log('✅ submit 캡처됨');
            handleChangePassword(e); // 기존 함수 재사용
        }
    }, true);

    // 2) 어떤 스크립트가 버튼 click에서 preventDefault로 submit 막는 경우를 대비
    document.addEventListener('click', function (e) {
        const btn = e.target.closest('#change-password-form button[type="submit"]');
        if (!btn) return;

        const form = btn.closest('form');
        if (!form) return;

        log('✅ submit 버튼 클릭 캡처됨');

        // 다른 JS가 submit 막아도 여기서 직접 실행
        // (submit 이벤트가 아예 안 발생하는 케이스 커버)
        e.preventDefault();
        e.stopPropagation();

        // handleChangePassword는 submit 이벤트를 가정하니까,
        // fake event 형태로 넘김(최소한 preventDefault만 제공)
        handleChangePassword({
            preventDefault() {},
            target: form
        });
    }, true);

    log('바인딩 완료');

})();

// ========== 현재 비밀번호 blur 검증 ==========
// ========== 현재 비밀번호 실시간 검증 (input + debounce) ==========
(function bindCurrentPasswordLiveVerify() {
    const input = document.getElementById('current-password');
    const hint = document.getElementById('current-password-hint');
    if (!input || !hint) return;

    let timer = null;
    let controller = null;

    async function verify(value) {
        // 이전 요청 취소
        if (controller) controller.abort();
        controller = new AbortController();

        setHint(hint, '확인 중...', 'loading');

        try {
            const res = await fetch('/api/account/verify-current-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ currentPassword: value }),
                signal: controller.signal
            });

            // ✅ 400/500도 json으로 내려오니까 파싱
            const data = await res.json().catch(() => ({}));

            if (!res.ok) {
                // 서버가 400을 내려준 경우(빈 값 등)
                setHint(hint, data.message || '검증 실패', 'error');
                return;
            }

            if (data.success) {
                setHint(hint, data.message || '현재 비밀번호가 일치합니다.', 'success');
            } else {
                setHint(hint, data.message || '현재 비밀번호가 일치하지 않습니다.', 'error');
            }

        } catch (err) {
            // abort는 정상 흐름이라 표시 안 함
            if (err?.name === 'AbortError') return;

            console.error('❌ 현재 비밀번호 검증 실패:', err);
            setHint(hint, '검증 중 오류가 발생했습니다.', 'error');
        }
    }

    input.addEventListener('input', () => {
        const value = input.value.trim();

        // 값 비면 힌트 숨김 + 요청 취소
        if (!value) {
            if (controller) controller.abort();
            clearHint(hint);
            return;
        }

        // 너무 짧을 때는 UX상 굳이 서버 안 때리기(선택)
        if (value.length < 4) {
            setHint(hint, '4글자 이상 입력시 확인 가능 합니다.', 'info');
            return;
        }

        clearTimeout(timer);
        timer = setTimeout(() => verify(value), 400); // 0.4초 멈추면 호출
    });
})();
