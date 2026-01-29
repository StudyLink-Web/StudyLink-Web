// src/main/resources/static/js/inquiry.js
document.addEventListener("DOMContentLoaded", () => {
    const isAdmin = !!window.IS_ADMIN;

    document.querySelectorAll(".inq-link").forEach((a) => {
        a.addEventListener("click", async (e) => {
            e.preventDefault();

            const qno = a.dataset.qno;
            const isPublic = a.dataset.public; // 'Y' or 'N' (list에서 넣어줬을 때만)

            if (!qno) return;

            // ✅ 관리자면 무조건 상세로
            if (isAdmin) {
                location.href = `/inquiry/detail/${qno}`;
                return;
            }

            // ✅ list.html에서 data-public 안 쓰는 경우 대비: 배지는 비공개만 .inq-link로 걸려있음
            const isPrivate = (isPublic === "N") || a.classList.contains("private");

            // 공개면 바로 상세로
            if (!isPrivate) {
                location.href = `/inquiry/detail/${qno}`;
                return;
            }

            // 비공개면 비번 검증
            const pw = prompt("비공개 문의입니다.\n비밀번호를 입력하세요.");
            if (!pw) return;

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content") || "";
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content") || "";

            const params = new URLSearchParams();
            params.append("qno", qno);
            params.append("password", pw);

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
        });
    });
});
