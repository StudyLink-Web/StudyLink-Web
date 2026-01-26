import React, { useState, useEffect } from "react";
import {
  Sparkles,
  Save,
  Trash2,
  Plus,
  ArrowLeft,
  Loader2,
  Clipboard,
  FileText,
} from "lucide-react";

interface CoverLetter {
  coverLetterId: number;
  title: string;
  questionNum: number;
  content: string;
  targetUniversity: string;
  targetMajor: string;
  createdAt: string;
}

const AdmissionEssayPage: React.FC = () => {
  const [view, setView] = useState<"list" | "create" | "edit">("list");
  const [essays, setEssays] = useState<CoverLetter[]>([]);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);

  // Form State
  const [formData, setFormData] = useState({
    title: "",
    questionNum: 1,
    questionText:
      "고등학교 재학 기간 중 자신의 진로와 관련하여 어떤 노력을 해왔는지 본인에게 의미 있는 학습 경험과 교내 활동을 중심으로 기술해 주시기 바랍니다. (1500자 이내)",
    keywords: [] as string[],
    keywordInput: "",
    tone: "논리적인",
    targetUniversity: "",
    targetMajor: "",
  });

  const [generatedContent, setGeneratedContent] = useState("");

  // Student Record Extraction State
  const [isExtractModalOpen, setIsExtractModalOpen] = useState(false);
  const [recordText, setRecordText] = useState("");
  const [isExtracting, setIsExtracting] = useState(false);
  const [extractedResult, setExtractedResult] = useState<{
    keywords: string[];
    suggestedTitle: string;
    summary: string;
  } | null>(null);

  useEffect(() => {
    const initData = async () => {
      await Promise.all([fetchEssays(), fetchProfile()]);
    };
    initData();
  }, []);

  const fetchEssays = async () => {
    setLoading(true);
    try {
      const resp = await fetch("/api/cover-letter/list");
      if (resp.ok) {
        const data = await resp.json();
        setEssays(data);
      }
    } catch {
      console.error("자소서 목록 로드 실패");
    } finally {
      setLoading(false);
    }
  };

  const fetchProfile = async () => {
    try {
      const resp = await fetch("/api/dashboard/data");
      if (resp.ok) {
        const data = await resp.json();
        if (data.profile) {
          setFormData((prev) => ({
            ...prev,
            targetUniversity: data.profile.targetUniversity || "",
            targetMajor: data.profile.targetMajor || "",
          }));
        }
      }
    } catch {
      console.error("프로필 정보 로드 실패");
    }
  };

  const handleAddKeyword = () => {
    if (
      formData.keywordInput.trim() &&
      !formData.keywords.includes(formData.keywordInput.trim())
    ) {
      setFormData({
        ...formData,
        keywords: [...formData.keywords, formData.keywordInput.trim()],
        keywordInput: "",
      });
    }
  };

  const removeKeyword = (kw: string) => {
    setFormData({
      ...formData,
      keywords: formData.keywords.filter((k) => k !== kw),
    });
  };

  const generateAI = async () => {
    setGenerating(true);
    try {
      const resp = await fetch("/api/cover-letter/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });
      if (resp.ok) {
        const data = await resp.json();
        setGeneratedContent(data.content);
      }
    } catch {
      alert("AI 생성 중 오류가 발생했습니다.");
    } finally {
      setGenerating(false);
    }
  };

  const saveEssay = async () => {
    try {
      const resp = await fetch("/api/cover-letter/save", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          request: formData,
          content: generatedContent,
        }),
      });
      if (resp.ok) {
        alert("자소서가 저장되었습니다.");
        setView("list");
        fetchEssays();
      }
    } catch {
      alert("저장 중 오류가 발생했습니다.");
    }
  };

  const handleExtractRecord = async () => {
    if (!recordText.trim()) return alert("생기부 내용을 입력해주세요.");
    setIsExtracting(true);
    try {
      const resp = await fetch("/api/cover-letter/extract", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ rawText: recordText }),
      });
      if (resp.ok) {
        const data = await resp.json();
        setExtractedResult(data);
      }
    } catch {
      alert("데이터 추출 중 오류가 발생했습니다.");
    } finally {
      setIsExtracting(false);
    }
  };

  const applyExtractedData = () => {
    if (!extractedResult) return;
    setFormData({
      ...formData,
      keywords: [
        ...new Set([...formData.keywords, ...extractedResult.keywords]),
      ],
      title: formData.title || extractedResult.suggestedTitle,
    });
    setIsExtractModalOpen(false);
    setRecordText("");
    setExtractedResult(null);
    alert("키워드와 제목이 적용되었습니다.");
  };

  const deleteEssay = async (id: number) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      const resp = await fetch(`/api/cover-letter/${id}`, { method: "DELETE" });
      if (resp.ok) {
        fetchEssays();
      }
    } catch {
      alert("삭제 실패");
    }
  };

  return (
    <div className="min-h-screen bg-white dark:bg-[#030014] pt-24 pb-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 dark:text-white flex items-center gap-3">
              <Sparkles className="text-purple-500" />
              AI 대입 자소서 메이커
            </h1>
            <p className="text-slate-600 dark:text-slate-400 mt-2">
              나의 경험 키워드를 바탕으로 합격 자소서 초안을 만듭니다.
            </p>
          </div>
          {view === "list" ? (
            <button
              onClick={() => setView("create")}
              style={{ backgroundColor: "#4f46e5" }}
              className="px-6 py-3 text-white rounded-2xl font-bold flex items-center gap-2 transition-all shadow-lg shadow-indigo-500/20 hover:opacity-90 active:scale-95"
            >
              <Plus size={20} color="white" />
              <span className="text-white">새 자소서 작성</span>
            </button>
          ) : (
            <button
              onClick={() => setView("list")}
              className="text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white flex items-center gap-2"
            >
              <ArrowLeft size={20} /> 목록으로
            </button>
          )}
        </div>

        {/* Main Content */}
        {view === "list" ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {loading ? (
              <div className="col-span-full flex justify-center py-20">
                <Loader2 className="animate-spin text-purple-500" size={40} />
              </div>
            ) : essays.length === 0 ? (
              <div className="col-span-full text-center py-20 bg-white dark:bg-white/5 rounded-[2rem] border border-slate-200 dark:border-white/10">
                <FileText
                  className="mx-auto text-slate-300 dark:text-slate-700 mb-4"
                  size={60}
                />
                <p className="text-slate-500">
                  저장된 자소서가 없습니다. 첫 초안을 만들어보세요!
                </p>
              </div>
            ) : (
              essays.map((essay) => (
                <div
                  key={essay.coverLetterId}
                  className="p-6 bg-white dark:bg-white/5 rounded-[2rem] border border-slate-200 dark:border-white/10 hover:shadow-lg transition-all group"
                >
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-xl font-bold text-slate-900 dark:text-white">
                      {essay.title}
                    </h3>
                    <button
                      onClick={() => deleteEssay(essay.coverLetterId)}
                      className="text-slate-400 hover:text-red-500 transition-colors"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                  <div className="space-y-2 mb-6 text-sm text-slate-500">
                    <p>지원 대학: {essay.targetUniversity}</p>
                    <p>지원 학과: {essay.targetMajor}</p>
                    <p>
                      작성일: {new Date(essay.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <button
                    onClick={() => {
                      setGeneratedContent(essay.content);
                      setFormData({
                        ...formData,
                        title: essay.title,
                        targetUniversity: essay.targetUniversity,
                        targetMajor: essay.targetMajor,
                        questionNum: essay.questionNum,
                      });
                      setView("edit");
                    }}
                    className="w-full py-2 bg-slate-100 dark:bg-white/10 hover:bg-purple-100 dark:hover:bg-purple-500/20 text-slate-600 dark:text-slate-300 rounded-xl transition-all"
                  >
                    내용 보기
                  </button>
                </div>
              ))
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Input Section */}
            <div className="space-y-6">
              <div className="p-6 bg-white dark:bg-white/5 rounded-[2rem] border border-slate-200 dark:border-white/10 shadow-sm">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
                    1. 정보 및 키워드 입력
                  </h2>
                  <button
                    onClick={() => setIsExtractModalOpen(true)}
                    className="text-xs font-bold text-purple-600 dark:text-purple-400 bg-purple-50 dark:bg-purple-500/10 px-3 py-1.5 rounded-lg border border-purple-200 dark:border-purple-500/20 hover:bg-purple-100 transition-all"
                  >
                    ✨ 생기부로 키워드 생성
                  </button>
                </div>

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
                      자소서 제목
                    </label>
                    <input
                      type="text"
                      className="w-full px-4 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-slate-50 dark:bg-white/5 text-slate-900 dark:text-white focus:ring-2 focus:ring-purple-500 transition-all outline-none"
                      placeholder="예: 서울대 화공과 1차"
                      value={formData.title}
                      onChange={(e) =>
                        setFormData({ ...formData, title: e.target.value })
                      }
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
                        지원 대학교
                      </label>
                      <input
                        type="text"
                        className="w-full px-4 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-slate-50 dark:bg-white/5 text-slate-900 dark:text-white focus:ring-2 focus:ring-purple-500 outline-none"
                        value={formData.targetUniversity}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            targetUniversity: e.target.value,
                          })
                        }
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
                        지원 학과
                      </label>
                      <input
                        type="text"
                        className="w-full px-4 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-slate-50 dark:bg-white/5 text-slate-900 dark:text-white focus:ring-2 focus:ring-purple-500 outline-none"
                        value={formData.targetMajor}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            targetMajor: e.target.value,
                          })
                        }
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
                      문항 내용
                    </label>
                    <textarea
                      className="w-full px-4 py-2 h-24 rounded-xl border border-slate-200 dark:border-white/10 bg-slate-50 dark:bg-white/5 text-slate-900 dark:text-white focus:ring-2 focus:ring-purple-500 outline-none resize-none"
                      value={formData.questionText}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          questionText: e.target.value,
                        })
                      }
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
                      경험 키워드 (활동 태그)
                    </label>
                    <div className="flex gap-2 mb-2">
                      <input
                        type="text"
                        className="flex-1 px-4 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-slate-50 dark:bg-white/5 text-slate-900 dark:text-white outline-none"
                        placeholder="예: 학생회장, 과학 캠프 등"
                        value={formData.keywordInput}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            keywordInput: e.target.value,
                          })
                        }
                        onKeyPress={(e) =>
                          e.key === "Enter" && handleAddKeyword()
                        }
                      />
                      <button
                        onClick={handleAddKeyword}
                        className="px-4 py-2 bg-slate-200 dark:bg-white/10 text-slate-700 dark:text-white rounded-xl"
                      >
                        추가
                      </button>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {formData.keywords.map((kw) => (
                        <span
                          key={kw}
                          className="px-3 py-1 bg-purple-100 dark:bg-purple-500/20 text-purple-700 dark:text-purple-300 rounded-full text-xs flex items-center gap-1"
                        >
                          {kw}{" "}
                          <button
                            onClick={() => removeKeyword(kw)}
                            className="hover:text-red-500"
                          >
                            ×
                          </button>
                        </span>
                      ))}
                    </div>
                  </div>

                  <button
                    disabled={generating}
                    onClick={generateAI}
                    style={{
                      backgroundColor: generating ? "#9ca3af" : "#4f46e5",
                    }}
                    className="w-full py-4 text-white rounded-2xl font-bold flex items-center justify-center gap-2 shadow-lg shadow-purple-500/20 disabled:cursor-not-allowed transition-all"
                  >
                    {generating ? (
                      <Loader2 className="animate-spin" color="white" />
                    ) : (
                      <Sparkles size={20} color="white" />
                    )}
                    <span className="text-white">
                      {generating
                        ? "AI가 자소서를 집필 중입니다..."
                        : "AI 초안 생성하기"}
                    </span>
                  </button>
                </div>
              </div>
            </div>

            {/* Preview Section */}
            <div className="space-y-6">
              <div className="p-6 bg-white dark:bg-white/5 rounded-[2rem] border border-slate-200 dark:border-white/10 shadow-sm h-full flex flex-col">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
                    2. 작성 결과 프리뷰
                  </h2>
                  <div className="flex gap-2">
                    <button
                      onClick={() => {
                        navigator.clipboard.writeText(generatedContent);
                        alert("복사되었습니다!");
                      }}
                      className="p-2 text-slate-400 hover:text-slate-900 dark:hover:text-white transition-colors"
                      title="복사하기"
                    >
                      <Clipboard size={18} />
                    </button>
                    <button
                      onClick={saveEssay}
                      className="p-2 text-slate-400 hover:text-green-500 transition-colors"
                      title="저장하기"
                    >
                      <Save size={18} />
                    </button>
                  </div>
                </div>

                <div className="flex-1 bg-slate-50 dark:bg-black/20 rounded-2xl p-6 overflow-y-auto text-slate-800 dark:text-slate-200 leading-relaxed whitespace-pre-wrap min-h-[400px]">
                  {generatedContent || (
                    <div className="h-full flex flex-col items-center justify-center text-slate-400">
                      <Bot className="mb-4 opacity-20" size={60} />
                      <p>왼쪽 정보를 입력하고 생성 버튼을 눌러주세요.</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Extraction Modal */}
        {isExtractModalOpen && (
          <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-300">
            <div className="bg-white dark:bg-[#0d1117] w-full max-w-2xl rounded-[2.5rem] shadow-2xl border border-slate-200 dark:border-white/10 overflow-hidden flex flex-col max-h-[90vh]">
              <div className="p-8 border-b border-slate-100 dark:border-white/5 flex items-center justify-between">
                <div>
                  <h2 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
                    <Sparkles className="text-purple-500" size={24} />
                    생기부 키워드 추출기
                  </h2>
                  <p className="text-sm text-slate-500 mt-1">
                    생기부 원문을 붙여넣으시면 AI가 핵심 경험을 찾아드립니다.
                  </p>
                </div>
                <button
                  onClick={() => setIsExtractModalOpen(false)}
                  className="p-2 text-slate-400 hover:text-slate-600 dark:hover:text-white transition-colors"
                >
                  ✕
                </button>
              </div>

              <div className="p-8 flex-1 overflow-y-auto space-y-6">
                {!extractedResult ? (
                  <div className="space-y-4">
                    <label className="block text-sm font-bold text-slate-700 dark:text-slate-300">
                      생기부 원문(세특/창체 등) 입력
                    </label>
                    <textarea
                      className="w-full h-64 p-4 rounded-3xl border border-slate-200 dark:border-white/10 bg-slate-50 dark:bg-black/20 text-slate-900 dark:text-white focus:ring-2 focus:ring-purple-500 outline-none resize-none transition-all"
                      placeholder="나이스(NEIS) 등에서 복사한 내용을 여기에 붙여넣으세요..."
                      value={recordText}
                      onChange={(e) => setRecordText(e.target.value)}
                    />
                    <button
                      disabled={isExtracting}
                      onClick={handleExtractRecord}
                      className="w-full py-4 bg-slate-900 dark:bg-white text-white dark:text-black rounded-2xl font-bold flex items-center justify-center gap-2 disabled:opacity-50 transition-all hover:scale-[1.01]"
                    >
                      {isExtracting ? (
                        <>
                          <Loader2 className="animate-spin" size={20} />
                          분석 중...
                        </>
                      ) : (
                        "키워드 추출 시작하기"
                      )}
                    </button>
                  </div>
                ) : (
                  <div className="space-y-6 animate-in slide-in-from-bottom-4 duration-500">
                    <div className="bg-purple-50 dark:bg-purple-500/5 p-6 rounded-3xl border border-purple-100 dark:border-purple-500/10">
                      <h3 className="text-sm font-bold text-purple-700 dark:text-purple-300 mb-2">
                        활동 요약
                      </h3>
                      <p className="text-sm text-slate-700 dark:text-slate-300 leading-relaxed italic">
                        "{extractedResult.summary}"
                      </p>
                    </div>

                    <div>
                      <h3 className="text-sm font-bold text-slate-700 dark:text-slate-300 mb-3">
                        추출된 키워드
                      </h3>
                      <div className="flex flex-wrap gap-2">
                        {extractedResult.keywords.map((kw, i) => (
                          <span
                            key={i}
                            className="px-4 py-2 bg-white dark:bg-white/5 border border-slate-200 dark:border-white/10 text-slate-700 dark:text-slate-300 rounded-2xl text-xs font-bold shadow-sm"
                          >
                            #{kw}
                          </span>
                        ))}
                      </div>
                    </div>

                    <div className="pt-4 flex gap-3">
                      <button
                        onClick={() => setExtractedResult(null)}
                        className="flex-1 py-4 bg-slate-100 dark:bg-white/5 text-slate-600 dark:text-slate-300 rounded-2xl font-bold hover:bg-slate-200 transition-all"
                      >
                        다시 입력
                      </button>
                      <button
                        onClick={applyExtractedData}
                        className="flex-1 py-4 bg-[#4f46e5] text-white rounded-2xl font-bold shadow-lg shadow-indigo-500/20 hover:opacity-90 transition-all"
                      >
                        키워드 적용하기
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

const Bot: React.FC<{ className?: string; size?: number }> = React.memo(
  ({ className, size = 24 }) => (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
    >
      <path d="M12 8V4H8" />
      <rect width="16" height="12" x="4" y="8" rx="2" />
      <path d="M2 14h2" />
      <path d="M20 14h2" />
      <path d="M15 13v2" />
      <path d="M9 13v2" />
    </svg>
  ),
);

export default AdmissionEssayPage;
