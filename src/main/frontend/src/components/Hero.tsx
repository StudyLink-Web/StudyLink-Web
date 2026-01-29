import { BookOpen, Trophy, ArrowRight, Sparkles } from "lucide-react";
import type { FC } from "react";

interface HeroProps {
  scrollProgress?: number;
}

const Hero: FC<HeroProps> = ({ scrollProgress = 0 }) => {
  // 스크롤에 따른 블루 스팟 투명도 계산 (1 -> 0)
  const spotOpacity = 1 - scrollProgress;

  return (
    <section className="relative w-full min-h-[90vh] flex flex-col justify-center items-center overflow-hidden bg-transparent dark:bg-transparent pt-20 transition-colors duration-300">
      {/* 배경 레이어: 노이즈 질감과 그리드 패턴 */}
      <div className="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20 z-[1] pointer-events-none mix-blend-soft-light" />
      <div className="absolute inset-0 bg-grid-white/[0.03] bg-[bottom_1px_center] z-[0] pointer-events-none mask-image-gradient-vertical" />

      {/* 스포트라이트 효과: 그라데이션, 블러, 애니메이션이 조합된 동적인 배경광 */}
      <div
        className="absolute inset-0 pointer-events-none overflow-hidden"
        style={{
          maskImage: "linear-gradient(to bottom, black 40%, transparent 95%)",
          WebkitMaskImage:
            "linear-gradient(to bottom, black 40%, transparent 95%)",
        }}
      >
        <div
          className="absolute top-[-10%] left-[-30%] w-[160%] h-[140vh] bg-teal-300/40 dark:bg-purple-800/20 rounded-full blur-[160px] animate-pulse-slow transition-opacity duration-300"
          style={{ opacity: spotOpacity * 0.4 }}
        />
        <div
          className="absolute top-[40%] left-[-10%] w-[80vw] h-[80vw] bg-teal-200/30 dark:bg-blue-800/15 rounded-full blur-[140px] animate-blob transition-opacity duration-300"
          style={{ opacity: spotOpacity * 0.3 }}
        />
        <div
          className="absolute bottom-[-10%] right-[-10%] w-[80vw] h-[80vw] bg-teal-100/30 dark:bg-indigo-800/15 rounded-full blur-[140px] animate-blob animation-delay-2000 transition-opacity duration-300"
          style={{ opacity: spotOpacity * 0.3 }}
        />
      </div>

      {/* 부유하는 요소 (데스크톱): 둥둥 떠 있는 듯한 애니메이션과 유리 질감(Glassmorphism) 효과 */}
      <div className="absolute top-[20%] left-[10%] hidden lg:block animate-float opacity-80 dark:opacity-60">
        <div className="bg-white/60 dark:bg-white/5 backdrop-blur-lg border border-slate-200 dark:border-white/10 p-4 rounded-2xl transform -rotate-12 hover:rotate-0 transition-transform duration-500 shadow-2xl shadow-teal-900/10 dark:shadow-purple-900/10">
          <BookOpen
            className="text-teal-500 dark:text-blue-400 mb-2"
            width={32}
            height={32}
          />
          <div className="h-2 w-12 bg-slate-300 dark:bg-white/20 rounded-full mb-1" />
          <div className="h-2 w-8 bg-slate-200 dark:bg-white/10 rounded-full" />
        </div>
      </div>

      <div className="absolute top-[25%] right-[12%] hidden lg:block animate-float-delayed opacity-80 dark:opacity-60">
        <div className="bg-white/60 dark:bg-white/5 backdrop-blur-lg border border-slate-200 dark:border-white/10 p-4 rounded-2xl transform rotate-6 hover:rotate-0 transition-transform duration-500 shadow-2xl shadow-teal-900/10 dark:shadow-purple-900/10">
          <Trophy
            className="text-yellow-500 dark:text-yellow-400 mb-2"
            width={32}
            height={32}
          />
          <div className="text-xs font-bold text-slate-800 dark:text-white">
            Top 1%
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="relative z-10 max-w-5xl mx-auto px-4 sm:px-6 text-center mt-[-5vh]">
        {/* Badge */}
        <div className="inline-flex items-center justify-center mb-8 animate-fade-in-up">
          <div className="group cursor-pointer relative">
            {/* 글로우 효과: 배지 뒤에서 은은하게 빛나는 그라데이션 */}
            <div className="absolute inset-0 bg-gradient-to-r from-teal-500 to-teal-400 dark:from-purple-500 dark:to-indigo-500 rounded-full blur opacity-20 dark:opacity-30 group-hover:opacity-60 transition duration-300" />
            {/* 유리 질감 배지: backdrop-blur를 이용한 반투명 효과 */}
            <div className="relative flex items-center gap-2 bg-white/50 dark:bg-[#0F0C29]/80 border border-slate-200 dark:border-white/10 rounded-full pl-3 pr-4 py-1.5 backdrop-blur-md hover:border-teal-300 dark:hover:border-purple-300 transition-colors">
              <span className="flex h-2 w-2 relative">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-teal-400 dark:bg-green-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-teal-500 dark:bg-green-500" />
              </span>
              <span className="text-slate-600 dark:text-slate-300 text-xs font-medium tracking-wide">
                2025학년도 입시 리포트 공개
              </span>
            </div>
          </div>
        </div>

        {/* 메인 타이틀: 큰 폰트 크기와 입체적인 그라데이션 텍스트 적용 */}
        <h1 className="text-5xl md:text-7xl lg:text-8xl font-bold text-slate-900 dark:text-white tracking-tight leading-[1.1] mb-8 animate-fade-in-up [animation-delay:200ms]">
          입시의{" "}
          <span className="text-transparent bg-clip-text bg-gradient-to-b from-slate-600 to-slate-400 dark:from-white dark:to-slate-300">
            모든 것
          </span>
          ,<br />
          <span className="relative inline-block">
            <span className="absolute -inset-2 bg-teal-500/10 dark:bg-purple-500/20 blur-2xl rounded-full" />
            {/* 쉬머 효과: 좌우로 흐르는 듯한 빛나는 텍스트 애니메이션 */}
            <span className="relative text-transparent bg-clip-text bg-gradient-to-r from-teal-600 via-teal-400 to-teal-600 dark:from-cyan-300 dark:via-white dark:to-cyan-300 animate-shimmer bg-[length:200%_100%]">
              StudyLink
            </span>
          </span>
          <span className="text-slate-900 dark:text-white">에서</span>
        </h1>

        <p className="text-lg md:text-xl text-slate-600 dark:text-slate-200 max-w-2xl mx-auto mb-12 leading-relaxed animate-fade-in-up [animation-delay:400ms]">
          SKY 재학생 멘토링부터 AI 생활기록부 분석까지.
          <br className="hidden md:block" />
          가장 확실한 합격 데이터를 지금 바로 경험하세요.
        </p>

        {/* 버튼 섹션: 그림자 효과, 스케일 변화, 그라데이션 오버레이로 인터랙션 강조 */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 animate-fade-in-up [animation-delay:600ms]">
          <a
            href="/room/list"
            className="group relative w-full sm:w-auto overflow-hidden rounded-full bg-slate-900 dark:bg-indigo-600 px-8 py-4 text-base font-bold text-white transition-all transform hover:scale-105 active:scale-95 shadow-[0_20px_50px_rgba(0,0,0,0.3)] dark:shadow-[0_20px_50px_rgba(79,70,229,0.2)] no-underline hover:no-underline flex items-center justify-center gap-2"
          >
            <div className="absolute inset-0 bg-gradient-to-r from-teal-500/20 to-teal-400/20 dark:from-white/10 dark:to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
            <div className="relative flex items-center justify-center gap-2">
              <span>문제 풀러가기</span>
              <ArrowRight
                width={18}
                height={18}
                className="group-hover:translate-x-1 transition-transform"
              />
            </div>
          </a>

          <a
            href="/chatbot"
            className="w-full sm:w-auto flex items-center justify-center gap-2 px-8 py-4 rounded-full font-medium text-slate-700 dark:text-white border border-slate-300 dark:border-white/10 bg-white/50 dark:bg-white/5 hover:bg-white/80 dark:hover:bg-white/10 backdrop-blur-sm transition-all hover:border-slate-400 dark:hover:border-white/20 shadow-md no-underline hover:no-underline"
          >
            <Sparkles
              className="text-teal-600 dark:text-purple-400"
              width={16}
              height={16}
            />
            <span>AI 무료 상담</span>
          </a>
        </div>
      </div>

      {/* 하단 페이드아웃 오버레이: 다음 섹션과 자연스럽게 연결하기 위한 긴 그라데이션 */}
      <div className="absolute bottom-0 left-0 w-full h-80 bg-gradient-to-t from-white via-white/80 to-transparent z-20 pointer-events-none dark:from-[#030014] dark:to-transparent" />
    </section>
  );
};

export default Hero;
