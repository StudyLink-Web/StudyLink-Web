import { useState, useEffect, useRef, lazy, Suspense, memo } from "react";
import Hero from "./components/Hero";
import { requestForToken, onMessageListener } from "./firebase-init";
import { AnimatePresence } from "framer-motion";
import Splash from "./components/Splash";
import NotificationCenter from "./components/NotificationCenter";

// [Vercel Best Practice 1.5] 다이나믹 import를 통해 무거운 컴포넌트를 렌더링 전에 로딩
const AdmissionEssayPage = lazy(() => import("./pages/AdmissionEssayPage"));
const PricingPage = lazy(() => import("./pages/PricingPage"));
const MentorListPage = lazy(() => import("./pages/MentorListPage"));
const MentorDetailPage = lazy(() => import("./pages/MentorDetailPage"));

// 스크롤 시 불필요한 재렌더링을 방지하기 위해 메모이제이션된 컴포넌트로 추출
const MentorSection = memo(lazy(() => import("./components/MentorSection")));
const AdSection = memo(lazy(() => import("./components/AdSection")));
const CommunitySection = memo(
  lazy(() => import("./components/CommunitySection")),
);
const QuickActionGrid = memo(
  lazy(() => import("./components/QuickActionGrid")),
);

const isDarkInitial = typeof document !== 'undefined' && document.documentElement.classList.contains('dark');

