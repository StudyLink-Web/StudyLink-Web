/* ===========================
StudyLink - Header JavaScript
=========================== */

/**
 * 페이지 로드 완료 시 실행
 */
document.addEventListener('DOMContentLoaded', function () {
    console.log('✅ StudyLink Header 로드됨');

    // jQuery와 Bootstrap이 완전히 로드될 때까지 약간 지연
    setTimeout(() => {
        setupMenuEvents();
        highlightActiveMenu();
        setupMobileMenuAutoClose();
        setupLogoutForm();
        setupProfileDropdown();
        updateDday();
        initializeMyPageTabs();
        initNotificationCenter();
        initThemeToggle();
    }, 100);
}); 

/**
 * 로그아웃 폼 설정 (푸시 토큰 삭제 포함)
 */
function setupLogoutForm() {
    const logoutForms = document.querySelectorAll('form[action*="logout"]');

    logoutForms.forEach(form => {
        form.addEventListener('submit', async function(e) {
            const pushToken = localStorage.getItem('pushToken');
            
            if (pushToken) {
                // 🛑 토큰이 있으면 삭제될 때까지 폼 제출을 잠시 중단
                e.preventDefault();
                console.log('🔄 로그아웃 전 푸시 토큰 삭제 시도...');

                try {
                    const response = await fetch('/api/fcm/token', {
                        method: 'DELETE',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ token: pushToken })
                    });
                    
                    if (response.ok) {
                        console.log('✅ 기기 토큰 삭제 완료');
                    }
                } catch (err) {
                    console.error('❌ 토큰 삭제 중 오류 발생:', err);
                } finally {
                    // 성공 여부와 관계없이 로컬 정보 지우고 실제 로그아웃 진행
                    localStorage.removeItem('pushToken');
                    console.log('🔓 로그아웃 세션 처리 진행');
                    form.submit(); 
                }
            } else {
                console.log('🔓 등록된 토큰 없음, 일반 로그아웃 진행');
            }
        });
    });
}

/**
 * 프로필 드롭다운 메뉴 설정
 */
function setupProfileDropdown() {
    const currentPath = window.location.pathname;

    // login/signup 페이지에서는 드롭다운 비활성화
    if (currentPath.includes('/login') || currentPath.includes('/signup')) {
        console.log('🔒 로그인/회원가입 페이지: 드롭다운 비활성화');
        return;
    }

    // Bootstrap 드롭다운 비활성화 (자체 구현 사용)
    if (typeof $ !== 'undefined' && $.fn.dropdown) {
        $('[data-toggle="dropdown"]').off('click');
        console.log('✅ Bootstrap 4 드롭다운 비활성화 (자체 구현 사용)');
    }

    // 수동 클릭 이벤트 바인딩
    const userDropdown = document.getElementById('userDropdown');
    if (userDropdown) {
        userDropdown.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const menu = this.nextElementSibling; // 바로 다음 ul 요소
            if (menu && menu.classList.contains('dropdown-menu')) {
                menu.classList.toggle('show');
                console.log('🎯 드롭다운 메뉴 토글됨');
            }
        });
    }

    // ⭐ 수정: 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', function(e) {
        // 🎯 탭 버튼이면 완전히 무시 (아무것도 하지 않음)
        if (e.target.closest('.tab-btn')) {
            return;
        }

        // mypage 영역도 무시
        if (e.target.closest('.mypage-container')) {
            return;
        }

        // 그 외 모든 곳에서만 드롭다운 닫기
        const dropdown = document.querySelector('.header .dropdown');
        if (dropdown && !dropdown.contains(e.target)) {
            const menu = dropdown.querySelector('.dropdown-menu');
            if (menu && menu.classList.contains('show')) {
                menu.classList.remove('show');
                console.log('❌ 드롭다운 메뉴 닫음');
            }
        }
    });

    console.log('✅ 프로필 드롭다운 설정 완료');
}

/**
 * 마이페이지 탭 초기화
 */
