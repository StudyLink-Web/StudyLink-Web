console.log("boardComment.js in");
console.log("bnoValue =", typeof bnoValue !== "undefined" ? bnoValue : "UNDEFINED");

// CSRF
const _csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content") || "";
const _csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content") || "";

// 로그인 정보
const IS_LOGIN = !!window.IS_LOGIN;
const LOGIN_USER = window.LOGIN_USER || "";

// 헤더
function buildHeaders(extra = {}) {
    const h = { ...extra };
    if (_csrfToken && _csrfHeader) h[_csrfHeader] = _csrfToken;
    return h;
}

async function debugResponse(resp) {
    const text = await resp.text();
    console.log("status =", resp.status, resp.statusText);
    console.log("response =", text);
    return text;
}

function esc(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

// 초기 로드
document.addEventListener("DOMContentLoaded", () => {
    if (typeof bnoValue !== "undefined" && bnoValue) {
        spreadCommentList(bnoValue, 1);
    }
});

// 댓글 등록
const addBtn = document.getElementById("cmtAddBtn");
if (addBtn) {
    addBtn.addEventListener("click", async () => {
        if (!IS_LOGIN) {
            alert("로그인이 필요합니다.");
            location.href = "/login";
            return;
        }

        const cmtText = document.getElementById("cmtText");
        if (!cmtText || cmtText.value.trim() === "") {
            alert("댓글 입력요망!");
            cmtText?.focus();
            return;
        }

        const cmtData = {
            postId: bnoValue,
            writer: LOGIN_USER,
            content: cmtText.value.trim()
        };

        const result = await postCommentToServer(cmtData);
        if (String(result).trim() === "1") {
            cmtText.value = "";
            spreadCommentList(bnoValue, 1);
        } else {
            alert("등록실패 (서버 응답: " + result + ")");
        }
    });
}

// 리스트
async function spreadCommentList(bno, page = 1) {
    const ul = document.getElementById("cmtListArea");
    if (!ul) return;

    const result = await commentListFromServer(bno, page);
    if (!result || !Array.isArray(result.list)) {
        ul.innerHTML = `<li class="list-group-item">등록된 댓글이 없습니다.</li>`;
        return;
    }

    if (page === 1) ul.innerHTML = "";

    for (const comment of result.list) {
        const isMine = IS_LOGIN && LOGIN_USER === comment.writer;

        let html = `<li class="list-group-item" data-cno="${esc(comment.cno)}">`;
        html += `<div class="ms-2 me-auto">`;
        html += `<div class="fw-bold">${esc(comment.writer)}</div>`;
        html += `<span class="cmt-content">${esc(comment.content)}</span>`;
        html += `</div>`;
        html += `<span class="badge text-bg-primary">${esc(comment.regDate)}</span>`;

        if (isMine) {
            html += `<button type="button" class="btn btn-sm btn-outline-warning mod" data-bs-toggle="modal" data-bs-target="#commentModal">e</button>`;
            html += `<button type="button" class="btn btn-sm btn-outline-danger del">x</button>`;
        }
        html += `</li>`;
        ul.insertAdjacentHTML("beforeend", html);
    }

    const moreBtn = document.getElementById("moreBtn");
    if (moreBtn) {
        moreBtn.style.visibility = result.pageNo < result.totalPage ? "visible" : "hidden";
        moreBtn.dataset.page = String(page + 1);
    }
}

// 이벤트 위임
document.addEventListener("click", async (e) => {
    if (e.target.id === "moreBtn") {
        spreadCommentList(bnoValue, Number(e.target.dataset.page));
        return;
    }

    if (e.target.classList.contains("mod")) {
        if (!IS_LOGIN) {
            alert("로그인이 필요합니다.");
            location.href = "/login";
            return;
        }

        const li = e.target.closest("li");
        const cno = li.dataset.cno;
        document.getElementById("cmtWriterMod").innerHTML =
            `no.${esc(cno)} <b>${esc(li.querySelector(".fw-bold").innerText)}</b>`;
        document.getElementById("cmtTextMod").value =
            li.querySelector(".cmt-content").innerText.trim();
        document.getElementById("cmtModBtn").dataset.cno = cno;
        return;
    }

    if (e.target.id === "cmtModBtn") {
        const content = document.getElementById("cmtTextMod").value.trim();
        if (!content) return;

        await updateCommentToServer({
            cno: Number(e.target.dataset.cno),
            content
        });
        spreadCommentList(bnoValue, 1);
        document.querySelector("#commentModal .btn-close")?.click();
        return;
    }

    if (e.target.classList.contains("del")) {
        if (!confirm("댓글을 삭제할까요?")) return;
        const cno = e.target.closest("li").dataset.cno;
        await removeCommentToServer(cno);
        spreadCommentList(bnoValue, 1);
    }
});

// api
async function removeCommentToServer(cno) {
    const resp = await fetch("/comment/remove/" + cno, {
        method: "DELETE",
        headers: buildHeaders()
    });
    if (!resp.ok) await debugResponse(resp);
    return resp.text();
}

async function updateCommentToServer(modData) {
    const resp = await fetch("/comment/modify", {
        method: "PUT",
        headers: buildHeaders({ "Content-Type": "application/json; charset=utf-8" }),
        body: JSON.stringify(modData)
    });
    if (!resp.ok) await debugResponse(resp);
    return resp.text();
}

// 🔥 핵심 수정 부분 (빈 응답 방어)
async function commentListFromServer(bno, page) {
    try {
        const resp = await fetch(`/comment/list/${bno}/${page}`);
        if (!resp.ok) return null;

        const text = await resp.text();
        if (!text || text.trim() === "") {
            return { list: [], pageNo: page, totalPage: 0 };
        }
        return JSON.parse(text);
    } catch (e) {
        console.log("commentListFromServer error", e);
        return null;
    }
}

async function postCommentToServer(cmtData) {
    const resp = await fetch("/comment/post", {
        method: "POST",
        headers: buildHeaders({ "Content-Type": "application/json; charset=utf-8" }),
        body: JSON.stringify(cmtData)
    });
    if (!resp.ok) return await debugResponse(resp);
    return resp.text();
}
