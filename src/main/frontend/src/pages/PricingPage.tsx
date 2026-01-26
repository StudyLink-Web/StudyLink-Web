import type { FC } from "react";
import { Check, ArrowUpRight, Crown, Zap } from "lucide-react";

interface PlanProps {
  name: string;
  price: string;
  description: string;
  features: string[];
  buttonText: string;
  highlight?: boolean;
  freeTrial?: boolean;
}

const PlanCard: FC<PlanProps> = ({
  name,
  price,
  description,
  features,
  buttonText,
  highlight,
  freeTrial,
}) => {
  return (
    <div className="relative group flex flex-col h-full perspective-1000">
      {/* Background Glow Effect */}
      <div className={`absolute -inset-2 rounded-[2.5rem] blur-2xl opacity-0 group-hover:opacity-20 transition-opacity duration-500 pointer-events-none ${
        highlight ? "bg-blue-500" : "bg-slate-400"
      }`} />

      <div
        className={`relative flex flex-col h-full p-8 md:p-10 rounded-[2.5rem] border transition-all duration-500 overflow-hidden backdrop-blur-3xl shadow-2xl ${
          highlight
            ? "border-blue-500/30 bg-white/70 dark:bg-white/[0.04] ring-1 ring-blue-500/20"
            : "border-slate-200 dark:border-white/10 bg-white/50 dark:bg-white/[0.02]"
        }`}
      >
        {/* Recommend Badge */}
        {highlight && (
          <div className="absolute top-0 right-0 px-6 py-2 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-bl-2xl shadow-lg z-20">
            <span className="text-white text-[10px] font-black tracking-widest uppercase flex items-center gap-1.5">
              <Crown width={10} height={10} />
              Recommended
            </span>
          </div>
        )}

        <div className="mb-10">
          <div className="flex items-center gap-2 mb-4">
            <h3 className={`text-2xl font-black tracking-tight ${
              highlight ? "text-blue-600 dark:text-blue-400" : "text-slate-900 dark:text-slate-50"
            }`}>
              {name}
            </h3>
            {freeTrial && (
              <span className="px-2 py-0.5 rounded-md bg-teal-500/10 border border-teal-500/20 text-teal-600 dark:text-teal-400 text-[10px] font-bold uppercase tracking-tighter">
                Free Trial
              </span>
            )}
          </div>
          <p className="text-sm text-slate-600 dark:text-slate-300 font-medium leading-relaxed min-h-[40px]">
            {description}
          </p>
        </div>

        <div className="mb-10 flex items-baseline gap-1">
          <span className="text-4xl md:text-5xl font-black text-slate-900 dark:text-slate-50 tracking-tighter">
            {price}
          </span>
          {price !== "Free" && (
            <span className="text-sm font-bold text-slate-500 dark:text-slate-400">
              / month
            </span>
          )}
        </div>

        <div className="mb-10">
          <a
            href={name === "Free" ? "/login" : "/pricing"}
            className={`relative w-full py-4 rounded-2xl font-black flex items-center justify-center gap-2 transition-all transform hover:scale-[1.02] active:scale-[0.98] overflow-hidden no-underline hover:no-underline group/btn shadow-xl ${
              highlight
                ? "bg-slate-900 border-2 border-blue-500/20 shadow-blue-500/10"
                : "bg-slate-800 border-2 border-slate-700 shadow-black/20"
            }`}
          >
            {/* Hover Color Fill Effect */}
            <div className={`absolute inset-0 transition-transform duration-500 translate-y-[101%] group-hover/btn:translate-y-0 ${
              highlight 
                ? "bg-gradient-to-r from-blue-600 via-indigo-600 to-blue-700"
                : "bg-gradient-to-r from-slate-700 to-slate-600"
            }`} />
            
            <span className="relative z-10 flex items-center gap-2 text-white transition-colors duration-300">
              {buttonText}
              <ArrowUpRight width={18} height={18} className="group-hover/btn:translate-x-1 group-hover/btn:-translate-y-1 transition-transform" />
            </span>
          </a>
        </div>

        <div className="space-y-4 flex-1">
          <div className="text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2">
            What's included
          </div>
          {features.map((feature, i) => (
            <div key={i} className="flex items-start gap-3 group/item">
              <div className={`flex-shrink-0 mt-0.5 w-5 h-5 rounded-full flex items-center justify-center transition-colors ${
                highlight ? "bg-blue-500/10 text-blue-500" : "bg-slate-100 dark:bg-white/5 text-slate-400"
              }`}>
                <Check width={12} height={12} strokeWidth={3} />
              </div>
              <span className="text-sm text-slate-700 dark:text-slate-200 font-semibold group-hover/item:text-slate-900 dark:group-hover/item:text-white transition-colors">
                {feature}
              </span>
            </div>
          ))}
        </div>

        {/* Decorative corner elements */}
        <div className="absolute -bottom-12 -right-12 w-32 h-32 bg-gradient-to-br from-blue-500/10 to-transparent rounded-full blur-3xl opacity-0 group-hover:opacity-100 transition-opacity duration-1000" />
      </div>
    </div>
  );
};

