document.addEventListener("DOMContentLoaded", () => {
    const openBtn = document.getElementById("accountLookupBtn");
    const modal = document.getElementById("accountLookupModal");

    if (!openBtn || !modal) return;

    // 닫기 버튼들 (상단 X, 하단 닫기)
    const closeButtons = modal.querySelectorAll("[data-bs-dismiss='modal'], .btn-close");

    // 모달 열기
    openBtn.addEventListener("click", () => {
    modal.classList.add("show");
    modal.style.display = "block";
    modal.removeAttribute("aria-hidden");
    document.body.classList.add("modal-open");

    // backdrop 추가
    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop fade show";
    backdrop.id = "customModalBackdrop";
    document.body.appendChild(backdrop);
    });

    // 모달 닫기 함수
    const closeModal = () => {
    modal.classList.remove("show");
    modal.style.display = "none";
    modal.setAttribute("aria-hidden", "true");
    document.body.classList.remove("modal-open");

    const backdrop = document.getElementById("customModalBackdrop");
    if (backdrop) backdrop.remove();
    };

    // 닫기 버튼 이벤트 연결
    closeButtons.forEach(btn => {
    btn.addEventListener("click", closeModal);
    });
});

if (status === "PENDING") {
    document.getElementById('approveBtn').addEventListener('click', ()=>{
        approve();
    });
}


async function approve(){
    const url = "/admin/exchangeApprove/" + exchangeId;
    const config = {
        method: "get"
    };

    try {
        const res = await fetch(url, config);

        if (!res.ok) {
            alert("환전 승인 실패");
        } else {
            alert("환전이 승인 되었습니다.");
        }
        location.reload(); // 화면 갱신
    } catch (err) {
        alert("환전 승인 실패");
    }
}

if (status === "PENDING") {
    // 모달 띄우기
    document.getElementById('rejectBtn').addEventListener('click', () => {
        const modal = document.getElementById('rejectModal');
        modal.style.display = 'block';
        modal.classList.add('show');
        modal.setAttribute('aria-modal', 'true');
        modal.removeAttribute('aria-hidden');
        document.body.style.overflow = 'hidden'; // 스크롤 막기
    });


    // 모달 닫기 (X 버튼과 취소 버튼에 이벤트 추가)
    document.querySelectorAll('#rejectModal .btn-close, #rejectModal .btn-secondary').forEach(btn => {
        btn.addEventListener('click', () => {
        const modal = document.getElementById('rejectModal');
        modal.style.display = 'none';
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
        modal.removeAttribute('aria-modal');
        document.body.style.overflow = 'auto'; // 스크롤 다시 허용
        });
    });


    // 모달 바깥 클릭 시 닫기
    window.addEventListener('click', (e) => {
        const modal = document.getElementById('rejectModal');
        if (e.target === modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
        modal.removeAttribute('aria-modal');
        document.body.style.overflow = 'auto';
        }
    });

    document.getElementById('rejectForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const reason = document.getElementById('rejectReason').value.trim();
        if (!reason) {
            alert('거부 사유를 입력해주세요.');
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const url = "/admin/exchangeReject";
        const config = {
            method: "post",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({
                id: exchangeId,
                reason: reason
            })
        };

        try {
            const res = await fetch(url, config);

            if (!res.ok) {
                alert("환전 거부 실패");
            } else {
                alert("환전이 거부 되었습니다.");
            }
            location.reload(); // 화면 갱신
        } catch (err) {
            alert("환전 거부 실패");
        }

        location.reload();
    });

    async function reject(){

    }
}
