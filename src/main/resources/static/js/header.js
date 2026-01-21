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
    }, 100);
}); // ← DOMContentLoated 닫기

/**
 * 로그아웃 폼 설정
 */
function setupLogoutForm() {
    const logoutForms = document.querySelectorAll('form[action*="logout"]');

    logoutForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            console.log('🔓 로그아웃 폼 제출');
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
            console.log('✅ 탭 버튼 - 처리 안 함');
            return;
        }

        // mypage 영역도 무시
        if (e.target.closest('.mypage-container')) {
            console.log('📌 mypage 영역 - 처리 안 함');
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

console.log('%c🎓 StudyLink - Header Loaded', 'font-size:14px;color:#667eea;font-weight:bold');
