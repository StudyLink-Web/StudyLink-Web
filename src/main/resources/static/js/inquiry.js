document.addEventListener("DOMContentLoaded", () => {
    console.log("[inquiry.js] loaded");

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content") || "";
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content") || "";

    if (!csrfToken || !csrfHeader) {
        console.warn("[inquiry.js] CSRF meta not found (layout head에 meta 필요)");
    }

    async function verifyPasswordAjax(qno, password) {
        const body = new URLSearchParams();
        body.append("qno", qno);
        body.append("password", password);

        const headers = {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            "Accept": "application/json"
        };
        if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

        const resp = await fetch("/inquiry/password/verify-ajax", {
            method: "POST",
            headers,
            body: body.toString()
        });

        const text = await resp.text();
        console.log("[verify-ajax] status=", resp.status, "body=", text);

        try {
            return JSON.parse(text);
        } catch (e) {
            return { ok: false, message: "서버 응답이 JSON이 아닙니다. 콘솔 확인" };
        }
    }

    const links = document.querySelectorAll(".inq-link");
    console.log("[inquiry.js] .inq-link count =", links.length);

    links.forEach(a => {
        a.addEventListener("click", async (e) => {
            e.preventDefault();

            const qno = (a.dataset.qno || "").trim();
            const isPublic = (a.dataset.public || "").trim(); // 'Y' or 'N'

            console.log("[click] qno=", qno, "isPublic=", isPublic);

            if (!qno) {
                alert("qno가 없습니다. data-qno 확인");
                return;
            }

            // 공개면 바로 상세 이동
            if (isPublic !== "N") {
                location.href = `/inquiry/detail/${qno}`;
                return;
            }

            // 비공개면 비번 입력
            const pw = prompt("비공개 문의입니다.\n비밀번호를 입력하세요:");
            if (pw === null) return;
            if (!pw.trim()) {
                alert("비밀번호를 입력하세요.");
                return;
            }

            const res = await verifyPasswordAjax(qno, pw.trim());
            if (res.ok) {
                location.href = `/inquiry/detail/${qno}`;
            } else {
                alert(res.message || "비밀번호가 일치하지 않습니다.");
            }
        });
    });
});
