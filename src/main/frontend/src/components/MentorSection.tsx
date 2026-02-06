import { ArrowUpRight, Star } from "lucide-react";
import type { FC } from "react";

interface Mentor {
  userId: number;
  nickname?: string;
  name?: string;
  university: string;
  major: string;
  subjects?: string[];
  averageRating: number;
  profileImageUrl: string;
  quizCount: number;
  usersDTO?: {
    name: string;
    nickname: string;
  };
}

const mockMentors: Mentor[] = [];

interface CustomWindow extends Window {
  __INITIAL_DATA__?: {
    topMentors?: any[];
  };
}

const subjectMap: { [key: string]: string } = {
  korean_common: "국어 공통",
  korean_speech: "화법과 작문",
  korean_reading: "독서",
  korean_literature: "문학",
  english_common: "영어 공통",
  math_common: "수학 공통",
  math_calculus: "미적분",
  math_geometry: "기하",
  math_prob_stat: "확률과 통계",
  korean_history: "한국사",
  phys_1: "물리학 I",
  phys_2: "물리학 II",
  chem_1: "화학 I",
  chem_2: "화학 II",
  bio_1: "생명과학 I",
  bio_2: "생명과학 II",
  earth_1: "지구과학 I",
  earth_2: "지구과학 II",
  korean_language: "언어와 매체",
  math_1: "수학 I",
  math_2: "수학 II"
};

const MentorSection: FC = () => {
  // window.__INITIAL_DATA__에 데이터가 있으면 사용
  const initialData = (window as unknown as CustomWindow).__INITIAL_DATA__;
  const mentors: Mentor[] =
    initialData && initialData.topMentors && initialData.topMentors.length > 0
      ? initialData.topMentors
      : mockMentors;
  return (
    <section
      id="mentors"
      className="py-32 bg-transparent dark:bg-transparent relative border-t border-slate-200 dark:border-white/5 transition-colors duration-300"
    >
      <div className="absolute top-1/2 left-0 w-[500px] h-[500px] bg-teal-200/50 dark:bg-purple-900/10 blur-[100px] rounded-full pointer-events-none" />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="flex flex-col md:flex-row justify-between items-end mb-16 gap-6">
          <div className="max-w-xl">
            <span className="text-teal-600 dark:text-purple-400 font-semibold tracking-wider uppercase text-xs mb-3 block">
              Verified Mentors
            </span>
            <h2 className="text-3xl md:text-5xl font-bold text-slate-900 dark:text-white leading-tight">
              검증된 명문대 멘토라인
            </h2>
            <p className="mt-4 text-slate-600 dark:text-slate-400 text-lg">
              학생증 인증을 완료한 3,000명의 멘토들이
              <br />
              여러분의 합격 순간까지 함께합니다.
            </p>
          </div>
          <button 
            onClick={() => window.location.href = "/mentors"}
            className="group flex items-center gap-2 text-sm font-medium text-slate-600 dark:text-white transition-colors border border-slate-300 dark:border-white/10 px-6 py-3 rounded-full bg-white dark:bg-white/5 hover:bg-slate-100 dark:hover:bg-white/10 hover:border-slate-400 dark:hover:border-white/20"
          >
            멘토 전체보기{" "}
            <ArrowUpRight
              width={16}
              height={16}
              className="group-hover:translate-x-0.5 group-hover:-translate-y-0.5 transition-transform"
            />
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {mentors.length > 0 ? (
            mentors.map((mentor) => (
              <div
                key={mentor.userId}
                onClick={() => window.location.href = `/mentors/${mentor.userId}`}
                className="group relative bg-white/40 dark:bg-white/[0.02] backdrop-blur-xl border border-slate-200 dark:border-white/5 rounded-3xl overflow-hidden hover:border-teal-400 dark:hover:border-purple-500/30 hover:shadow-xl hover:shadow-teal-500/10 dark:hover:shadow-purple-500/10 transition-all duration-500 cursor-pointer"
              >
                <div className="absolute inset-0 z-10 bg-gradient-to-t from-slate-900/90 dark:from-[#030014] via-transparent to-transparent opacity-80 dark:opacity-90 transition-opacity duration-300" />
                <div className="relative h-96 w-full overflow-hidden">
                  <img
                    src={mentor.profileImageUrl || "/img/default_profile.png"} // 이미지 경로 수정 (하이픈 -> 언더스코어)
                    alt={mentor.nickname || mentor.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 ease-in-out filter grayscale-[30%] group-hover:grayscale-0"
                  />
                </div>
                <div className="absolute bottom-0 left-0 w-full p-6 z-20 translate-y-2 group-hover:translate-y-0 transition-transform duration-300">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <span className="text-[10px] font-bold text-teal-100 dark:text-purple-200 bg-teal-600/80 dark:bg-purple-500/20 border border-teal-400/30 dark:border-purple-500/20 px-2 py-1 rounded mb-3 inline-block backdrop-blur-md">
                        {mentor.university}
                      </span>
                      <h3 className="text-2xl font-bold text-white mb-1">
                        {mentor.usersDTO?.nickname || mentor.usersDTO?.name || mentor.nickname || mentor.name}
                      </h3>
                      <p className="text-sm text-slate-200 dark:text-slate-400 font-medium">
                        {mentor.major}
                      </p>
                    </div>
                  </div>
                  <div className="h-[1px] w-full bg-white/20 dark:bg-white/10 my-4" />
                  <div className="flex justify-between items-center">
                    <div className="flex gap-2">
                      {mentor.subjects?.slice(0, 2).map((subject) => (
                        <span
                          key={subject}
                          className="text-[10px] text-slate-300 dark:text-slate-400 border border-white/20 dark:border-white/10 px-2 py-1 rounded-full bg-white/10 dark:bg-white/5"
                        >
                          {/* 한글 매핑 적용 */}
                          {subjectMap[subject] || subject}
                        </span>
                      ))}
                    </div>
                    <div className="flex flex-col items-end gap-1">
                      <div className="flex items-center gap-1">
                        <Star
                          width={12}
                          height={12}
                          className="text-yellow-400 fill-yellow-400"
                        />
                        <span className="text-xs font-bold text-white">
                          {Number(mentor.averageRating).toFixed(1)}
                        </span>
                      </div>
                      <span className="text-[10px] text-slate-300 dark:text-slate-400">
                        퀴즈 {mentor.quizCount}개 해결
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="col-span-full py-20 text-center border-2 border-dashed border-slate-200 dark:border-white/10 rounded-3xl">
              <p className="text-slate-500 dark:text-slate-400">
                현재 활성화된 멘토가 없습니다. 데이터 연동을 확인해 주세요.
              </p>
            </div>
          )}
        </div>
      </div>
    </section>
  );
};

export default MentorSection;
