console.log("boardComment.js in");
console.log("bnoValue =", typeof bnoValue !== "undefined" ? bnoValue : "UNDEFINED");

// ✅ CSRF: meta에서 직접 읽기 (전역 csrfToken/csrfHeader 의존 제거)
const _csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content") || "";
const _csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content") || "";

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

// ✅ 페이지 로드 시 댓글 1페이지 출력
document.addEventListener("DOMContentLoaded", () => {
    if (typeof bnoValue !== "undefined" && bnoValue) {
        spreadCommentList(bnoValue, 1);
    }
});

// ✅ 댓글 등록 버튼
const addBtn = document.getElementById("cmtAddBtn");
if (addBtn) {
    addBtn.addEventListener("click", async () => {
        const cmtText = document.getElementById("cmtText");
        const cmtWriter = document.getElementById("cmtWriter");

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
            writer: cmtWriter ? cmtWriter.innerText : (typeof loginUser !== "undefined" ? loginUser : ""),
            content: cmtText.value.trim()
        };

        const result = await postCommentToServer(cmtData);
        console.log("post result =", result);

        if (result === "1") {
            alert("등록성공");
            cmtText.value = "";
            cmtText.focus();
            spreadCommentList(bnoValue, 1);
        } else {
            alert("등록실패 (서버 응답: " + result + ")");
        }
    });
}

// ✅ 댓글 리스트 출력
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

        let li = "";
        for (const comment of result.list) {
            li += `<li class="list-group-item" data-cno="${comment.cno}">`;
            li += `  <div class="ms-2 me-auto">`;
            li += `    <div class="fw-bold">${comment.writer}</div>`;
            li += `    ${comment.content}`;
            li += `  </div>`;
            li += `  <span class="badge text-bg-primary">${comment.regDate}</span>`;

            if (typeof loginUser !== "undefined" && loginUser && loginUser === comment.writer) {
                li += `  <button type="button" class="btn btn-sm btn-outline-warning mod" data-bs-toggle="modal" data-bs-target="#commentModal">e</button>`;
                li += `  <button type="button" class="btn btn-sm btn-outline-danger del">x</button>`;
            }

            li += `</li>`;
        }
        ul.innerHTML += li;

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
    }
}

// ✅ 이벤트 위임(수정/삭제/더보기)
document.addEventListener("click", async (e) => {
    if (e.target.id === "moreBtn") {
        spreadCommentList(bnoValue, parseInt(e.target.dataset.page, 10));
    }

    if (e.target.classList.contains("mod")) {
        const li = e.target.closest("li");
        const cno = li?.dataset.cno;

        const writerEl = li?.querySelector(".fw-bold");
        const cmtWriter = writerEl ? writerEl.innerText : "";
        const cmtTextNode = writerEl ? writerEl.nextSibling : null;
        const cmtText = cmtTextNode ? cmtTextNode.nodeValue : "";

        document.getElementById("cmtWriterMod").innerHTML = `no.${cno}  <b>${cmtWriter}</b>`;
        document.getElementById("cmtTextMod").value = (cmtText || "").trim();
        document.getElementById("cmtModBtn").setAttribute("data-cno", cno);
    }

    if (e.target.id === "cmtModBtn") {
        const modData = {
            cno: e.target.dataset.cno,
            content: document.getElementById("cmtTextMod").value.trim()
        };

        const result = await updateCommentToServer(modData);
        if (result === "1") alert("수정성공!");
        spreadCommentList(bnoValue, 1);
        document.querySelector("#commentModal .btn-close")?.click();
    }

    if (e.target.classList.contains("del")) {
        const li = e.target.closest("li");
        const result = await removeCommentToServer(li.dataset.cno);
        if (result === "1") alert("댓글삭제성공!");
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

// list
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

// post (JSON only)
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