function initializeMyPageTabs() {
    // 마이페이지가 아니면 실행 안 함
    if (!document.querySelector('.mypage-container')) {
        return;
    }

    console.log('🔍 마이페이지 탭 초기화 시작');

    const tabLinks = document.querySelectorAll('.nav-link[data-tab]');
    console.log(`📍 찾은 탭 링크 개수: ${tabLinks.length}`);

    if (tabLinks.length === 0) {
        console.error('❌ 탭 링크를 찾을 수 없습니다!');
        return;
    }

    tabLinks.forEach(link => {
        const tabName = link.getAttribute('data-tab');
        console.log(`📌 탭 링크 바인딩: ${tabName}`);

        link.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const clickedTab = this.getAttribute('data-tab');
            console.log(`🔄 클릭된 탭: ${clickedTab}`);

            // 1️⃣ 모든 탭 콘텐츠 숨기기
            document.querySelectorAll('.tab-content').forEach(tab => {
                tab.classList.remove('active');
            });

            // 2️⃣ 모든 네비게이션 링크 비활성화
            document.querySelectorAll('.nav-link[data-tab]').forEach(nav => {
                nav.classList.remove('active');
            });

            // 3️⃣ 클릭한 링크 활성화
            this.classList.add('active');

            // 4️⃣ 해당 탭 콘텐츠 활성화
            const selectedContent = document.querySelector(`#${clickedTab}-tab`);
            if (selectedContent) {
                selectedContent.classList.add('active');
                console.log(`✅ 탭 변경 완료: ${clickedTab}`);
            } else {
                console.error(`❌ 탭을 찾을 수 없습니다: #${clickedTab}-tab`);
            }
        });
    });

    console.log('✅ 마이페이지 탭 초기화 완료');
}

/**
 * D-day 업데이트 (Thymeleaf에서 받은 값 활용)
 */
function updateDday() {
    try {
        const ddaySpans = document.querySelectorAll('.dday span');

        console.log('🔍 �은 D-day span 개수:', ddaySpans.length);

        if (ddaySpans.length === 0) {
            console.warn('⚠️ D-day 요소를 찾을 수 없습니다');
            return;
        }

        ddaySpans.forEach((span, index) => {
            const originalText = span.textContent.trim();
            console.log(`📌 Span ${index} 원본 텍스트:`, `"${originalText}"`);

            if (!originalText || originalText === '' || isNaN(originalText)) {
                console.warn(`⚠️ Span ${index}에 유효한 값이 없습니다. 기본값 사용`);

                const ddayValue = span.getAttribute('data-dday') || span.parentElement.getAttribute('data-dday');

                if (!ddayValue) {
                    console.warn(`⚠️ 데이터 속성도 없습니다. 요소 내용:`, span.outerHTML);
                    return;
                }

                processAndDisplayDday(span, ddayValue);
            } else {
                processAndDisplayDday(span, originalText);
            }
        });

        scheduleNextDayUpdate();

    } catch (e) {
        console.error('❌ D-day 업데이트 오류', e);
    }
}

/**
 * D-day 값 처리 및 표시
 */
function processAndDisplayDday(span, ddayValue) {
    try {
        const dayDiff = parseInt(ddayValue, 10);

        console.log(`📊 처리된 D-day 값:`, dayDiff);

        if (isNaN(dayDiff)) {
            console.warn(`⚠️ parseInt 실패. 원본 값: "${ddayValue}"`);
            return;
        }

        if (dayDiff > 0) {
            span.textContent = `D-${dayDiff}`;
            span.style.color = '#667eea';
            span.style.fontWeight = '700';
            console.log(`✅ 양수 D-day 적용: D-${dayDiff}`);
        } else if (dayDiff === 0) {
            span.textContent = 'D-DAY 🎯';
            span.style.color = '#ff6b6b';
            span.style.fontWeight = 'bold';
            console.log(`✅ D-DAY 적용`);
        } else {
            span.textContent = `D+${Math.abs(dayDiff)}`;
            span.style.color = '#95a5a6';
            span.style.fontWeight = '600';
            console.log(`✅ 음수 D-day 적용: D+${Math.abs(dayDiff)}`);
        }
    } catch (e) {
        console.error(`❌ D-day 처리 오류:`, e);
    }
}

/**
 * 매일 자정에 D-day 자동 갱신
 */
function scheduleNextDayUpdate() {
    const now = new Date();
    const tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0);
    const timeUntilMidnight = tomorrow.getTime() - now.getTime();

    setTimeout(() => {
        console.log('🔄 D-day 자동 갱신 시간입니다. 페이지를 새로고침하세요.');
        scheduleNextDayUpdate();
    }, timeUntilMidnight);
}

