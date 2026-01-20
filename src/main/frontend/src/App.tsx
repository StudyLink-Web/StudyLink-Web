import { useState, useEffect } from "react";
import Hero from "./components/Hero";
import MentorSection from "./components/MentorSection";
import AdSection from "./components/AdSection";
import CommunitySection from "./components/CommunitySection";
import QuickActionGrid from "./components/QuickActionGrid";
import AdmissionEssayPage from "./pages/AdmissionEssayPage";

function App() {
  const [scrollY, setScrollY] = useState(0);

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

  // AI 자소서 페이지일 경우 전체 화면 렌더링
  if (isCoverLetter) {
    return (
      <div className="min-h-screen bg-white dark:bg-[#030014] relative z-[9999]">
        <AdmissionEssayPage />
      </div>
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
      </main>
    </div>
  );
}

export default App;
