/* ================= 공통 차트 유틸 ================= */
function drawChart(canvasId, labels, data) {
const canvas = document.getElementById(canvasId);

if (canvas.chartInstance) {
canvas.chartInstance.destroy();
}

canvas.chartInstance = new Chart(canvas, {
    type: 'line',
    data: {
        labels: labels,
        datasets: [{
            data: data,
            borderWidth: 2,
            tension: 0.3,
            pointRadius: 3
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: { display: false }
        },
        scales: {
            y: { beginAtZero: true }
        }
    }
});

/* ================= 차트 데이터 맵 ================= */
const chartConfig = {
    user: {
        data: userChartData,
        dailyChartId: 'userDailyChart',
        cumulativeChartId: 'userCumulativeChart',
        dailyTitleId: 'userDailyTitle',
        cumulativeTitleId: 'userCumulativeTitle',
        dailyLabel: '가입자 수'
    },
    payment: {
        data: paymentChartData,
        dailyChartId: 'paymentDailyChart',
        cumulativeChartId: 'paymentCumulativeChart',
        dailyTitleId: 'paymentDailyTitle',
        cumulativeTitleId: 'paymentCumulativeTitle',
        dailyLabel: '결제 금액'
    },
    exchange: {
        data: exchangeChartData,
        dailyChartId: 'exchangeDailyChart',
        cumulativeChartId: 'exchangeCumulativeChart',
        dailyTitleId: 'exchangeDailyTitle',
        cumulativeTitleId: 'exchangeCumulativeTitle',
        dailyLabel: '환전 금액'
    }
};


/* ================= 차트 렌더 ================= */
function renderChart(category, days) {
    const config = chartConfig[category];
    const periodKey = days === 7 ? 'day7' : 'day30';
    const d = config.data[periodKey];

    drawChart(config.dailyChartId, d.labels, d.daily ?? d.dailyAmount);
    drawChart(config.cumulativeChartId, d.labels, d.cumulative ?? d.cumulativeAmount);

    document.getElementById(config.dailyTitleId).innerText =
    `${days}일간 일별 ${config.dailyLabel}`;
    document.getElementById(config.cumulativeTitleId).innerText =
    `${days}일간 누적 ${config.dailyLabel}`;
}


/* ================= 토글 버튼 이벤트 ================= */
document.querySelectorAll('button[data-category]').forEach(btn => {
    btn.addEventListener('click', () => {
        const category = btn.dataset.category;
        const days = Number(btn.dataset.period);

        // 버튼 active 처리 (같은 카테고리 내에서)
        btn.parentElement.querySelectorAll('button').forEach(b =>
            b.classList.remove('active')
        );
        btn.classList.add('active');

        renderChart(category, days);
    });
});


/* ================= 초기 로딩 ================= */
renderChart('user', 7);
renderChart('payment', 7);
renderChart('exchange', 7);