/**
 * 메뉴 이벤트 바인딩
 */
function setupMenuEvents() {
    const navLinks = document.querySelectorAll('.header-nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', function () {
            console.log('🔗 메뉴 클릭:', this.textContent.trim());
        });
    });

    const dropdownItems = document.querySelectorAll('.dropdown-item');
    dropdownItems.forEach(item => {
        item.addEventListener('click', function () {
            console.log('📌 드롭다운 클릭:', this.textContent.trim());
        });
    });
}

/**
 * 현재 경로에 따라 활성 메뉴 표시
 */
function highlightActiveMenu() {
    const currentPath = window.location.pathname;
    console.log('🔍 현재 경로:', currentPath);

    const navLinks = document.querySelectorAll('.header-nav-link');

    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (!href) return;

        // ⭐ 추가: /my-page 경로 명시적 처리
        if (currentPath === '/my-page' && href === '/my-page') {
            link.classList.add('active');
            console.log('✅ 마이페이지 활성화됨');
        } else if (href === currentPath || (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}


/**
 * 모바일 메뉴 자동 닫기
 */
function setupMobileMenuAutoClose() {
    const navLinks = document.querySelectorAll('.header-nav-link, .dropdown-item');
    navLinks.forEach(link => {
        link.addEventListener('click', function () {
            console.log('📱 모바일 메뉴 닫기 트리거');
        });
    });
}

/**
 * 스크롤 시 헤더 그림자 효과
 */
let lastScrollTop = 0;
window.addEventListener('scroll', function () {
    const header = document.querySelector('.header');
    if (!header) return;

    const currentScroll = window.pageYOffset || document.documentElement.scrollTop;

    if (currentScroll > 50 && currentScroll > lastScrollTop) {
        header.style.boxShadow = '0 8px 20px rgba(102, 126, 234, 0.2)';
    } else if (currentScroll <= 50) {
        header.style.boxShadow = 'none';
    }

    lastScrollTop = Math.max(currentScroll, 0);
});

/**
 * 알림 센터 초기화
 */
async function initNotificationCenter() {
    const notiBell = document.getElementById('notiBell');
    const notiPanel = document.getElementById('notiPanel');
    const notiBadge = document.getElementById('notiBadge');
    const notiList = document.getElementById('notiList');
    const markAllRead = document.getElementById('markAllRead');

    if (!notiBell) return;

    // 1. 읽지 않은 알림 개수 로드
    updateUnreadCount();

    // 2. 종 클릭 시 패널 토글
    notiBell.addEventListener('click', (e) => {
        e.stopPropagation();
        const isShowing = notiPanel.classList.toggle('show');
        if (isShowing) {
            fetchNotifications();
        }
    });

    // ⭐ 리액트 알림 센터와 동기화를 위한 커스텀 이벤트 리스너 추가
    window.addEventListener('notificationUpdate', (e) => {
        console.log('🔔 Notification update event received:', e.detail);
        
        // 📍 상세 데이터에 count가 있으면 서버 요청 없이 즉시 업데이트
        if (e.detail && typeof e.detail.count === 'number') {
            updateUnreadCount(e.detail.count);
        } else {
            updateUnreadCount();
        }

        if (notiPanel.classList.contains('show')) {
            fetchNotifications();
        }
    });

    // 3. 패널 내부 클릭 시 닫히지 않게
    notiPanel.addEventListener('click', (e) => e.stopPropagation());

    // 4. 외부 클릭 시 패널 닫기
    document.addEventListener('click', () => {
        notiPanel.classList.remove('show');
    });

    // 5. 모두 읽음 처리
    markAllRead.addEventListener('click', async () => {
        try {
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

            await fetch('/api/notifications/read-all', { 
                method: 'PUT',
                headers: {
                    [csrfHeader]: csrfToken
                }
            });
            updateUnreadCount();
            fetchNotifications();
        } catch (err) {
            console.error('Failed to mark all as read', err);
        }
    });

    /**
     * 안 읽은 알림 개수 업데이트
     */
    async function updateUnreadCount(forcedCount = null) {
        try {
            // 📍 강제 값이 들어오면 서버 요청 없이 즉시 반영 (실시간성)
            if (forcedCount !== null) {
                applyBadgeCount(forcedCount);
                return;
            }

            // 📍 캐시 방지를 위해 타임스탬프 추가
            const res = await fetch('/api/notifications/unread-count?t=' + new Date().getTime());
            const count = await res.json();
            applyBadgeCount(count);
        } catch (err) {
            console.error('Failed to fetch unread count', err);
        }
    }

    /**
     * 배지 표시 및 숫자 적용 공통 함수
     */
    function applyBadgeCount(count) {
        if (count > 0) {
            notiBadge.textContent = count > 99 ? '99+' : count;
            notiBadge.style.display = 'flex';
        } else {
            notiBadge.textContent = '0';
            notiBadge.style.display = 'none';
        }
    }

    /**
     * 알림 목록 가져오기 및 렌더링
     */
    async function fetchNotifications() {
        try {
            notiList.innerHTML = '<div class="noti-empty">불러오는 중...</div>';
            // 📍 캐시 방지 타임스탬프 추가
            const res = await fetch('/api/notifications?t=' + new Date().getTime());
            const data = await res.json();

            if (!data || data.length === 0) {
                notiList.innerHTML = '<div class="noti-empty">알림이 없습니다.</div>';
                return;
            }

            notiList.innerHTML = '';
            data.forEach(noti => {
                const item = document.createElement('div');
                item.className = `noti-item ${noti.isRead ? '' : 'unread'}`;
                
                const timeStr = new Date(noti.createdAt).toLocaleString('ko-KR', {
                    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                });

                item.innerHTML = `
                    <div class="noti-title">${noti.message}</div>
                    <div class="noti-time">${timeStr}</div>
                `;

                item.addEventListener('click', async () => {
                    if (!noti.isRead) {
                        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

                        await fetch(`/api/notifications/${noti.id}/read`, { 
                            method: 'PUT',
                            headers: {
                                [csrfHeader]: csrfToken
                            }
                        });
                        updateUnreadCount();
                    }
                    // 클릭 시 관련 링크로 이동 로직 추가 가능 (현재는 내역 확인이 목적)
                    item.classList.remove('unread');
                });

                notiList.appendChild(item);
            });

            // ⭐ 추가: "전체 보기" 버튼 클릭 시 리액트 알림 센터 열기
            const viewAllBtn = document.querySelector('.noti-footer a');
            if (viewAllBtn) {
                viewAllBtn.addEventListener('click', (e) => {
                    if (window.openNotificationCenter) {
                        e.preventDefault();
                        e.stopPropagation();
                        window.openNotificationCenter();
                        notiPanel.classList.remove('show'); // 기존 드롭다운은 닫기
                    }
                });
            }
        } catch (err) {
            notiList.innerHTML = '<div class="noti-empty">알림을 불러오지 못했습니다.</div>';
            console.error('Failed to fetch notifications', err);
        }
    }
}

/**
 * 테마 토글 초기화 (다크모드/라이트모드)
 */
function initThemeToggle() {
    const themeToggle = document.getElementById('themeToggle');
    const sunIcon = themeToggle?.querySelector('.sun-icon');
    const moonIcon = themeToggle?.querySelector('.moon-icon');

    if (!themeToggle) return;

    // 1. 저장된 테마 확인
    const savedTheme = localStorage.getItem('theme');
    const isDark = savedTheme === 'dark' || (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches);

    if (isDark) {
        document.documentElement.classList.add('dark');
        document.body.classList.add('dark');
        sunIcon.style.display = 'none';
        moonIcon.style.display = 'inline-block';
    }

    // 2. 클릭 이벤트
    themeToggle.addEventListener('click', () => {
        const currentlyDark = document.documentElement.classList.toggle('dark');
        document.body.classList.toggle('dark', currentlyDark);
        localStorage.setItem('theme', currentlyDark ? 'dark' : 'light');

        if (currentlyDark) {
            sunIcon.style.display = 'none';
            moonIcon.style.display = 'inline-block';
        } else {
            sunIcon.style.display = 'inline-block';
            moonIcon.style.display = 'none';
        }
    });
}

console.log('%c🎓 StudyLink - Header Loaded', 'font-size:14px;color:#667eea;font-weight:bold');
