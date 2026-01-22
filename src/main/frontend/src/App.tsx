import { useState, useEffect } from "react";
import Hero from "./components/Hero";
import MentorSection from "./components/MentorSection";
import AdSection from "./components/AdSection";
import CommunitySection from "./components/CommunitySection";
import QuickActionGrid from "./components/QuickActionGrid";
import AdmissionEssayPage from "./pages/AdmissionEssayPage";
import PricingPage from "./pages/PricingPage";
import { requestForToken, onMessageListener } from "./firebase-init";

function App() {
  const [scrollY, setScrollY] = useState(0);
  const [pushToken, setPushToken] = useState<string | null>(null);

  // 푸시 알림 권한 요청 핸들러
  const handleRequestPermission = async () => {
    try {
      const token = await requestForToken();
      if (token) {
        setPushToken(token);
        // 📍 서버에 토큰 저장 호출 (빌드 에러 해결 및 기능 완결)
        await saveTokenToServer(token);
        alert("✅ 푸시 알림 권한 승인 및 서버 등록 완료!");
      } else {
        alert(
          "⚠️ 토큰을 가져오지 못했습니다. 브라우저 설정에서 알림 권한을 확인해 주세요.",
        );
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : String(error);
      alert(`❌ 오류 발생: ${errorMessage}`);
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
    const handleScroll = () => {
      setScrollY(window.scrollY);
    };
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const isCoverLetter =
    window.location.pathname === "/cover-letter" ||
    window.location.pathname === "/cover_letter";

  const isPricing = window.location.pathname === "/pricing";

  // AI 자소서 페이지일 경우 전체 화면 렌더링
  if (isCoverLetter) {
    return (
      <div className="min-h-screen bg-white dark:bg-[#030014] relative z-[9999]">
        <AdmissionEssayPage />
      </div>
    );
  }

  // 요금제 페이지 렌더링
  if (isPricing) {
    return <PricingPage />;
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

        {/* 푸시 알림 테스트용 플로팅 버튼 */}
        <div className="fixed bottom-8 right-8 z-50 flex flex-col items-end gap-3">
          {pushToken && (
            <div className="bg-white/95 dark:bg-slate-900/95 backdrop-blur-xl p-5 rounded-3xl shadow-[0_20px_50px_rgba(0,0,0,0.3)] border border-white/20 dark:border-white/5 text-[10px] max-w-[240px] break-all animate-in zoom-in-95 fade-in duration-500">
              <div className="flex items-center justify-between mb-3">
                <p className="font-black text-[#0969da] dark:text-blue-400 uppercase tracking-tighter">
                  Device Native PWA
                </p>
                <span className="flex h-2 w-2 rounded-full bg-green-500 animate-pulse" />
              </div>

              <div className="bg-slate-100 dark:bg-white/5 p-3 rounded-xl mb-4 font-mono text-[9px] text-slate-500 dark:text-slate-400 leading-tight border border-slate-200 dark:border-white/5">
                {pushToken}
              </div>

              <div className="flex gap-2">
                <button
                  onClick={handleTestServerPush}
                  className="flex-1 py-3 bg-gradient-to-br from-[#0969da] to-[#033d8b] hover:from-[#005cc5] hover:to-[#004a9f] text-white text-[10px] font-bold rounded-2xl transition-all shadow-lg active:scale-[0.98] flex items-center justify-center gap-1 group"
                >
                  <span>🚀</span>
                  나에게
                </button>
                <button
                  onClick={handleTestMineDevicesPush}
                  className="flex-1 py-3 bg-gradient-to-br from-[#12b886] to-[#087f5b] hover:from-[#099268] hover:to-[#055a44] text-white text-[10px] font-bold rounded-2xl transition-all shadow-lg active:scale-[0.98] flex items-center justify-center gap-1 group"
                >
                  <span>🔗</span>
                  내기기들
                </button>
                <button
                  onClick={handleTestAllDevicesPush}
                  className="flex-1 py-3 bg-gradient-to-br from-[#868e96] to-[#495057] hover:from-[#abb2b9] hover:to-[#566573] text-white text-[10px] font-bold rounded-2xl transition-all shadow-lg active:scale-[0.98] flex items-center justify-center gap-1 group"
                >
                  <span>📢</span>
                  전체공지
                </button>
              </div>
            </div>
          )}
          <button
            onClick={handleRequestPermission}
            className="bg-slate-900 dark:bg-white text-white dark:text-slate-900 px-7 py-4 rounded-full shadow-2xl font-black transition-all flex items-center gap-3 hover:scale-105 active:scale-95 group border border-white/10"
          >
            <span className="text-xl group-hover:rotate-12 transition-transform">
              🔔
            </span>
            알림 받기 설정
          </button>
        </div>
      </main>
    </div>
  );
}

export default App;
