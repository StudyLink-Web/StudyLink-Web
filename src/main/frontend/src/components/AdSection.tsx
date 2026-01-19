import { Crown, Check } from "lucide-react";
import type { FC } from "react";

const AdSection: FC = () => {
  return (
    <section className="py-32 bg-slate-50 dark:bg-[#030014] relative transition-colors duration-300">
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[1px] h-32 bg-gradient-to-b from-slate-200 dark:from-white/10 to-transparent" />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="relative rounded-[3rem] overflow-hidden border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0A0A0A] shadow-2xl dark:shadow-none group transition-colors duration-300">
          <div className="absolute top-0 right-0 w-[600px] h-[600px] bg-teal-200/50 dark:bg-purple-600/20 blur-[150px] rounded-full pointer-events-none opacity-50 group-hover:opacity-70 transition-opacity duration-1000" />
          <div className="absolute bottom-0 left-0 w-[600px] h-[600px] bg-teal-100/50 dark:bg-indigo-600/10 blur-[150px] rounded-full pointer-events-none" />
          <div className="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-10 mix-blend-overlay" />

          <div className="relative z-10 flex flex-col lg:flex-row items-center justify-between p-10 md:p-24 gap-16">
            <div className="flex-1 space-y-8 max-w-xl">
              <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-yellow-500/30 bg-yellow-500/10 text-yellow-600 dark:text-yellow-400 text-xs font-bold tracking-wider uppercase">
                <Crown width={14} height={14} />
                <span>Premium Membership</span>
              </div>
              <h2 className="text-4xl md:text-6xl font-bold text-slate-900 dark:text-white leading-[1.2] tracking-tight">
                <span className="whitespace-nowrap">합격 데이터의 힘,</span>
                <br />
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-yellow-500 to-amber-600 dark:from-yellow-200 dark:to-amber-500">
                  스터디링크 PASS
                </span>
              </h2>
              <p className="text-slate-600 dark:text-slate-400 text-lg leading-relaxed">
                더 이상 정보 격차로 고민하지 마세요.
                <br />
                오직 패스 회원에게만 제공되는 시크릿 리포트.
              </p>
              <div className="space-y-4 pt-4">
                {[
                  "전년도 합격자 생기부 원본 열람",
                  "AI 모의면접 무제한 이용",
                  "전용 Q&A 라운지 입장",
                ].map((item, i) => (
                  <div
                    key={i}
                    className="flex items-center gap-4 text-slate-700 dark:text-slate-300"
                  >
                    <div className="flex-shrink-0 w-6 h-6 rounded-full bg-yellow-500/10 border border-yellow-500/20 flex items-center justify-center">
                      <Check
                        className="text-yellow-600 dark:text-yellow-400"
                        width={12}
                        height={12}
                      />
                    </div>
                    <span className="text-sm font-medium">{item}</span>
                  </div>
                ))}
              </div>
              <div className="pt-8 flex gap-4">
                <button className="bg-slate-900 dark:bg-white text-white dark:text-black hover:bg-slate-800 dark:hover:bg-slate-200 px-8 py-4 rounded-full font-bold transition-all transform hover:scale-105 shadow-lg shadow-teal-500/10 dark:shadow-purple-500/10">
                  30일 무료 체험하기
                </button>
              </div>
            </div>
            {/* Visual element on the right */}
            <div className="flex-1 w-full flex justify-center lg:justify-end perspective-1000">
              <div className="relative w-80 h-[32rem] bg-[#020617] rounded-[2.5rem] border-2 border-cyan-500/20 shadow-[0_0_50px_-12px_rgba(6,182,212,0.3)] transition-transform duration-500 hover:scale-105 group/card overflow-hidden">
                {/* 1. Background Layer (Deep Space Navy) */}
                <div className="absolute inset-0 bg-gradient-to-br from-[#020617] via-[#082f49] to-[#020617]" />

                {/* 2. Effect Layer (Clean Cyan Glow) - 노이즈 제거하여 선명도 확보 */}
                <div className="absolute top-[-20%] left-[-20%] w-full h-full bg-cyan-400/10 blur-[100px] rounded-full pointer-events-none group-hover/card:bg-cyan-300/20 transition-all duration-1000" />

                {/* 3. Text Layer (Electric Cyan & Pure White) - 가시성 극대화 */}
                <div className="relative z-10 p-8 flex flex-col h-full">
                  <div className="w-full flex justify-between items-center mb-10">
                    <span className="text-[10px] font-black text-white tracking-[0.25em] uppercase">
                      Membership Pass
                    </span>
                    <Crown
                      className="text-white drop-shadow-[0_0_15px_rgba(255,255,255,0.4)]"
                      width={24}
                      height={24}
                    />
                  </div>

                  <div className="mt-auto space-y-7">
                    <div>
                      <div className="text-white text-3xl font-black mb-1.5 tracking-tight drop-shadow-sm">
                        PREMIUM PASS
                      </div>
                      <div className="text-white text-xs font-black tracking-[0.2em] uppercase">
                        StudyLink Exclusive
                      </div>
                    </div>

                    <div className="pt-6 border-t border-white/10">
                      <div className="flex justify-between items-end">
                        <div className="space-y-2">
                          <div className="text-white text-[10px] tracking-[0.1em] font-bold">
                            VERIFIED MEMBER
                          </div>
                          <div className="text-white text-base font-black tracking-widest">
                            DIGITAL TICKET
                          </div>
                        </div>
                        <div className="w-12 h-7 bg-gradient-to-br from-cyan-400 to-blue-600 rounded-md shadow-[0_0_15px_rgba(34,211,238,0.4)]" />
                      </div>
                    </div>
                  </div>
                </div>

                {/* Decorative border glow */}
                <div className="absolute inset-0 border border-cyan-500/10 rounded-[2.5rem] pointer-events-none" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default AdSection;