const PricingPage: FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-[#030014] py-24 px-4 sm:px-6 relative overflow-hidden transition-colors duration-500">
      {/* Background Decorative Layers - AdSection 스타일 계승 */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full h-full opacity-30 dark:opacity-20 pointer-events-none">
        <div className="absolute top-[10%] left-[10%] w-[40rem] h-[40rem] bg-blue-400/20 dark:bg-purple-600/10 blur-[150px] rounded-full" />
        <div className="absolute bottom-[10%] right-[10%] w-[40rem] h-[40rem] bg-indigo-400/20 dark:bg-blue-600/10 blur-[150px] rounded-full" />
      </div>
      <div className="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-[0.03] dark:opacity-[0.05] mix-blend-overlay pointer-events-none" />

      <div className="max-w-7xl mx-auto relative z-10">
        <div className="text-center mb-24 space-y-6">
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-blue-500/30 bg-blue-500/10 text-blue-600 dark:text-blue-400 text-xs font-bold tracking-wider uppercase animate-fade-in-up">
            <Zap width={14} height={14} fill="currentColor" />
            <span>Scale your potential</span>
          </div>
          <h1 className="text-5xl md:text-7xl font-black text-slate-900 dark:text-white tracking-tighter leading-tight animate-fade-in-up [animation-delay:200ms]">
            Choose Your <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 via-indigo-500 to-blue-600 dark:from-blue-400 dark:via-white dark:to-blue-400 animate-shimmer bg-[length:200%_100%]">
              Dream Factory
            </span>
          </h1>
          <p className="text-slate-600 dark:text-slate-400 text-lg md:text-xl max-w-2xl mx-auto font-medium leading-relaxed animate-fade-in-up [animation-delay:400ms]">
            당신의 입시 성공을 위한 최적의 파트너. <br className="hidden md:block" />
            지금 바로 스터디링크와 함께 합격의 길을 열어보세요.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-8 lg:gap-10 items-stretch pt-6 animate-fade-in-up [animation-delay:600ms]">
          <PlanCard
            name="Free"
            price="Free"
            description="입시 준비의 시작을 위한 베이직 플랜"
            buttonText="시작하기"
            features={[
              "최신 대학 입시 뉴스 구독",
              "기본 입시 데이터 조회 (일 5회)",
              "공개 멘토링 게시판 읽기 권한",
              "AI 상담 기초 답변 (일 3회)",
            ]}
          />

          <PlanCard
            highlight
            name="Standard"
            price="₩19,900"
            description="효율적인 합격 전략을 위한 인기 플랜. 첫 이용 시 30일 무료 체험 제공"
            buttonText="30일 체험 시작하기"
            freeTrial
            features={[
              "AI 자소서 분석 (월 10회)",
              "대학별 합격 예측 데이터 조회",
              "맞춤형 입시 리포트 제공",
              "AI 모의면접 체험 (월 3회)",
              "첫 이용 시 30일 무료 체험",
            ]}
          />

          <PlanCard
            name="Premium PASS"
            price="₩49,900"
            description="완벽한 합격을 위한 모든 권한과 데이터"
            buttonText="PASS 구매하기"
            features={[
              "전년도 합격자 생기부 원본 열람",
              "AI 자소서/면접 분석 무제한",
              "1:1 입시 멘토링 우선 매칭",
              "전용 Q&A 라운지 입장 권한",
              "실시간 전문 컨설턴트 상담 연동",
            ]}
          />
        </div>

        {/* Footer CTA */}
        <div className="mt-32 p-12 rounded-[3.5rem] bg-white/50 dark:bg-white/[0.03] border border-slate-200 dark:border-white/10 text-center backdrop-blur-3xl shadow-2xl relative overflow-hidden group hover:border-blue-500/30 transition-all duration-500 animate-fade-in-up [animation-delay:800ms]">
          <div className="absolute -top-24 -left-24 w-64 h-64 bg-blue-500/10 blur-[100px] rounded-full group-hover:bg-blue-500/20 transition-all duration-1000" />
          
          <h2 className="text-3xl font-black text-slate-900 dark:text-white mb-6 relative z-10 tracking-tight">
            학교 및 교육기관 전용 플랜
          </h2>
          <p className="text-slate-600 dark:text-slate-400 mb-10 font-semibold max-w-2xl mx-auto leading-relaxed relative z-10">
            학교, 학원 등 단체 이용을 위한 맞춤형 대량 구매 플랜을 제안해 드립니다. <br />
            전문 컨설턴트가 직접 방문하여 도입 상담을 도와드립니다.
          </p>
          <button className="relative z-10 px-8 py-3 rounded-full border-2 border-slate-900 dark:border-white text-slate-900 dark:text-white font-black hover:bg-slate-900 hover:text-white dark:hover:bg-white dark:hover:text-slate-950 transition-all transform hover:scale-[1.05] active:scale-[0.95] flex items-center justify-center gap-2 mx-auto group/cta">
            입시 거점 센터 문의하기
            <ArrowUpRight className="group-hover/cta:translate-x-1 group-hover/cta:-translate-y-1 transition-transform" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default PricingPage;
