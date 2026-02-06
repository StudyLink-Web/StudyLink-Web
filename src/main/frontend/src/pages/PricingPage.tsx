import { useState, useEffect } from "react";
import type { FC } from "react";
import { Check, ArrowUpRight, Crown, Zap, Lock } from "lucide-react";

interface FeatureItem {
  text: string;
  locked?: boolean;
}

interface PlanProps {
  name: string;
  price: string;
  description: string;
  features: (string | FeatureItem)[];
  buttonText: string;
  highlight?: boolean;
  freeTrial?: boolean;
  productId?: number;
  onClick?: (productId: number) => void;
}

interface PlanData {
  id: number;
  name: string;
  price: number;
  priceDisplay: string;
}

const PLANS: PlanData[] = [
  { id: 0, name: "Free", price: 0, priceDisplay: "Free" },
  { id: 1, name: "Standard", price: 19900, priceDisplay: "₩19,900" },
  { id: 2, name: "Premium PASS", price: 49900, priceDisplay: "₩49,900" },
];

const PlanCard: FC<PlanProps> = ({
  name,
  price,
  description,
  features,
  buttonText,
  highlight,
  freeTrial,
  productId,
  onClick,
}) => {
  return (
    <div className="relative group flex flex-col h-full perspective-1000">
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
          <button
            onClick={() => productId !== undefined && onClick?.(productId)}
            className={`relative w-full py-4 rounded-2xl font-black flex items-center justify-center gap-2 transition-all transform hover:scale-[1.02] active:scale-[0.98] overflow-hidden group/btn shadow-xl ${
              highlight
                ? "bg-slate-900 border-2 border-blue-500/20 shadow-blue-500/10"
                : "bg-slate-800 border-2 border-slate-700 shadow-black/20"
            }`}
          >
            <div className={`absolute inset-0 transition-transform duration-500 translate-y-[101%] group-hover/btn:translate-y-0 ${
              highlight 
                ? "bg-gradient-to-r from-blue-600 via-indigo-600 to-blue-700"
                : "bg-gradient-to-r from-slate-700 to-slate-600"
            }`} />
            
            <span className="relative z-10 flex items-center gap-2 text-white transition-colors duration-300">
              {buttonText}
              <ArrowUpRight width={18} height={18} className="group-hover/btn:translate-x-1 group-hover/btn:-translate-y-1 transition-transform" />
            </span>
          </button>
        </div>

        <div className="space-y-4 flex-1">
          <div className="text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2">
            What's included
          </div>
          {features.map((feature, i) => {
            const isString = typeof feature === "string";
            const text = isString ? feature : feature.text;
            const locked = !isString && feature.locked;

            return (
              <div key={i} className={`flex items-start gap-3 group/item ${locked ? "opacity-50" : ""}`}>
                <div className={`flex-shrink-0 mt-0.5 w-5 h-5 rounded-full flex items-center justify-center transition-colors ${
                  locked 
                    ? "bg-slate-200 dark:bg-white/10 text-slate-400" 
                    : highlight ? "bg-blue-500/10 text-blue-500" : "bg-slate-100 dark:bg-white/5 text-slate-400"
                }`}>
                  {locked ? <Lock width={10} height={10} /> : <Check width={12} height={12} strokeWidth={3} />}
                </div>
                <span className={`text-sm font-semibold transition-colors ${
                  locked 
                    ? "text-slate-400 line-through" 
                    : "text-slate-700 dark:text-slate-200 group-hover/item:text-slate-900 dark:group-hover/item:text-white"
                }`}>
                  {text}
                </span>
              </div>
            );
          })}
        </div>

        <div className="absolute -bottom-12 -right-12 w-32 h-32 bg-gradient-to-br from-blue-500/10 to-transparent rounded-full blur-3xl opacity-0 group-hover:opacity-100 transition-opacity duration-1000" />
      </div>
    </div>
  );
};

