import { useState, useEffect } from "react";
import { ArrowLeft, Star, Heart, CheckCircle, GraduationCap, BookOpen, Clock, MapPin, DollarSign } from "lucide-react";

interface MentorProfile {
  userId: number;
  usersDTO: {
    name: string;
    nickname: string;
    profileImageUrl: string;
  };
  university: string;
  major: string;
  introduction: string;
  averageRating: number;
  reviewCount: number;
  lessonCount: number;
  isVerified: boolean;
  
  // Academic
  entranceYear?: number;
  graduationYear?: number;
  credentials?: string;
  
  // Lesson
  subjects?: string[];
  grades?: string[];
  lessonType?: string;
  lessonLocation?: string;
  availableTime?: string;
  pricePerHour?: number;
  minLessonHours?: number;
}

interface CustomWindow extends Window {
  __INITIAL_DATA__?: {
    user?: {
      userId: number;
    };
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

const MentorDetailPage = () => {
  const [mentor, setMentor] = useState<MentorProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("info");
  const [isFavored, setIsFavored] = useState(false);
  
  // URL에서 ID 추출 (React Router 없이 window.location 사용)
  const pathParts = window.location.pathname.split("/");
  const mentorId = pathParts[pathParts.length - 1];

  const currentUser = (window as unknown as CustomWindow).__INITIAL_DATA__?.user;

  useEffect(() => {
    fetchMentorProfile();
    if (currentUser) {
      checkFavoriteStatus();
    }
  }, [mentorId]);

  const fetchMentorProfile = async () => {
    try {
      const response = await fetch(`/api/mentor-profiles/${mentorId}`);
      const result = await response.json();
      if (result.success) {
        console.log("Loaded mentor data:", result.data); // 디버깅용 로그
        setMentor(result.data);
      } else {
        console.error("Failed response:", result);
      }
    } catch (error) {
      console.error("Failed to load mentor profile:", error);
    } finally {
      setLoading(false);
    }
  };

  const checkFavoriteStatus = async () => {
    if (!currentUser) return;
    try {
      // currentUser.userId가 없는 경우 방어
      if (!currentUser.userId) return;
      
      const response = await fetch(`/api/favorites/check/${currentUser.userId}/${mentorId}`);
      const result = await response.json();
      if (result.success) {
        setIsFavored(result.is_favored);
      }
    } catch (error) {
      console.error("Failed to check favorite status:", error);
    }
  };

  const toggleFavorite = async () => {
    if (!currentUser) {
      alert("로그인이 필요한 서비스입니다.");
      return;
    }

    try {
      let response;
      if (isFavored) {
        // 즐겨찾기 ID를 모르므로, API가 삭제 로직을 좀 더 유연하게 처리하거나 
        // 혹은 check API가 ID를 반환해줘야 함. 
        // 현재 FavoriteController 로직상 /remove/{id} 이므로 ID가 필요함.
        // 하지만 여기서는 편의상 add/remove 토글을 위해 studentId, mentorId를 받는 endpoint가 없으므로
        // 일단 add만 구현하거나, check시 id를 받아야 함.
        // FavoriteController 분석 결과: remove는 favoriteId가 필요함.
        // check API는 is_favored Boolean만 반환함.
        // 따라서 정확한 구현을 위해선 check API가 favoriteId도 반환하도록 수정하거나, 
        // removeByUserAndMentor 같은 API가 필요함. 
        // 일단 현재는 Add만 연동하고 Remove는 alert로 처리 또는 추가 로직 구현 필요.
        // (사용자 요청: 찜하기 연동) -> Add라도 우선 구현.
        
        // *임시*: 이미 찜한 상태라면 해제 불가 메시지 (또는 컨트롤러 수정 필요)
        // 여기서는 UX를 위해 "찜하기 취소는 마이페이지에서 가능합니다" 등으로 처리하거나
        // check API를 수정하는 것이 좋음.
        alert("관심 멘토 취소는 마이페이지에서 관리할 수 있습니다.");
        return; 
      } else {
        const formData = new FormData();
        formData.append("studentId", currentUser.userId.toString());
        formData.append("mentorId", mentorId);
        
        response = await fetch("/api/favorites/add", {
          method: "POST",
          body: formData
        });
      }

      const result = await response.json();
      if (result.success) {
        setIsFavored(true);
      }
    } catch (error) {
      console.error("Failed to toggle favorite:", error);
    }
  };

  const handleApply = () => {
    alert("준비 중입니다.");
  };

  const handleBack = () => {
    window.location.href = "/mentors";
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-[#030014]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-500"></div>
      </div>
    );
  }

  if (!mentor) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-slate-50 dark:bg-[#030014] gap-4">
        <p className="text-slate-500">멘토 정보를 찾을 수 없습니다.</p>
        <button onClick={handleBack} className="text-teal-500 hover:underline">돌아가기</button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-[#030014] pb-24 transition-colors duration-300">
      {/* Header Image Background (Blur) */}
      <div className="relative h-64 w-full overflow-hidden bg-slate-900">
        <img 
          src={mentor.usersDTO?.profileImageUrl || "/img/default_profile.png"} 
          className="w-full h-full object-cover opacity-50 blur-xl scale-110"
          alt="background"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#030014] to-transparent"></div>
        
        <div className="absolute top-0 left-0 w-full p-4 flex justify-between items-center z-10">
          <button 
            onClick={handleBack}
            className="p-2 bg-white/10 backdrop-blur-md rounded-full text-white hover:bg-white/20 transition-colors"
          >
            <ArrowLeft width={24} height={24} />
          </button>
        </div>
      </div>

      {/* Main Profile Card */}
      <div className="max-w-4xl mx-auto px-4 -mt-32 relative z-10">
        <div className="bg-white dark:bg-[#1a1625] rounded-3xl p-6 shadow-xl border border-slate-200 dark:border-white/5">
          <div className="flex flex-col md:flex-row gap-6 items-start">
            {/* Profile Image */}
            <div className="relative w-32 h-32 md:w-40 md:h-40 rounded-full border-4 border-white dark:border-[#1a1625] shadow-lg overflow-hidden flex-shrink-0 mx-auto md:mx-0">
              <img 
                src={mentor.usersDTO?.profileImageUrl || "/img/default_profile.png"} 
                alt="profile" 
                className="w-full h-full object-cover"
              />
            </div>

            {/* Basic Info */}
            <div className="flex-1 text-center md:text-left">
              <div className="flex flex-col md:flex-row items-center md:items-start gap-2 mb-2">
                <span className="text-teal-600 dark:text-teal-400 font-bold bg-teal-50 dark:bg-teal-900/30 px-3 py-1 rounded-full text-xs box-border border border-teal-100 dark:border-teal-800">
                  {mentor.university}
                </span>
                {mentor.isVerified && (
                  <span className="flex items-center gap-1 text-blue-500 bg-blue-50 dark:bg-blue-900/30 px-3 py-1 rounded-full text-xs font-medium border border-blue-100 dark:border-blue-800">
                    <CheckCircle width={12} height={12} /> 인증됨
                  </span>
                )}
              </div>
              
              <h1 className="text-3xl font-bold text-slate-900 dark:text-white mb-2">
                {mentor.usersDTO?.nickname || mentor.usersDTO?.name || "알 수 없는 사용자"}
              </h1>
              
              <p className="text-slate-600 dark:text-slate-400 mb-4 text-lg">
                {mentor.major}
              </p>

              <div className="flex items-center justify-center md:justify-start gap-6 text-sm">
                <div className="flex flex-col items-center md:items-start">
                  <div className="flex items-center gap-1 text-yellow-500 font-bold text-lg">
                    <Star width={18} height={18} fill="currentColor" />
                    {Number(mentor.averageRating).toFixed(1)}
                  </div>
                  <span className="text-slate-400 text-xs">평점</span>
                </div>
                <div className="w-px h-8 bg-slate-200 dark:bg-white/10"></div>
                <div className="flex flex-col items-center md:items-start">
                  <span className="font-bold text-slate-900 dark:text-white text-lg">{mentor.lessonCount}회</span>
                  <span className="text-slate-400 text-xs">수업 진행</span>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-8 border-t border-slate-100 dark:border-white/5 pt-6">
            <h3 className="text-sm font-bold text-slate-400 mb-2 uppercase tracking-wider">Introduction</h3>
            <p className="text-slate-700 dark:text-slate-300 leading-relaxed whitespace-pre-wrap">
              {mentor.introduction || "아직 자기소개가 입력되지 않았습니다."}
            </p>
          </div>
        </div>

        {/* Tabs */}
        <div className="flex gap-4 mt-8 mb-6 overflow-x-auto pb-2 scrollbar-hide">
          {["info", "lesson", "reviews"].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-6 py-3 rounded-full font-bold transition-all whitespace-nowrap ${
                activeTab === tab
                  ? "bg-slate-900 dark:bg-white text-white dark:text-slate-900 shadow-lg scale-105"
                  : "bg-white dark:bg-white/5 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-white/10"
              }`}
            >
              {tab === "info" && "학력/자격"}
              {tab === "lesson" && "수업 정보"}
              {tab === "reviews" && "수강 후기"}
            </button>
          ))}
        </div>

        {/* Tab Content */}
        <div className="space-y-6">
          {activeTab === "info" && (
            <div className="bg-white dark:bg-[#1a1625] rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200 dark:border-white/5 animate-fadeIn">
              <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-6 flex items-center gap-2">
                <GraduationCap className="text-teal-500" /> 학력 및 자격사항
              </h3>
              
              <div className="space-y-6">
                <div>
                  <label className="block text-sm font-medium text-slate-400 mb-2">🎓 최종 학력</label>
                  <p className="text-lg text-slate-800 dark:text-slate-200 font-medium">
                    {mentor.university} {mentor.major}
                  </p>
                  <p className="text-sm text-slate-500 mt-1">
                    {mentor.entranceYear ? `${mentor.entranceYear}학번` : ""} 
                    {mentor.graduationYear ? ` (졸업: ${mentor.graduationYear}년)` : " (재학 중)"}
                  </p>
                </div>

                {mentor.credentials && (
                  <div>
                    <label className="block text-sm font-medium text-slate-400 mb-2">🏆 주요 수상 및 자격</label>
                    <ul className="list-disc list-inside text-slate-700 dark:text-slate-300 space-y-2">
                      {mentor.credentials.split("\n").map((cred, idx) => (
                        <li key={idx}>{cred}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === "lesson" && (
            <div className="bg-white dark:bg-[#1a1625] rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200 dark:border-white/5 animate-fadeIn">
              <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-6 flex items-center gap-2">
                <BookOpen className="text-teal-500" /> 수업 상세 정보
              </h3>

              <div className="grid md:grid-cols-2 gap-8">
                <div>
                  <h4 className="font-bold text-slate-700 dark:text-slate-300 mb-3">수업 과목</h4>
                  <div className="flex flex-wrap gap-2">
                    {mentor.subjects?.map((sub) => (
                      <span key={sub} className="bg-teal-50 dark:bg-teal-900/30 text-teal-600 dark:text-teal-400 px-3 py-1 rounded-lg text-sm font-medium border border-teal-100 dark:border-teal-800">
                        {subjectMap[sub] || sub}
                      </span>
                    )) || <span className="text-slate-400">등록된 과목이 없습니다.</span>}
                  </div>
                </div>

                <div>
                  <h4 className="font-bold text-slate-700 dark:text-slate-300 mb-3">수업 대상</h4>
                  <div className="flex flex-wrap gap-2">
                    {mentor.grades?.map((grade) => (
                      <span key={grade} className="bg-purple-50 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 px-3 py-1 rounded-lg text-sm font-medium border border-purple-100 dark:border-purple-800">
                        {grade}
                      </span>
                    )) || <span className="text-slate-400">등록된 대상이 없습니다.</span>}
                  </div>
                </div>
              </div>

              <div className="h-px bg-slate-100 dark:bg-white/5 my-6"></div>

              <div className="grid sm:grid-cols-3 gap-6">
                <div className="bg-slate-50 dark:bg-white/5 p-4 rounded-xl">
                  <div className="flex items-center gap-2 text-slate-400 mb-2">
                    <DollarSign width={16} /> <span className="text-xs font-bold uppercase">수업료</span>
                  </div>
                  <p className="text-lg font-bold text-slate-800 dark:text-slate-200">
                     {mentor.pricePerHour ? `${mentor.pricePerHour.toLocaleString()}원` : "협의"}
                     <span className="text-xs font-normal text-slate-500 ml-1">/ 시간</span>
                  </p>
                </div>
                
                <div className="bg-slate-50 dark:bg-white/5 p-4 rounded-xl">
                  <div className="flex items-center gap-2 text-slate-400 mb-2">
                    <MapPin width={16} /> <span className="text-xs font-bold uppercase">장소</span>
                  </div>
                  <p className="text-lg font-bold text-slate-800 dark:text-slate-200">
                    {mentor.lessonLocation || "온라인/오프라인 협의"}
                  </p>
                </div>

                <div className="bg-slate-50 dark:bg-white/5 p-4 rounded-xl">
                  <div className="flex items-center gap-2 text-slate-400 mb-2">
                    <Clock width={16} /> <span className="text-xs font-bold uppercase">진행 방식</span>
                  </div>
                  <p className="text-lg font-bold text-slate-800 dark:text-slate-200">
                    {mentor.lessonType === "ON_OFF" ? "온/오프라인 병행" : mentor.lessonType || "협의"}
                  </p>
                </div>
              </div>

              {mentor.availableTime && (
                <div className="mt-6 bg-yellow-50 dark:bg-yellow-900/10 p-4 rounded-xl border border-yellow-100 dark:border-yellow-900/20">
                  <h5 className="text-sm font-bold text-yellow-700 dark:text-yellow-500 mb-1 flex items-center gap-2">
                    <Clock width={14} /> 수업 가능 시간
                  </h5>
                  <p className="text-slate-700 dark:text-slate-300 text-sm">
                    {mentor.availableTime}
                  </p>
                </div>
              )}
            </div>
          )}

          {activeTab === "reviews" && (
            <div className="bg-white dark:bg-[#1a1625] rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200 dark:border-white/5 text-center py-20">
              <p className="text-slate-400">아직 작성된 후기가 없습니다.</p>
            </div>
          )}
        </div>
      </div>

      {/* Sticky Action Bar */}
      <div className="fixed bottom-0 left-0 w-full bg-white dark:bg-[#1a1625] border-t border-slate-200 dark:border-white/5 p-4 z-50 safe-area-bottom">
        <div className="max-w-4xl mx-auto flex gap-4">
          <button 
            onClick={toggleFavorite}
            className={`flex flex-col items-center justify-center min-w-[80px] rounded-2xl transition-all ${
              isFavored 
                ? "text-red-500 bg-red-50 dark:bg-red-900/20" 
                : "text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
            }`}
          >
            <Heart fill={isFavored ? "currentColor" : "none"} />
            <span className="text-[10px] font-bold mt-1">찜하기</span>
          </button>
          
          <button 
            onClick={handleApply}
            className="flex-1 bg-teal-600 hover:bg-teal-700 text-white font-bold text-lg rounded-2xl py-3 shadow-lg shadow-teal-500/30 transition-all active:scale-95"
          >
            수업 신청하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default MentorDetailPage;
