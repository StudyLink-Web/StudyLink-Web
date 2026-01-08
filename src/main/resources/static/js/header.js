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
    updateDday();
});

/**
 * D-day 업데이트 (Thymeleaf에서 받은 값 활용)
 */
function updateDday() {
    try {
        // .dday 섹션의 span 요소 찾기
        const ddaySpans = document.querySelectorAll('.dday span');

        console.log('🔍 찾은 D-day span 개수:', ddaySpans.length);

        if (ddaySpans.length === 0) {
            console.warn('⚠️ D-day 요소를 찾을 수 없습니다');
            return;
        }

        // 모든 D-day 요소 업데이트 (로그인/비로그인 상태 모두 대응)
        ddaySpans.forEach((span, index) => {
            // 원본 텍스트 출력
            const originalText = span.textContent.trim();
            console.log(`📌 Span ${index} 원본 텍스트:`, `"${originalText}"`);

            // 빈 문자열이나 공백만 있는 경우 처리
            if (!originalText || originalText === '' || isNaN(originalText)) {
                console.warn(`⚠️ Span ${index}에 유효한 값이 없습니다. 기본값 사용`);

                // ✅ 새로운 방법: HTML 데이터 속성 확인
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

        // 매일 자정에 자동 갱신 (선택사항)
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

        // D-day 표시 로직
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
 * 매일 자정에 D-day 자동 갱신 (선택사항)
 */
function scheduleNextDayUpdate() {
    const now = new Date();
    const tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0);
    const timeUntilMidnight = tomorrow.getTime() - now.getTime();

    setTimeout(() => {
        console.log('🔄 D-day 자동 갱신 시간입니다. 페이지를 새로고침하세요.');
        // window.location.reload(); // 필요시 활성화
        scheduleNextDayUpdate(); // 재귀적으로 매일 실행
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
