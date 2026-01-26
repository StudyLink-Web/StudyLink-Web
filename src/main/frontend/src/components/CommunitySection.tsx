import { MessageSquare, Users, Zap } from "lucide-react";
import type { FC } from "react";

const CommunitySection: FC = () => {
  return (
    <section className="py-32 bg-transparent dark:bg-transparent transition-colors duration-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-20">
          <h2 className="text-3xl md:text-5xl font-bold text-slate-900 dark:text-white mb-6">
            함께 성장하는 커뮤니티
          </h2>
          <p className="text-slate-600 dark:text-slate-400 text-lg max-w-2xl mx-auto">
            혼자 고민하면 짐이 되지만, 함께 나누면 길이 됩니다.
            <br />
            실시간으로 업데이트되는 입시 정보와 팁을 확인하세요.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {/* Card 1 */}
          <div className="group p-8 rounded-3xl bg-white/40 dark:bg-white/[0.02] backdrop-blur-xl border border-slate-200 dark:border-white/10 hover:border-teal-400 dark:hover:border-purple-500/30 transition-all hover:shadow-xl">
            <div className="w-12 h-12 rounded-2xl bg-teal-500/10 flex items-center justify-center mb-6">
              <MessageSquare className="text-teal-600 dark:text-purple-400" />
            </div>
            <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-4">
              실시간 질문 답변
            </h3>
            <p className="text-slate-600 dark:text-slate-200 text-sm leading-relaxed mb-6">
              모르는 문제는 바로 물어보세요. 평균 15분 이내에 명쾌한 답변이
              달립니다.
            </p>
            <div className="flex -space-x-2">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="w-8 h-8 rounded-full border-2 border-white dark:border-[#030014] bg-slate-200"
                />
              ))}
              <div className="px-2 py-1 text-[10px] items-center flex text-slate-500">
                +12명 답변 중
              </div>
            </div>
          </div>

          {/* Card 2 */}
          <div className="group p-8 rounded-3xl bg-white/40 dark:bg-white/[0.02] backdrop-blur-xl border border-slate-200 dark:border-white/10 hover:border-teal-400 dark:hover:border-purple-500/30 transition-all hover:shadow-xl">
            <div className="w-12 h-12 rounded-2xl bg-teal-500/10 flex items-center justify-center mb-6">
              <Users className="text-teal-600 dark:text-purple-400" />
            </div>
            <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-4">
              입시 메이트 찾기
            </h3>
            <p className="text-slate-600 dark:text-slate-200 text-sm leading-relaxed mb-6">
              같은 목표를 가진 친구들과 스터디 그룹을 만들고 서로 응원하며
              공부하세요.
            </p>
            <span className="text-xs font-bold text-teal-600 dark:text-purple-400">
              현재 156개의 스터디 활성화
            </span>
          </div>

          {/* Card 3 */}
          <div className="group p-8 rounded-3xl bg-white/40 dark:bg-white/[0.02] backdrop-blur-xl border border-slate-200 dark:border-white/10 hover:border-teal-400 dark:hover:border-purple-500/30 transition-all hover:shadow-xl">
            <div className="w-12 h-12 rounded-2xl bg-teal-500/10 flex items-center justify-center mb-6">
              <Zap className="text-teal-600 dark:text-purple-400" />
            </div>
            <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-4">
              합격 꿀팁 매거진
            </h3>
            <p className="text-slate-600 dark:text-slate-200 text-sm leading-relaxed mb-6">
              매주 업데이트되는 최신 입시 전형 분석과 멘토들의 합격 수기를
              만나보세요.
            </p>
            <div className="flex gap-2">
              <span className="px-2 py-1 rounded-md bg-white dark:bg-white/10 text-[10px] text-slate-500">
                #2025전형
              </span>
              <span className="px-2 py-1 rounded-md bg-white dark:bg-white/10 text-[10px] text-slate-500">
                #학종꿀팁
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default CommunitySection;
