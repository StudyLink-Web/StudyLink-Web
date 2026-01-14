console.log("boardComment.js in");
console.log("bnoValue =", typeof bnoValue !== "undefined" ? bnoValue : "UNDEFINED");

// ✅ CSRF: meta에서 직접 읽기
const _csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content") || "";
const _csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content") || "";

// ✅ 로그인 정보(템플릿에서 window.IS_LOGIN / window.LOGIN_USER 주입한 값 사용)
const IS_LOGIN = !!window.IS_LOGIN;
const LOGIN_USER = window.LOGIN_USER || "";

// ✅ 공통: 헤더 만들기
function buildHeaders(extra = {}) {
    const h = { ...extra };
    if (_csrfToken && _csrfHeader) {
        h[_csrfHeader] = _csrfToken;
    }
    return h;
}

// ✅ 공통: 실패 디버깅용
async function debugResponse(resp) {
    const text = await resp.text();
    console.log("status =", resp.status, resp.statusText);
    console.log("response =", text);
    return text;
}

// ✅ XSS 방지(렌더링용)
function esc(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

// ✅ 페이지 로드 시 댓글 1페이지 출력 (누구나)
document.addEventListener("DOMContentLoaded", () => {
    if (typeof bnoValue !== "undefined" && bnoValue) {
        spreadCommentList(bnoValue, 1);
    }
});

// ✅ 댓글 등록 버튼 (로그인만)
const addBtn = document.getElementById("cmtAddBtn");
if (addBtn) {
    addBtn.addEventListener("click", async () => {
        if (!IS_LOGIN) {
            alert("로그인이 필요합니다. 로그인 페이지로 이동합니다.");
            location.href = "/user/login";
            return;
        }

        const cmtText = document.getElementById("cmtText");

        if (!cmtText || cmtText.value.trim() === "") {
            alert("댓글 입력요망!");
            cmtText?.focus();
            return;
        }

        if (typeof bnoValue === "undefined" || !bnoValue) {
            alert("게시글 번호(bnoValue)가 없습니다. detail.html에서 bnoValue 주입 확인!");
            return;
        }

        const cmtData = {
            postId: bnoValue,
            writer: LOGIN_USER,
            content: cmtText.value.trim()
        };

        const result = await postCommentToServer(cmtData);
        console.log("post result =", result);

        if (String(result).trim() === "1") {
            alert("등록성공");
            cmtText.value = "";
            cmtText.focus();
            spreadCommentList(bnoValue, 1);
        } else {
            alert("등록실패 (서버 응답: " + result + ")");
        }
    });
}

// ✅ 댓글 리스트 출력 (누구나)
async function spreadCommentList(bno, page = 1) {
    const ul = document.getElementById("cmtListArea");
    if (!ul) return;

    const result = await commentListFromServer(bno, page);
    console.log("list result =", result);

    if (!result || !result.list) {
        ul.innerHTML = `<li class="list-group-item">댓글 데이터를 가져오지 못했습니다.</li>`;
        return;
    }

    if (result.list.length > 0) {
        if (page === 1) ul.innerHTML = "";

        let html = "";
        for (const comment of result.list) {
            const isMine = IS_LOGIN && LOGIN_USER && (LOGIN_USER === comment.writer);

            html += `<li class="list-group-item" data-cno="${esc(comment.cno)}">`;
            html += `  <div class="ms-2 me-auto">`;
            html += `    <div class="fw-bold">${esc(comment.writer)}</div>`;
            html += `    <span class="cmt-content">${esc(comment.content)}</span>`;
            html += `  </div>`;
            html += `  <span class="badge text-bg-primary">${esc(comment.regDate)}</span>`;

            if (isMine) {
                html += `  <button type="button" class="btn btn-sm btn-outline-warning mod" data-bs-toggle="modal" data-bs-target="#commentModal">e</button>`;
                html += `  <button type="button" class="btn btn-sm btn-outline-danger del">x</button>`;
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
    } else {
        ul.innerHTML = `<li class="list-group-item">Comment List Empty</li>`;
        const moreBtn = document.getElementById("moreBtn");
        if (moreBtn) moreBtn.style.visibility = "hidden";
    }
}

// ✅ 이벤트 위임(수정/삭제/더보기)
document.addEventListener("click", async (e) => {
    // 더보기
    if (e.target.id === "moreBtn") {
        spreadCommentList(bnoValue, parseInt(e.target.dataset.page, 10));
        return;
    }

    // 수정 버튼 클릭 → 모달 세팅 (본인만 버튼이 보이지만, 방어적으로 로그인 체크)
    if (e.target.classList.contains("mod")) {
        if (!IS_LOGIN) {
            alert("로그인이 필요합니다.");
            location.href = "/user/login";
            return;
        }

        const li = e.target.closest("li");
        const cno = li?.dataset.cno;

        const cmtWriter = li?.querySelector(".fw-bold")?.innerText || "";
        const cmtText = li?.querySelector(".cmt-content")?.innerText || "";

        document.getElementById("cmtWriterMod").innerHTML = `no.${esc(cno)}  <b>${esc(cmtWriter)}</b>`;
        document.getElementById("cmtTextMod").value = (cmtText || "").trim();
        document.getElementById("cmtModBtn").setAttribute("data-cno", cno);
        return;
    }

    // 모달 수정 확정
    if (e.target.id === "cmtModBtn") {
        if (!IS_LOGIN) {
            alert("로그인이 필요합니다.");
            location.href = "/user/login";
            return;
        }

        const content = document.getElementById("cmtTextMod")?.value?.trim() || "";
        if (!content) {
            alert("내용을 입력하세요.");
            document.getElementById("cmtTextMod")?.focus();
            return;
        }

        const modData = {
            cno: Number(e.target.dataset.cno),
            content
        };

        const result = await updateCommentToServer(modData);
        if (String(result).trim() === "1") alert("수정성공!");
        spreadCommentList(bnoValue, 1);
        document.querySelector("#commentModal .btn-close")?.click();
        return;
    }

    // 삭제
    if (e.target.classList.contains("del")) {
        if (!IS_LOGIN) {
            alert("로그인이 필요합니다.");
            location.href = "/user/login";
            return;
        }

        if (!confirm("댓글을 삭제할까요?")) return;

        const li = e.target.closest("li");
        const cno = li?.dataset.cno;

        const result = await removeCommentToServer(cno);
        if (String(result).trim() === "1") alert("댓글삭제성공!");
        spreadCommentList(bnoValue, 1);
    }
});

// ------ 비동기 데이터 함수 --------

// remove
async function removeCommentToServer(cno) {
    try {
        const url = "/comment/remove/" + cno;
        const resp = await fetch(url, {
            method: "DELETE",
            headers: buildHeaders()
        });
        if (!resp.ok) return await debugResponse(resp);
        return await resp.text();
    } catch (error) {
        console.log(error);
        return "";
    }
}

// modify
async function updateCommentToServer(modData) {
    try {
        const url = "/comment/modify";
        const resp = await fetch(url, {
            method: "PUT",
            headers: buildHeaders({ "Content-Type": "application/json; charset=utf-8" }),
            body: JSON.stringify(modData)
        });
        if (!resp.ok) return await debugResponse(resp);
        return await resp.text();
    } catch (error) {
        console.log(error);
        return "";
    }
}

// list (누구나)
async function commentListFromServer(bno, page) {
    try {
        const resp = await fetch("/comment/list/" + bno + "/" + page, { method: "GET" });
        if (!resp.ok) return null;
        return await resp.json();
    } catch (error) {
        console.log(error);
        return null;
    }
}

// post (로그인만)
async function postCommentToServer(cmtData) {
    try {
        const url = "/comment/post";
        const resp = await fetch(url, {
            method: "POST",
            headers: buildHeaders({
                "Content-Type": "application/json; charset=utf-8",
                "Accept": "text/plain"
            }),
            body: JSON.stringify(cmtData)
        });

        if (!resp.ok) return await debugResponse(resp);
        return await resp.text();
    } catch (error) {
        console.log(error);
        return "";
    }
}
