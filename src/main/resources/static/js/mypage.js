/**
 * mypage.js - 마이페이지 JavaScript
 * 마이페이지의 모든 기능을 관리하는 메인 스크립트
 */

// ========== 전역 설정 ==========

const API_BASE = '/api';
const TOAST_DURATION = 3000; // 3초

// ========== 초기화 ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ 마이페이지 초기화 시작');

    initializeEventListeners();
    loadInitialData();
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
    document.getElementById('change-password-form')?.addEventListener('submit', handleChangePassword);
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
    e.preventDefault();

    const currentPassword = document.getElementById('current-password').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    // 검증
    if (!currentPassword || !newPassword || !confirmPassword) {
        showToast('모든 항목을 입력하세요', 'warning');
        return;
    }

    if (newPassword.length < 8) {
        showToast('새 비밀번호는 8자 이상이어야 합니다', 'warning');
        return;
    }

    if (newPassword !== confirmPassword) {
        showToast('새 비밀번호가 일치하지 않습니다', 'warning');
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
                showToast('비밀번호가 변경되었습니다', 'success');
            } else {
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

/**
 * 토스트 알림 표시
 */
function showToast(message, type = 'info', duration = TOAST_DURATION) {
    const container = document.getElementById('toast-container');

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('removing');
        setTimeout(() => {
            toast.remove();
        }, 300);
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
