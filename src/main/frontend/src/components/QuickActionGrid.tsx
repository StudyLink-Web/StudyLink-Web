import {
  ClipboardList,
  MessageCircleQuestion,
  Bot,
  Layout,
  MapPin,
  Users,
  ArrowRight,
  Sparkles,
  Coins,
} from "lucide-react";
import type { FC } from "react";

const actions = [
  {
    title: "AI 대입 상담",
    description: "스마트 입시 컨설팅",
    icon: Bot,
    href: "/chatbot",
    color: "bg-green-600",
    lightColor: "text-green-600 dark:text-green-400",
    bgColor: "bg-green-50 dark:bg-green-500/10",
  },
  {
    title: "AI 대입 자소서",
    description: "나만의 활동으로 만드는 합격 초안",
    icon: Sparkles,
    href: "/cover-letter",
    color: "bg-purple-600",
    lightColor: "text-purple-600 dark:text-purple-400",
    bgColor: "bg-purple-50 dark:bg-purple-500/10",
  },
  {
    title: "문제 리스트",
    description: "SKY 멘토들의 실시간 풀이",
    icon: ClipboardList,
    href: "/room/list",
    color: "bg-blue-600",
    lightColor: "text-blue-600 dark:text-blue-400",
    bgColor: "bg-blue-50 dark:bg-blue-500/10",
  },
  {
    title: "나의 질문 목록",
    description: "내가 올린 질문과 답변 확인",
    icon: MessageCircleQuestion,
    href: "/room/myQuiz",
    color: "bg-blue-600",
    lightColor: "text-blue-600 dark:text-blue-400",
    bgColor: "bg-blue-50 dark:bg-blue-500/10",
  },
  {
    title: "입시지도",
    description: "내 주변 입시 정보 탐색",
    icon: MapPin,
    href: "/map",
    color: "bg-cyan-500",
    lightColor: "text-cyan-600 dark:text-cyan-400",
    bgColor: "bg-cyan-50 dark:bg-cyan-500/10",
  },
  {
    title: "대학생활 게시판",
    description: "리얼한 캠퍼스 라이프 공유",
    icon: Layout,
    href: "/board/list",
    color: "bg-green-600",
    lightColor: "text-green-600 dark:text-green-400",
    bgColor: "bg-green-50 dark:bg-green-500/10",
  },
  {
    title: "취준생 커뮤니티",
    description: "합격 그 이상의 커리어 준비",
    icon: Users,
    href: "/community/list",
    color: "bg-amber-500",
    lightColor: "text-amber-600 dark:text-amber-400",
    bgColor: "bg-amber-50 dark:bg-amber-500/10",
  },
  {
    title: "포인트 환전",
    description: "적립된 포인트를 현금으로 환전",
    icon: Coins,
    href: "/payment/exchange",
    color: "bg-rose-500",
    lightColor: "text-rose-600 dark:text-rose-400",
    bgColor: "bg-rose-50 dark:bg-rose-500/10",
  },
];

const QuickActionGrid: FC = () => {
  return (
    <section className="py-20 relative overflow-hidden bg-transparent dark:bg-transparent transition-colors duration-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {actions.map((action, index) => (
            <a
              key={index}
              href={action.href}
              className="group relative p-8 rounded-[2rem] border border-slate-200 dark:border-white/5 bg-white/40 dark:bg-white/[0.02] backdrop-blur-xl hover:bg-white/60 dark:hover:bg-white/[0.05] hover:border-teal-500/30 dark:hover:border-purple-500/30 transition-all duration-500 hover:shadow-2xl hover:shadow-teal-500/5 dark:hover:shadow-purple-500/10 no-underline hover:underline"
            >
              <div className="flex items-start justify-between mb-8">
                <div
                  className={`p-4 rounded-2xl ${action.bgColor} ${action.lightColor} group-hover:scale-110 transition-transform duration-500`}
                >
                  <action.icon width={32} height={32} />
                </div>
                <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-500 text-slate-400 dark:text-slate-500">
                  <ArrowRight width={24} height={24} />
                </div>
              </div>

              <h3
                className={`text-xl font-bold ${action.lightColor} group-hover:text-slate-900 dark:group-hover:text-white transition-colors duration-300 mb-2`}
              >
                {action.title}
              </h3>
              <p className="text-slate-600 dark:text-slate-400 text-sm leading-relaxed">
                {action.description}
              </p>

              {/* Hover highlight effect */}
              <div className="absolute inset-x-0 bottom-0 h-1 bg-gradient-to-r from-transparent via-teal-500 dark:via-purple-500 to-transparent scale-x-0 group-hover:scale-x-100 transition-transform duration-700 rounded-full" />
            </a>
          ))}
        </div>
      </div>
    </section>
  );
};

export default QuickActionGrid;
