document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".inq-link").forEach(a => {
        a.addEventListener("click", async (e) => {
            e.preventDefault();

            const qno = a.dataset.qno;
            const isPublic = a.dataset.public; // 'Y' or 'N'

            // ✅ 관리자면 무조건 통과 (prompt 없음)
            if (window.IS_ADMIN) {
                location.href = `/inquiry/detail/${qno}`;
                return;
            }

            // 공개면 바로 이동
            if (isPublic !== "N") {
                location.href = `/inquiry/detail/${qno}`;
                return;
            }

            // 비공개면 비번 prompt
            const pw = prompt("비공개 문의입니다.\n비밀번호를 입력하세요.");
            if (!pw) return;

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || "";
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || "";

            const params = new URLSearchParams();
            params.append("qno", qno);
            params.append("password", pw);

            const resp = await fetch("/inquiry/password/verify-ajax", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
                    ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {})
                },
                body: params.toString()
            });

            const data = await resp.json();
            if (data.ok) {
                location.href = `/inquiry/detail/${qno}`;
            } else {
                alert(data.message || "비밀번호가 일치하지 않습니다.");
            }
        });
    });
});
