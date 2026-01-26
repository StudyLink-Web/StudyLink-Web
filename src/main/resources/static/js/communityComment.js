console.log("boardComment.js in");
console.log("bnoValue =", typeof bnoValue !== "undefined" ? bnoValue : "UNDEFINED");

const _csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content") || "";
const _csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content") || "";

const IS_LOGIN = !!window.IS_LOGIN;
const LOGIN_USER = window.LOGIN_USER || "";

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

document.addEventListener("DOMContentLoaded", () => {
    if (typeof bnoValue !== "undefined" && bnoValue) {
        spreadCommentList(bnoValue, 1);
    }
});

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

        if (!bnoValue) {
            alert("게시글 번호가 없습니다.");
            return;
        }

        const cmtData = {
            bno: bnoValue,
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

async function spreadCommentList(bno, page = 1) {
    const ul = document.getElementById("cmtListArea");
    if (!ul) return;

    const result = await commentListFromServer(bno, page);
    if (!result || !result.list) {
        ul.innerHTML = `<li class="list-group-item">댓글 데이터를 가져오지 못했습니다.</li>`;
        return;
    }

    if (page === 1) ul.innerHTML = "";

    if (result.list.length === 0 && page === 1) {
        ul.innerHTML = `<li class="list-group-item">등록된 댓글이 없습니다.</li>`;
        return;
    }

    let html = "";
    for (const comment of result.list) {
        const isMine = IS_LOGIN && LOGIN_USER === comment.writer;

        html += `<li class="list-group-item" data-cno="${esc(comment.cno)}">`;
        html += ` <div class="ms-2 me-auto">`;
        html += `  <div class="fw-bold">${esc(comment.writer)}</div>`;
        html += `  <span class="cmt-content">${esc(comment.content)}</span>`;
        html += ` </div>`;
        html += ` <span class="badge text-bg-primary">${esc(comment.createdAt)}</span>`;

        if (isMine) {
            html += ` <button type="button" class="btn btn-sm btn-outline-warning mod" data-bs-toggle="modal" data-bs-target="#commentModal">e</button>`;
            html += ` <button type="button" class="btn btn-sm btn-outline-danger del">x</button>`;
        }
        html += `</li>`;
    }

    ul.innerHTML += html;

    const moreBtn = document.getElementById("moreBtn");
    if (moreBtn) {
        if (result.pageNo < result.totalPage) {
            moreBtn.style.visibility = "visible";
            moreBtn.dataset.page = String(page + 1);
        } else {
            moreBtn.style.visibility = "hidden";
        }
    }
}

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
        const cno = li?.dataset.cno;
        const writer = li?.querySelector(".fw-bold")?.innerText || "";
        const content = li?.querySelector(".cmt-content")?.innerText || "";

        document.getElementById("cmtWriterMod").innerHTML = `no.${esc(cno)} <b>${esc(writer)}</b>`;
        document.getElementById("cmtTextMod").value = content;
        document.getElementById("cmtModBtn").dataset.cno = cno;
        return;
    }

    if (e.target.id === "cmtModBtn") {
        const content = document.getElementById("cmtTextMod")?.value?.trim();
        if (!content) {
            alert("내용을 입력하세요.");
            return;
        }

        const modData = {
            cno: Number(e.target.dataset.cno),
            content
        };

        const result = await updateCommentToServer(modData);
        if (String(result).trim() === "1") {
            spreadCommentList(bnoValue, 1);
            document.querySelector("#commentModal .btn-close")?.click();
        }
        return;
    }

    if (e.target.classList.contains("del")) {
        if (!confirm("댓글을 삭제할까요?")) return;

        const cno = e.target.closest("li")?.dataset.cno;
        const result = await removeCommentToServer(cno);
        if (String(result).trim() === "1") {
            spreadCommentList(bnoValue, 1);
        }
    }
});

async function removeCommentToServer(cno) {
    const resp = await fetch("/comment/remove/" + cno, {
        method: "DELETE",
        headers: buildHeaders()
    });
    if (!resp.ok) return await debugResponse(resp);
    return await resp.text();
}

async function updateCommentToServer(modData) {
    const resp = await fetch("/comment/modify", {
        method: "PUT",
        headers: buildHeaders({ "Content-Type": "application/json; charset=utf-8" }),
        body: JSON.stringify(modData)
    });
    if (!resp.ok) return await debugResponse(resp);
    return await resp.text();
}

async function commentListFromServer(bno, page) {
    const resp = await fetch(`/comment/list/${bno}/${page}`);
    if (!resp.ok) return null;
    return await resp.json();
}

async function postCommentToServer(cmtData) {
    const resp = await fetch("/comment/post", {
        method: "POST",
        headers: buildHeaders({
            "Content-Type": "application/json; charset=utf-8",
            "Accept": "text/plain"
        }),
        body: JSON.stringify(cmtData)
    });
    if (!resp.ok) return await debugResponse(resp);
    return await resp.text();
}
