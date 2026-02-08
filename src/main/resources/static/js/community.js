document.addEventListener("DOMContentLoaded", () => {
  const registerBtn = document.querySelector(".register_campus_link");
  if (!registerBtn) return;

  // 기본 숨김
  registerBtn.style.display = "none";

  // 비로그인
  if (!window.IS_LOGIN) return;

  const role = (window.USER_ROLE || "").toUpperCase();

  // ✅ 학생 또는 관리자
  const hasPermission =
    role === "ROLE_MENTOR" || role === "MENTOR" ||
    role === "ROLE_ADMIN"   || role === "ADMIN";

  if (hasPermission) {
    registerBtn.style.display = "";
  }
});

function goRegister() {
  if (!window.IS_LOGIN) {
    alert("로그인이 필요합니다.");
    location.href = "/login";
    return;
  }

  const role = (window.USER_ROLE || "").toUpperCase();

  const hasPermission =
    role === "ROLE_MENTOR" || role === "MENTOR" ||
    role === "ROLE_ADMIN"   || role === "ADMIN";

  if (!hasPermission) {
    alert("해당 기능은 권한이 없습니다.");
    return;
  }

  location.href = "/community/register";
}
