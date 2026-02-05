// src/main/resources/static/js/inquiry.js
document.addEventListener("DOMContentLoaded", () => {
  // ====== 전역 변수 ======
  const isLogin = !!window.IS_LOGIN;
  const role = (window.USER_ROLE || "").toUpperCase();

  const isAdmin = role === "ADMIN" || role === "ROLE_ADMIN";
  const hasRegisterPermission =
    isLogin &&
    (role === "STUDENT" || role === "ROLE_STUDENT" ||
     role === "MENTOR"  || role === "ROLE_MENTOR"  ||
     role === "ADMIN"   || role === "ROLE_ADMIN");

  // ====== "문의 등록하기" 버튼 표시 제어 ======
  const regBox = document.querySelector(".register_campus_link");
  if (regBox) {
    // 기본 숨김(안전)
    regBox.style.display = "none";
    if (hasRegisterPermission) regBox.style.display = "";
  }

  // ====== 비공개 글(.inq-link) 클릭 처리 ======
  document.querySelectorAll(".inq-link").forEach((a) => {
    a.addEventListener("click", async (e) => {
      e.preventDefault();

      const qno = a.dataset.qno;
      if (!qno) return;

      // 관리자면 비번 없이 바로 상세
      if (isAdmin) {
        location.href = `/inquiry/detail/${qno}`;
        return;
      }

      // 비로그인이면 로그인 유도
      if (!isLogin) {
        alert("로그인이 필요합니다.");
        location.href = "/login";
        return;
      }

      // 비공개 비밀번호 입력
      const pw = prompt("비공개 문의입니다.\n비밀번호를 입력하세요.");
      if (!pw) return;

      // CSRF
      const csrfToken =
        document.querySelector('meta[name="_csrf"]')?.getAttribute("content") || "";
      const csrfHeader =
        document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content") || "";

      const params = new URLSearchParams();
      params.append("qno", qno);
      params.append("password", pw);

      try {
        const resp = await fetch("/inquiry/password/verify-ajax", {
          method: "POST",
          headers: {
            "Accept": "application/json",
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {})
          },
          body: params.toString()
        });

        const data = await resp.json().catch(() => ({}));

        if (resp.ok && data.ok) {
          location.href = `/inquiry/detail/${qno}`;
        } else {
          alert(data.message || "비밀번호가 일치하지 않습니다.");
        }
      } catch (err) {
        console.error(err);
        alert("요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      }
    });
  });
});
