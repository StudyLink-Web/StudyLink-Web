const statusMap = {
    TEMP: '임시',
    PENDING: '답변 전',
    IN_PROGRESS: '답변 중',
    ANSWERED: '답변 완료',
    COMPLETED: '종료'
};

const statusOrder = {
    TEMP: 1,
    PENDING: 2,
    IN_PROGRESS: 3,
    ANSWERED: 4,
    COMPLETED: 5
};

let roleStrings = userRoles.map(r => r.authority);

myQuizList
    .sort((a, b) => {
        return (statusOrder[a.status] || 99) - (statusOrder[b.status] || 99);
    })
    .forEach(room => {
        const link = document.createElement('a');
        link.href = `/room/enterRoom?roomId=${room.roomId}`;
        link.className = 'grid-item d-flex flex-column justify-content-center align-items-center';
        link.style.textDecoration = 'none';
        link.style.color = 'inherit';

        const subjectP = document.createElement('p');
        subjectP.textContent = room.subjectDTO?.name || '과목 없음';

        const pointP = document.createElement('p');
        pointP.textContent = `${room.point || 0}p`;

        const statusP = document.createElement('p');
        statusP.className = 'room-status';
        statusP.textContent = statusMap[room.status] || '알 수 없음';

        link.appendChild(subjectP);
        link.appendChild(pointP);
        link.appendChild(statusP);

        // 클릭 이벤트 처리
        link.addEventListener('click', (e) => {
            if (room.status === 'IN_PROGRESS' && roleStrings.includes("ROLE_STUDENT")) {
                e.preventDefault(); // 링크 이동 막기
                alert('멘토가 답변 중입니다. 잠시만 기다려주세요.');
            }
        });

        myQuizContainer.appendChild(link);
    });