const PricingPage: FC = () => {
  const [selectedPlanId, setSelectedPlanId] = useState<number | null>(null);
  const [widgets, setWidgets] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(false);

  const currentUserMembership = (window as any).__INITIAL_DATA__?.user?.membership;

  // 1. 요금제 선택 시 초기 데이터 및 Toss 위젯 로드
  const handlePlanSelect = async (productId: number) => {
    if (productId === 0) {
      window.location.href = "/login";
      return;
    }

    if (!(window as any).__INITIAL_DATA__?.isAuthenticated) {
      alert("로그인이 필요한 서비스입니다.");
      window.location.href = "/login";
      return;
    }

    setSelectedPlanId(productId);
    // UI 스크롤 이동 등 추가 처리 가능
  };

  // 2. 위젯 영역 렌더링 (selectedPlanId가 변경될 때마다 실행)
  useEffect(() => {
    if (selectedPlanId === null) return;

    const renderTossWidgets = async () => {
      const TossPayments = (window as any).TossPayments;
      if (!TossPayments) return;

      const plan = PLANS.find(p => p.id === selectedPlanId);
      if (!plan) return;

      // 위젯용 클라이언트 키 ( test_gck_... )
      const clientKey = "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm";
      const tossPayments = TossPayments(clientKey);
      
      // 사용자 고유 키 (인증 데이터에서 가져오거나 임시 생성)
      const customerKey = (window as any).__INITIAL_DATA__?.userId || "ANONYMOUS_" + Math.random().toString(36).substring(7);

      const widgetsInstance = tossPayments.widgets({ customerKey });
      setWidgets(widgetsInstance);

      await widgetsInstance.setAmount({
        currency: "KRW",
        value: plan.price,
      });

      await Promise.all([
        widgetsInstance.renderPaymentMethods({
          selector: "#payment-method",
          variantKey: "DEFAULT",
        }),
        widgetsInstance.renderAgreement({
          selector: "#agreement",
          variantKey: "AGREEMENT",
        }),
      ]);
    };

    renderTossWidgets();
  }, [selectedPlanId]);

  // 3. 결제 요청 (서버에 주문 생성 후 위젯 실행)
  const handleFinalPayment = async () => {
    if (!widgets || selectedPlanId === null) return;
    setIsLoading(true);

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    try {
      console.log("Creating pending order on server...");
      const response = await fetch("/payment/pending", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
        },
        body: JSON.stringify({ productId: selectedPlanId })
      });

      if (!response.ok) throw new Error("주문 생성 실패");
      
      const data = await response.json();
      console.log("Order created:", data.orderId);

      await widgets.requestPayment({
        orderId: data.orderId,
        orderName: data.productName,
        successUrl: window.location.origin + "/payment/success",
        failUrl: window.location.origin + "/payment/fail",
      });
    } catch (err: any) {
      console.error("Payment Error:", err);
      alert(`결제 준비 중 오류가 발생했습니다. (${err.message})`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-[#030014] py-24 px-4 sm:px-6 relative overflow-hidden transition-colors duration-500">
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

          {/* 📍 [링크 2] 결제 위젯 표시 영역 (요금제 선택 시 활성화) */}
          {selectedPlanId !== null && (
            <div className="mt-12 p-8 rounded-3xl bg-white/10 border border-white/20 backdrop-blur-xl animate-fade-in text-left max-w-4xl mx-auto shadow-2xl overflow-hidden relative">
              <div className="flex justify-between items-center mb-6">
                 <h2 className="text-2xl font-black text-white">결제 내용 확인</h2>
                 <button onClick={() => setSelectedPlanId(null)} className="text-slate-400 hover:text-white transition-colors">취소</button>
              </div>
              <div className="bg-slate-900/50 rounded-2xl p-6 mb-6 border border-white/5">
                <div className="flex justify-between items-center">
                  <span className="text-slate-400 font-bold">선택한 플랜</span>
                  <span className="text-blue-400 text-lg font-black">{PLANS.find(p => p.id === selectedPlanId)?.name}</span>
                </div>
                <div className="flex justify-between items-center mt-2">
                  <span className="text-slate-400 font-bold">결제 금액</span>
                  <span className="text-white text-xl font-black">{PLANS.find(p => p.id === selectedPlanId)?.priceDisplay}</span>
                </div>
              </div>

              {/* Toss 위젯 렌더링 영역 */}
              <div id="payment-method" className="mb-4" />
              <div id="agreement" className="mb-8" />

              <button 
                onClick={handleFinalPayment}
                disabled={isLoading}
                className="w-full py-4 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-700 text-white rounded-2xl font-black text-lg transition-all transform hover:scale-[1.01] active:scale-[0.99] shadow-lg shadow-blue-500/20"
              >
                {isLoading ? "처리 중..." : "안전하게 결제하기"}
              </button>
            </div>
          )}
        </div>

        <div className="grid md:grid-cols-3 gap-8 lg:gap-10 items-stretch pt-6 animate-fade-in-up [animation-delay:600ms]">
          <PlanCard
            name="Free"
            price="Free"
            description="입시 준비의 기초 데이터와 뉴스를 확인해보세요."
            buttonText={currentUserMembership === "FREE" || !currentUserMembership ? "현재 이용 중" : "시작하기"}
            productId={0} 
            onClick={() => window.location.href = "/login"}
            highlight={currentUserMembership === "FREE" || !currentUserMembership}
            features={[
              "최신 대학 입시 뉴스 구독",
              "기본 입시 데이터 조회 (일 5회)",
              "공개 멘토링 게시판 읽기 권한",
              "입시 커뮤니티 기본 이용",
              "AI 자소서 메이커",
              { text: "AI 대입 상담 (미지원)", locked: true },
            ]}
          />

          <PlanCard
            highlight={currentUserMembership === "STANDARD"}
            name="Standard"
            price="₩19,900"
            description="합격을 부르는 AI의 명쾌한 자소서 첨삭과 예측 데이터!"
            buttonText={currentUserMembership === "STANDARD" ? "현재 이용 중" : "구독 시작하기"}
            productId={1} 
            onClick={currentUserMembership === "STANDARD" ? undefined : handlePlanSelect}
            features={[
              "AI 자소서 분석/첨삭 (월 10회)",
              "AI 대입 상담 챗봇 (월 30회)",
              "대학별 합격 예측 데이터 조회",
              "맞춤형 입시 리포트 제공",
              "프리미엄 게시판 접근 권한",
            ]}
          />

          <PlanCard
            highlight={currentUserMembership === "PREMIUM"}
            name="Premium PASS"
            price="₩49,900"
            description="완벽한 합격을 위한 모든 유료 데이터와 무제한 AI 서비스!"
            buttonText={currentUserMembership === "PREMIUM" ? "현재 이용 중" : "PASS 구매하기"}
            productId={2} 
            onClick={currentUserMembership === "PREMIUM" ? undefined : handlePlanSelect}
            features={[
              "전년도 합격자 생기부 원본 열람",
              "AI 자소서/상담/면접 무제한",
              "1:1 입시 멘토링 우선 매칭",
              "전용 Q&A 라운지 입장 권한",
              "실시간 전문 컨설턴트 상담 연동",
            ]}
          />
        </div>

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
