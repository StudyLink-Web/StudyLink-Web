import { useState, useEffect, useRef, lazy, Suspense, memo } from "react";
import Hero from "./components/Hero";
import { requestForToken, onMessageListener } from "./firebase-init";

// Use Dynamic Imports for Heavy Components
const AdmissionEssayPage = lazy(() => import("./pages/AdmissionEssayPage"));
const PricingPage = lazy(() => import("./pages/PricingPage"));

// Extract to Memoized Components to prevent unnecessary re-renders during scroll
const MentorSection = memo(lazy(() => import("./components/MentorSection")));
const AdSection = memo(lazy(() => import("./components/AdSection")));
const CommunitySection = memo(
  lazy(() => import("./components/CommunitySection")),
);
const QuickActionGrid = memo(
  lazy(() => import("./components/QuickActionGrid")),
);

function App() {
  const [scrollY, setScrollY] = useState(0);
  const [pushToken, setPushToken] = useState<string | null>(null);
  const [isPushPanelOpen, setIsPushPanelOpen] = useState(false);
  const panelRef = useRef<HTMLDivElement>(null);

  // 푸시 알림 권한 요청 핸들러
  const handleRequestPermission = async () => {
    // 📍 이미 열려있고 토큰이 있다면 토글(닫기)
    if (isPushPanelOpen && pushToken) {
      setIsPushPanelOpen(false);
      return;
    }

    try {
      const token = await requestForToken();
      if (token) {
        setPushToken(token);
        await saveTokenToServer(token);
        setIsPushPanelOpen(true); // 📍 성공 시 패널 열기
        if (!isPushPanelOpen)
          alert("✅ 푸시 알림 권한 승인 및 서버 등록 완료!");
      } else {
        alert(
          "⚠️ 토큰을 가져오지 못했습니다. 브라우저 설정에서 알림 권한을 확인해 주세요.",
        );
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : String(error);

      if (errorMessage.includes("permission-blocked")) {
        alert(
          "🔒 브라우저에서 알림 권한이 차단되어 있습니다.\n\n" +
            "해결 방법:\n" +
            "1. 주소창 왼쪽의 [자물쇠] 또는 [설정] 아이콘 클릭\n" +
            "2. [알림] 항목을 [허용]으로 변경\n" +
            "3. 페이지를 새로고침(F5) 후 다시 [권한 요청] 클릭",
        );
      } else {
        alert(`❌ 오류 발생: ${errorMessage}`);
      }
    }
  };

  // 📍 서버에 토큰 저장
  const saveTokenToServer = async (token: string) => {
    try {
      const response = await fetch("/api/fcm/token", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token }),
      });
      console.log("✅ 서버에 토큰 등록 시도:", await response.text());
    } catch (err) {
      console.error("❌ 서버 토큰 등록 실패:", err);
    }
  };

  // 📍 서버 측 테스트 푸시 발송 요청
  const handleTestServerPush = async () => {
    if (!pushToken) return alert("먼저 알림 권한을 승인해 주세요!");
    try {
      const response = await fetch("/api/fcm/test", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token: pushToken }),
      });
      const result = await response.text();
      alert(
        `🚀 서버 응답: ${result}\n\n알림이 안 온다면 응답 내용을 확인해 보세요!`,
      );
    } catch (error) {
      console.error("❌ 서버 테스트 푸시 요청 실패:", error);
      alert("❌ 서버 테스트 푸시 요청 실패");
    }
  };

  // 📍 모든 기기 대상 통합 알림 테스트
  const handleTestAllDevicesPush = async () => {
    try {
      const response = await fetch("/api/fcm/test-all", {
        method: "POST",
      });
      const result = await response.text();
      alert(
        `📢 모든 기기 발송 요청: ${result}\n\n이제 다른 기기를 확인해 보세요!`,
      );
    } catch (error) {
      console.error("❌ 통합 테스트 푸시 요청 실패:", error);
      alert("❌ 통합 테스트 푸시 요청 실패");
    }
  };

  // 📍 내 계정으로 로그인된 모든 기기에 전송
  const handleTestMineDevicesPush = async () => {
    try {
      const response = await fetch("/api/fcm/test-mine", {
        method: "POST",
      });
      const result = await response.text();
      if (result.includes("Error")) {
        alert("🔒 로그인이 필요한 기능입니다!");
      } else {
        alert(
          `🔗 내 기기 연동 알림: ${result}\n이 계정으로 로그인된 다른 폰/PC를 확인해 보세요!`,
        );
      }
    } catch (error) {
      console.error("❌ 내 기기 테스트 푸시 요청 실패:", error);
      alert("❌ 내 기기 테스트 푸시 요청 실패");
    }
  };

  // 포그라운드 메시지 수신 설정
  useEffect(() => {
    onMessageListener()
      .then((payload) => {
        const messagePayload = payload as any;
        console.log("📩 포그라운드 알림 수신:", messagePayload);
        if (messagePayload?.notification) {
          alert(
            `StudyLink 알림\n\n${messagePayload.notification.title}\n${messagePayload.notification.body}`,
          );
        }
      })
      .catch((error) => console.log("failed: ", error));
  }, []);

  useEffect(() => {
    // 📍 자동 토큰 동기화: 이미 권한이 있다면 로그인 상태 변화 등에 대비해 서버에 토큰 갱신
    const syncToken = async () => {
      if (Notification.permission === "granted") {
        const token = await requestForToken();
        if (token) {
          setPushToken(token);
          await saveTokenToServer(token);
          console.log("🔄 알림 토큰 자동 동기화 완료");
        }
      }
    };
    syncToken();

    const handleScroll = () => {
      // Throttle or just ensure passive is set
      setScrollY(window.scrollY);
    };
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []); // Static dependency array is correct here

  // 📍 외부 클릭 시 패널 닫기 로직
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        panelRef.current &&
        !panelRef.current.contains(event.target as Node)
      ) {
        setIsPushPanelOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const isCoverLetter =
    window.location.pathname === "/cover-letter" ||
    window.location.pathname === "/cover_letter";

  const isPricing = window.location.pathname === "/pricing";

  // AI 자소서 페이지일 경우 전체 화면 렌더링
  if (isCoverLetter) {
    return (
      <Suspense
        fallback={<div className="min-h-screen bg-white dark:bg-[#030014]" />}
      >
        <div className="min-h-screen bg-white dark:bg-[#030014] relative z-[9999]">
          <AdmissionEssayPage />
        </div>
      </Suspense>
    );
  }

  // 요금제 페이지 렌더링
  if (isPricing) {
    return (
      <Suspense
        fallback={<div className="min-h-screen bg-white dark:bg-[#0d1117]" />}
      >
        <PricingPage />
      </Suspense>
    );
  }

  // 배경색 보간 (BG Color Interpolation)
  // slate-50: rgb(248, 250, 252) -> white: rgb(255, 255, 255)
  // 임계값을 200으로 줄여 더 빠른 반응성 제공
  const progress = Math.min(scrollY / 200, 1);
  const bgColor = `rgb(${248 + (255 - 248) * progress}, ${
    250 + (255 - 250) * progress
  }, ${252 + (255 - 252) * progress})`;

  // 메인 페이지 렌더링
  return (
    <div
      className="min-h-screen w-full dark:bg-[#030014] transition-colors duration-300 overflow-x-hidden"
      style={{ backgroundColor: bgColor }}
    >
      <main className="relative">
        <Hero scrollProgress={progress} />

        {/* [Vercel Best Practice 1.5] Strategic Suspense Boundaries for independent sections */}
        <Suspense
          fallback={
            <div className="h-40 animate-pulse bg-slate-100 dark:bg-white/5" />
          }
        >
          <QuickActionGrid />

          {/* Infinite Ticker */}
          <div className="bg-white/50 dark:bg-[#030014] border-y border-slate-200 dark:border-white/5 py-4 overflow-hidden whitespace-nowrap relative z-20 backdrop-blur-sm">
            <div className="inline-block animate-shimmer bg-gradient-to-r from-transparent via-teal-500/5 dark:via-white/5 to-transparent bg-[length:200%_100%] w-full absolute inset-0 pointer-events-none" />
            <div className="inline-block animate-marquee">
              <span className="mx-8 text-xs font-mono text-slate-600 dark:text-slate-500 tracking-widest uppercase">
                ✨ 2024 SKY Admission Rate 94%
              </span>
              <span className="mx-8 text-xs font-mono text-slate-600 dark:text-slate-500 tracking-widest uppercase">
                ✨ Verified Mentors Only
              </span>
              <span className="mx-8 text-xs font-mono text-slate-600 dark:text-slate-500 tracking-widest uppercase">
                ✨ 15,000+ Matches
              </span>
              <span className="mx-8 text-xs font-mono text-slate-600 dark:text-slate-500 tracking-widest uppercase">
                ✨ 2024 SKY Admission Rate 94%
              </span>
              <span className="mx-8 text-xs font-mono text-slate-600 dark:text-slate-500 tracking-widest uppercase">
                ✨ Verified Mentors Only
              </span>
              <span className="mx-8 text-xs font-mono text-slate-600 dark:text-slate-500 tracking-widest uppercase">
                ✨ 15,000+ Matches
              </span>
            </div>
          </div>

          <MentorSection />
          <AdSection />
          <CommunitySection />
        </Suspense>

        {/* 푸시 알림 테스트용 플로팅 버튼 */}
        <div className="fixed bottom-8 right-8 z-50 flex flex-col items-end gap-3">
          {isPushPanelOpen && pushToken && (
            <div
              ref={panelRef}
              className="bg-white/95 dark:bg-slate-900/95 backdrop-blur-xl p-5 rounded-3xl shadow-[0_20px_50px_rgba(0,0,0,0.3)] border border-white/20 dark:border-white/5 text-[10px] max-w-[240px] break-all animate-in slide-in-from-bottom-5 zoom-in-95 fade-in duration-300 mb-2"
            >
              <div className="flex items-center justify-between mb-3">
                <p className="font-black text-[#0969da] dark:text-blue-400 uppercase tracking-tighter">
                  Device Native PWA
                </p>
                <button
                  onClick={() => setIsPushPanelOpen(false)}
                  className="text-slate-400 hover:text-slate-600 transition-colors"
                >
                  ✕
                </button>
              </div>

              <div className="bg-slate-100 dark:bg-white/5 p-3 rounded-xl mb-4 font-mono text-[9px] text-slate-500 dark:text-slate-400 leading-tight border border-slate-200 dark:border-white/5 max-h-[100px] overflow-y-auto">
                {pushToken}
              </div>

              <div className="grid grid-cols-1 gap-2">
                <button
                  onClick={handleTestServerPush}
                  className="w-full py-3 bg-gradient-to-br from-[#0969da] to-[#033d8b] hover:from-[#005cc5] hover:to-[#004a9f] text-white text-[10px] font-bold rounded-2xl transition-all shadow-lg active:scale-[0.98] flex items-center justify-center gap-1 group"
                >
                  <span>🚀</span> 나에게 쏘기
                </button>
                <button
                  onClick={handleTestMineDevicesPush}
                  className="w-full py-3 bg-gradient-to-br from-[#12b886] to-[#087f5b] hover:from-[#099268] hover:to-[#055a44] text-white text-[10px] font-bold rounded-2xl transition-all shadow-lg active:scale-[0.98] flex items-center justify-center gap-1 group"
                >
                  <span>🔗</span> 내 기기들에게 쏘기
                </button>
                <button
                  onClick={handleTestAllDevicesPush}
                  className="w-full py-3 bg-gradient-to-br from-[#868e96] to-[#495057] hover:from-[#abb2b9] hover:to-[#566573] text-white text-[10px] font-bold rounded-2xl transition-all shadow-lg active:scale-[0.98] flex items-center justify-center gap-1 group"
                >
                  <span>📢</span> 전체 공지 (테스트용)
                </button>
              </div>
            </div>
          )}
          <button
            onClick={handleRequestPermission}
            className={`px-7 py-4 rounded-full shadow-2xl font-black transition-all flex items-center gap-3 hover:scale-105 active:scale-95 group border border-white/10 ${
              isPushPanelOpen
                ? "bg-slate-100 dark:bg-slate-800 text-slate-900 dark:text-white"
                : "bg-slate-900 dark:bg-white text-white dark:text-slate-900"
            }`}
          >
            <span
              className={`text-xl transition-transform ${isPushPanelOpen ? "rotate-90" : "group-hover:rotate-12"}`}
            >
              {isPushPanelOpen ? "✕" : "🔔"}
            </span>
            {isPushPanelOpen ? "닫기" : "알림 받기 설정"}
          </button>
        </div>
      </main>
    </div>
  );
}

export default App;
