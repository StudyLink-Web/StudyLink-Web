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
    link.className = 'room-card';
    link.style.textDecoration = 'none';
    link.style.color = 'inherit';

    // 배경색 적용 (과목 색상 사용, 없으면 기본색)
    link.style.backgroundColor = room.subjectDTO?.color || '#f0f0f0';

    const subjectP = document.createElement('p');
    subjectP.textContent = room.subjectDTO?.name || '과목 없음';
    subjectP.className = 'subject';

    const pointP = document.createElement('p');
    pointP.textContent = `${room.point || 0}p`;
    pointP.className = 'point';

    link.appendChild(subjectP);
    link.appendChild(pointP);

    link.addEventListener('click', (e) => {
        if (room.status === 'IN_PROGRESS' && roleStrings.includes("ROLE_STUDENT")) {
            e.preventDefault();
            alert('멘토가 답변 중입니다. 잠시만 기다려주세요.');
        }
    });

    myQuizContainer.appendChild(link);
});