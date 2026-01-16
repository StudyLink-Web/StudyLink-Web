const statusMap = {
    TEMP: '임시',
    PENDING: '답변 전',
    IN_PROGRESS: '답변 중',
    ANSWERED: '답변 완료',
    COMPLETED: '종료'
};

let roleStrings = userRoles.map(r => r.authority);

myQuizList.forEach(room => {
    const link = document.createElement('a');
    link.href = `/room/enterRoom?roomId=${room.roomId}`;
    link.className = 'room-card'; // 그대로
    link.style.textDecoration = 'none';
    link.style.color = 'inherit';

    const subjectP = document.createElement('p');
    subjectP.textContent = room.subjectDTO?.name || '과목 없음';
    subjectP.className = 'subject';

    const pointP = document.createElement('p');
    pointP.textContent = `${room.point || 0}p`;
    pointP.className = 'point';

    const statusP = document.createElement('p');
    statusP.textContent = statusMap[room.status] || '알 수 없음';
    statusP.className = 'room-status ' + room.status.toLowerCase();

    link.appendChild(subjectP);
    link.appendChild(pointP);
    link.appendChild(statusP);

    link.addEventListener('click', (e) => {
        if (room.status === 'IN_PROGRESS' && roleStrings.includes("ROLE_STUDENT")) {
            e.preventDefault();
            alert('멘토가 답변 중입니다. 잠시만 기다려주세요.');
        }
    });

    myQuizContainer.appendChild(link);
});