function App() {
  const [scrollY, setScrollY] = useState(0);
  const [pushToken, setPushToken] = useState<string | null>(localStorage.getItem("pushToken"));
  const [isPushPanelOpen, setIsPushPanelOpen] = useState(false);
  const [showSplash, setShowSplash] = useState(true);
  const [unreadCount, setUnreadCount] = useState(0); 
  const [theme, setTheme] = useState<'light' | 'dark'>(isDarkInitial ? 'dark' : 'light'); 
  const panelRef = useRef<HTMLDivElement>(null);

  // 📍 인트로 세션 관리 (나중에 다시 활성화할 예정)
  /* useEffect(() => {
    const isSplashShown = sessionStorage.getItem("splash_shown");
    if (!isSplashShown) {
      setShowSplash(true);
    }
  }, []); */

  // 테마 감지 로직
  useEffect(() => {
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.attributeName === 'class') {
          const isDark = document.documentElement.classList.contains('dark');
          setTheme(isDark ? 'dark' : 'light');
        }
      });
    });

    observer.observe(document.documentElement, { attributes: true });
    return () => observer.disconnect();
  }, []);

  // 푸시 알림 권한 요청 핸들러
  const handleRequestPermission = async () => {
    if (isPushPanelOpen && pushToken) {
      setIsPushPanelOpen(false);
      return;
    }

    try {
      const token = await requestForToken();
      if (token) {
        
        setPushToken(token);
        localStorage.setItem("pushToken", token); 
        await saveTokenToServer(token);
        
        setIsPushPanelOpen(true); 

        // DB 초기화 이후에는 토큰이 같더라도 서버 입장에선 새로 등록이 필요하므로 알림을 띄워줍니다.
        alert("푸시 알림 기기 등록이 완료되었습니다! 🚀");
      } else {
        alert("토큰을 가져오지 못했습니다. 브라우저 설정에서 알림 권한을 확인해 주세요.");
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error);
      if (errorMessage.includes("permission-blocked")) {
        alert("브라우저에서 알림 권한이 차단되어 있습니다.\n\n설정에서 허용 후 다시 시도해 주세요.");
      } else {
        alert(`❌ 오류 발생: ${errorMessage}`);
      }
    }
  };

  const saveTokenToServer = async (token: string) => {
    try {
      await fetch("/api/fcm/token", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token }),
      });
    } catch (error) {
      console.error("서버 토큰 등록 실패:", error);
    }
  };

  const handleTestServerPush = async () => {
    if (!pushToken) return alert("먼저 알림 권한을 승인해 주세요!");
    try {
      await fetch("/api/fcm/test", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token: pushToken }),
      });
      alert("테스트 푸시가 발송되었습니다.");
    } catch (error) {
      alert("테스트 푸시 실패");
    }
  };

  const handleTestAllDevicesPush = async () => {
    const message = window.prompt("전체 공지 메시지:", "StudyLink 전체 알림입니다.");
    if (message === null) return;
    try {
      await fetch("/api/fcm/test-all", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message }),
      });
      alert("전체 발송 완료");
    } catch (error) {
      alert("전체 발송 실패");
    }
  };

  const handleTestMineDevicesPush = async () => {
    try {
      const response = await fetch("/api/fcm/test-mine", { method: "POST" });
      const result = await response.text();
      alert(`내 기기 알림 발송: ${result}`);
    } catch (error) {
      alert("내 기기 알림 실패");
    }
  };

  // 포그라운드 메시지 수신 설정
  useEffect(() => {
    onMessageListener()
      .then((payload) => {
        const messagePayload = payload as any;
        if (messagePayload?.data) {
          alert(`StudyLink 알림\n\n${messagePayload.data.title}\n${messagePayload.data.body}`);
        }
      })
      .catch((error) => console.log("failed: ", error));
  }, []);

  useEffect(() => {
    const syncToken = async () => {
      if (Notification.permission === "granted") {
        const token = await requestForToken();
        if (token) {
          setPushToken(token);
          localStorage.setItem("pushToken", token);
          await saveTokenToServer(token);
        }
      }
    };
    syncToken();

    const handleScroll = () => setScrollY(window.scrollY);
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  useEffect(() => {
    (window as any).openNotificationCenter = () => setIsPushPanelOpen(true);
    const handleClickOutside = (event: MouseEvent) => {
      if (panelRef.current && !panelRef.current.contains(event.target as Node)) {
        setIsPushPanelOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      delete (window as any).openNotificationCenter;
    };
  }, []);

  const isCoverLetter = window.location.pathname.startsWith("/cover");
  const isPricing = window.location.pathname === "/pricing";
  const isMentorList = window.location.pathname === "/mentors";

  if (isCoverLetter) {
    return (
      <Suspense fallback={<div className="min-h-screen bg-white dark:bg-[#030014]" />}>
        <div className="min-h-screen bg-white dark:bg-[#030014] relative z-[9999]">
          <AdmissionEssayPage />
        </div>
      </Suspense>
    );
  }

  if (isPricing) {
    return (
      <Suspense fallback={<div className="min-h-screen bg-white dark:bg-[#0d1117]" />}>
        <PricingPage />
      </Suspense>
    );
  }

  if (isMentorList) {
    return (
      <Suspense fallback={<div className="min-h-screen bg-white dark:bg-[#030014]" />}>
        <MentorListPage />
      </Suspense>
    );
  }

  // 멘토 상세 페이지 라우팅 처리 (기존 라우터 구조가 React Router Dom이 아닌 것으로 추정되거나 혼용 중이라 수동 분기 처리 필요)
  // 하지만 App.tsx 내용을 보니 react-router-dom을 안 쓰고 window.location 기반 수동 라우팅을 하고 있음 (isCoverLetter, isPricing, isMentorList 등)
  // 따라서 isMentorDetail 분기를 추가해야 함.

  const isMentorDetail = window.location.pathname.startsWith("/mentors/") && window.location.pathname.split("/").length === 3;

  if (isMentorDetail) {
    return (
      <Suspense fallback={<div className="min-h-screen bg-white dark:bg-[#030014]" />}>
        <MentorDetailPage />
      </Suspense>
    );
  }

  const progress = Math.min(scrollY / 200, 1);
  const bgColor = `rgb(${248 + (255 - 248) * progress}, ${250 + (255 - 250) * progress}, ${252 + (255 - 252) * progress})`;

  return (
    <AnimatePresence mode="wait">
      {showSplash ? (
        <Splash 
          key="splash" 
          onComplete={() => {
            // sessionStorage.setItem("splash_shown", "true"); // 상시 노출을 위해 주석 처리
            setShowSplash(false);
          }} 
        />
      ) : (
        <div
          key="main-app"
          className={`min-h-screen w-full transition-colors duration-300 overflow-x-hidden dynamic-bg ${theme}`}
          style={{ "--scroll-bg": bgColor } as React.CSSProperties}
        >
          <main className="relative">
            <Suspense fallback={<div className="h-screen bg-transparent" />}>
              <Hero scrollProgress={progress} />
              
              <div className="bg-white/50 dark:bg-[#030014] border-y border-slate-200 dark:border-white/5 py-4 overflow-hidden whitespace-nowrap relative z-20 backdrop-blur-sm">
                <div className="inline-block animate-marquee">
                  <span className="mx-8 text-xs font-mono text-slate-600 dark:text-slate-500 tracking-widest uppercase">✨ 2024 SKY Admission Rate 94%</span>
                  <span className="mx-8 text-xs font-mono text-slate-600 dark:text-slate-500 tracking-widest uppercase">✨ Verified Mentors Only</span>
                  <span className="mx-8 text-xs font-mono text-slate-600 dark:text-slate-500 tracking-widest uppercase">✨ 15,000+ Matches</span>
                </div>
              </div>

              <AdSection />
              <MentorSection />
              <QuickActionGrid />
              <CommunitySection />
            </Suspense>

            <NotificationCenter 
              ref={panelRef}
              isOpen={isPushPanelOpen} 
              onClose={() => setIsPushPanelOpen(false)} 
              onUnreadCountChange={setUnreadCount}
              pushToken={pushToken}
              onTestPush={handleTestServerPush}
              onTestMine={handleTestMineDevicesPush}
              onTestAll={handleTestAllDevicesPush}
            />

            <div className="fixed bottom-8 right-8 z-50 flex flex-col items-end gap-3">
              <button
                onClick={handleRequestPermission}
                className={`px-8 py-4 rounded-full shadow-2xl font-black transition-all flex items-center gap-3 hover:scale-105 active:scale-95 group backdrop-blur-xl border ${
                  isPushPanelOpen
                    ? "bg-white/90 dark:bg-slate-800/90 text-slate-900 border-slate-200"
                    : "bg-slate-900/90 dark:bg-indigo-600/20 text-white border-white/10"
                }`}
              >
                <div className={`relative ${!isPushPanelOpen && unreadCount > 0 && "animate-bounce"}`}>
                  <span className="text-xl">{isPushPanelOpen ? "✕" : "🔔"}</span>
                  {!isPushPanelOpen && unreadCount > 0 && (
                    <span className="absolute -top-2 -right-2 min-w-[18px] h-[18px] bg-red-500 text-white text-[10px] flex items-center justify-center rounded-full px-1">
                      {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                  )}
                </div>
                <span>{isPushPanelOpen ? "닫기" : "알림 설정"}</span>
              </button>
            </div>
          </main>
        </div>
      )}
    </AnimatePresence>
  );
}

export default App;
