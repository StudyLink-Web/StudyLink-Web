document.addEventListener("DOMContentLoaded", () => {

    function renderDDay(targetDateStr, el) {
        if (!el) return;

        const [y, m, d] = targetDateStr.split("-").map(Number);
        const target = new Date(y, m - 1, d);

        const now = new Date();
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

        const diffMs = target - today;
        const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

        if (diffDays > 0) el.textContent = `D-${diffDays}`;
        else if (diffDays === 0) el.textContent = `D-DAY`;
        else el.textContent = `D+${Math.abs(diffDays)}`;
    }

    // ✅ 반드시 선언 먼저
    const ddaySpan = document.getElementById("csatDday");

    // ✅ 선언 이후 사용
    renderDDay("2026-11-19", ddaySpan);
});
