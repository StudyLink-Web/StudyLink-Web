/* ===========================
   Verification Result JavaScript
   =========================== */

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Verification Result Page Loaded');
    initializeResultPage();
});

/**
 * 결과 페이지 초기화
 */
function initializeResultPage() {
    // 페이지 로드 애니메이션
    const resultCard = document.querySelector('.result-card');
    if (resultCard) {
        resultCard.style.animation = 'fadeInUp 0.6s ease-out';
    }

    // 버튼 클릭 이벤트
    setupButtonListeners();

    // 성공 시 분석 데이터 전송
    const resultIcon = document.querySelector('.result-icon');
    if (resultIcon && resultIcon.classList.contains('success-icon')) {
        logVerificationSuccess();
    }
}

/**
 * 버튼 클릭 이벤트 설정
 */
function setupButtonListeners() {
    const buttons = document.querySelectorAll('.btn');

    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
            // 버튼 클릭 로깅
            const btnText = this.textContent.trim();
            console.log('🔘 버튼 클릭:', btnText);
        });

        // 버튼 호버 효과
        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
        });

        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
}

/**
 * 인증 성공 로그
 */
function logVerificationSuccess() {
    const message = document.querySelector('.message');
    if (message) {
        console.log('✅ 인증 성공:', message.textContent);

        // Google Analytics 또는 다른 분석 도구에 전송 (선택사항)
        // gtag('event', 'student_verification_success', {
        //     event_category: 'authentication',
        //     event_label: 'email_verification'
        // });
    }
}

/**
 * 페이드인 애니메이션 추가 (CSS에 없을 경우)
 */
function addFadeInUpAnimation() {
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    `;
    document.head.appendChild(style);
}

// 초기 로드 시 애니메이션 추가
addFadeInUpAnimation();