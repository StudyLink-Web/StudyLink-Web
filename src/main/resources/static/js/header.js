/* ===========================
   StudyLink - Header JavaScript
   =========================== */

/**
 * 페이지 로드 완료 시 실행
 */
document.addEventListener('DOMContentLoaded', function () {
    console.log('✅ StudyLink Header 로드됨');

    setupMenuEvents();
    highlightActiveMenu();
    setupMobileMenuAutoClose();
    calculateCSATDday();
});

/**
 * 2027 수능 D-day 계산
 */
function calculateCSATDday() {
    try {
        const today = new Date();
        const csatDate = new Date(2027, 10, 11); // 2027-11-11

        const timeDiff = csatDate.getTime() - today.getTime();
        const dayDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24));

        const ddayElement = document.getElementById('csatDday');

        if (!ddayElement) return;

        if (dayDiff > 0) {
            ddayElement.textContent = `D-${dayDiff}`;
            ddayElement.style.color = '#667eea';
        } else if (dayDiff === 0) {
            ddayElement.textContent = 'D-DAY 🎯';
            ddayElement.style.color = '#ff6b6b';
            ddayElement.style.fontWeight = 'bold';
        } else {
            ddayElement.textContent = `D+${Math.abs(dayDiff)}`;
            ddayElement.style.color = '#95a5a6';
        }

        console.log(`📅 D-day 계산 완료: ${ddayElement.textContent}`);
    } catch (e) {
        console.error('❌ D-day 계산 오류', e);
    }
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
    const navLinks = document.querySelectorAll('.header-nav-link');

    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (!href) return;

        if (href === currentPath || (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

/**
 * 모바일 메뉴 자동 닫기 (향후 확장 대비)
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
