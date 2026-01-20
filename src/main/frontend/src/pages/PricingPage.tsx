import type { FC } from "react";
import { Check } from "lucide-react";

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
    <div
      className={`relative flex flex-col h-full pt-6 transition-all duration-500 hover:scale-[1.03] group`}
    >
      {highlight && (
        <div className="absolute top-0 left-0 right-0 h-12 bg-[#0969da] flex items-center justify-center rounded-t-2xl z-20 transition-transform duration-500">
          <span className="text-white text-xs font-black tracking-widest uppercase">
            Recommended
          </span>
        </div>
      )}

      <div
        className={`relative flex flex-col p-8 rounded-2xl border-2 h-full z-10 transition-colors duration-500 ${
          highlight
            ? "border-[#0969da] bg-white dark:bg-[#0d1117] shadow-xl rounded-t-none"
            : "border-slate-200 dark:border-white/10 bg-white dark:bg-[#0d1117]"
        }`}
      >
        {freeTrial && !highlight && (
          <div className="absolute -top-4 left-1/2 -translate-x-1/2 px-4 py-1 bg-gradient-to-r from-teal-500 to-blue-500 text-white text-[10px] font-black tracking-widest uppercase rounded-full shadow-lg">
            30 Days Free Trial
          </div>
        )}

        <div className="mb-8">
          <h3 className="text-2xl font-bold mb-3 text-slate-900 dark:text-white">
            {name}
          </h3>
          <p className="text-sm text-slate-600 dark:text-slate-400 font-medium leading-relaxed min-h-[40px]">
            {description}
          </p>
        </div>

        <div className="mb-8 flex items-baseline">
          <span className="text-4xl font-black text-slate-900 dark:text-white">
            {price}
          </span>
          {price !== "Free" && (
            <span className="text-sm ml-2 text-slate-500 font-bold">
              / month
            </span>
          )}
        </div>

        <button
          className={`w-full py-4 rounded-xl font-bold transition-all mb-8 ${
            highlight
              ? "bg-[#24292f] text-white hover:bg-black dark:bg-[#30363d] dark:hover:bg-[#484f58]"
              : "bg-white text-[#24292f] border border-slate-200 hover:bg-slate-50 dark:bg-transparent dark:text-white dark:border-white/10 dark:hover:bg-white/5"
          }`}
        >
          {buttonText}
        </button>

        <div className="space-y-4 flex-1">
          {features.map((feature, i) => (
            <div key={i} className="flex items-start gap-3">
              <Check
                className="flex-shrink-0 mt-1 text-slate-400"
                width={16}
                height={16}
              />
              <span className="text-sm text-slate-700 dark:text-slate-300 font-medium">
                {feature}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

const PricingPage: FC = () => {
  return (
    <div className="min-h-screen bg-white dark:bg-[#0d1117] py-12 px-4 sm:px-6 relative overflow-hidden transition-colors duration-300">
      <div className="max-w-7xl mx-auto relative z-10">
        <div className="text-center mb-16 space-y-4">
          <h1 className="text-4xl md:text-6xl font-black text-slate-900 dark:text-white tracking-tight">
            StudyLink Plans
          </h1>
          <p className="text-slate-600 dark:text-slate-400 text-lg max-w-2xl mx-auto font-medium">
            당신의 입시 성공을 위한 최적의 파트너. 지금 바로 시작해 보세요.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-8 lg:gap-8 items-stretch pt-6">
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

        <div className="mt-24 p-12 rounded-3xl bg-slate-50 dark:bg-white/5 border border-slate-200 dark:border-white/10 text-center">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-4">
            기업 및 기관용 요금제가 필요하신가요?
          </h2>
          <p className="text-slate-600 dark:text-slate-400 mb-8 font-medium">
            학교, 학원 등 단체 이용을 위한 맞춤형 플랜을 제안해 드립니다.
          </p>
          <button className="text-[#0969da] font-bold hover:underline">
            문의하기 &rarr;
          </button>
        </div>
      </div>
    </div>
  );
};

export default PricingPage;
