import { useState, useEffect } from "react";
import { ArrowLeft, Star, Search, Filter } from "lucide-react";

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
  introduction?: string;
  usersDTO?: {
    name: string;
    nickname: string;
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

const MentorListPage = () => {
  const [mentors, setMentors] = useState<Mentor[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    fetchMentors();
  }, []);

  const fetchMentors = async () => {
    try {
      const response = await fetch("/api/mentor-profiles/verified/list");
      const result = await response.json();
      if (result.success) {
        setMentors(result.data);
      }
    } catch (error) {
      console.error("멘토 목록 로딩 실패:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleBack = () => {
    window.location.href = "/";
  };

  const filteredMentors = mentors.filter((m) =>
    (m.usersDTO?.nickname || m.usersDTO?.name || m.nickname || m.name || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
    (m.university || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
    (m.major || "").toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-[#030014] transition-colors duration-300">
      {/* Header */}
      <header className="sticky top-0 z-30 bg-white/80 dark:bg-[#030014]/80 backdrop-blur-md border-b border-slate-200 dark:border-white/5">
        <div className="max-w-7xl mx-auto px-4 h-20 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={handleBack}
              className="p-2 hover:bg-slate-100 dark:hover:bg-white/5 rounded-full transition-colors text-slate-600 dark:text-slate-400"
            >
              <ArrowLeft width={24} height={24} />
            </button>
            <h1 className="text-xl font-bold text-slate-900 dark:text-white">
              검증된 멘토 라인업
            </h1>
          </div>
          <div className="hidden md:flex items-center gap-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" width={18} height={18} />
              <input
                type="text"
                placeholder="멘토명, 학교, 전공 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 bg-slate-100 dark:bg-white/5 border-none rounded-full w-64 focus:ring-2 focus:ring-teal-500 transition-all text-sm"
              />
            </div>
            <button className="p-2 bg-slate-100 dark:bg-white/5 rounded-full text-slate-600 dark:text-slate-400">
              <Filter width={20} height={20} />
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-12">
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {[1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
              <div key={n} className="h-[450px] bg-slate-200 dark:bg-white/5 animate-pulse rounded-3xl" />
            ))}
          </div>
        ) : (
          <>
            <div className="mb-8 md:hidden">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" width={18} height={18} />
                <input
                  type="text"
                  placeholder="멘토 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10 pr-4 py-3 bg-white dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-2xl w-full focus:ring-2 focus:ring-teal-500 transition-all"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {filteredMentors.length > 0 ? (
                filteredMentors.map((mentor) => (
                  <div
                    key={mentor.userId}
                    onClick={() => window.location.href = `/mentors/${mentor.userId}`}
                    className="group bg-white dark:bg-white/[0.02] border border-slate-200 dark:border-white/5 rounded-3xl overflow-hidden hover:border-teal-400 dark:hover:border-purple-500/30 transition-all duration-300 cursor-pointer"
                  >
                    <div className="relative h-64 overflow-hidden">
                      <img
                        src={mentor.profileImageUrl || "/img/default_profile.png"} // 이미지 경로 수정
                        alt={mentor.usersDTO?.nickname || mentor.usersDTO?.name || mentor.nickname || mentor.name}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                      />
                      <div className="absolute top-4 left-4">
                        <span className="text-[10px] font-bold text-teal-100 bg-teal-600/90 border border-teal-400/30 px-2 py-1 rounded backdrop-blur-md">
                          {mentor.university}
                        </span>
                      </div>
                    </div>
                    <div className="p-6">
                      <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-1">
                        {mentor.usersDTO?.nickname || mentor.usersDTO?.name || mentor.nickname || mentor.name}
                      </h3>
                      <p className="text-sm text-slate-500 dark:text-slate-400 mb-4">
                        {mentor.major}
                      </p>
                      
                      <div className="flex flex-wrap gap-2 mb-4">
                        {mentor.subjects?.slice(0, 3).map((s) => (
                          <span key={s} className="text-[10px] bg-slate-100 dark:bg-white/5 text-slate-600 dark:text-slate-400 px-2 py-1 rounded-full">
                            #{subjectMap[s] || s}
                          </span>
                        ))}
                      </div>

                      <div className="flex justify-between items-center pt-4 border-t border-slate-100 dark:border-white/5">
                        <div className="flex items-center gap-1">
                          <Star className="text-yellow-400 fill-yellow-400" width={14} height={14} />
                          <span className="text-sm font-bold text-slate-900 dark:text-white">{Number(mentor.averageRating).toFixed(1)}</span>
                        </div>
                        <span className="text-xs text-slate-500 dark:text-slate-400">
                          퀴즈 {mentor.quizCount}개 해결
                        </span>
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <div className="col-span-full py-32 text-center text-slate-500">
                  검색 결과가 없습니다.
                </div>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
};

export default MentorListPage;
