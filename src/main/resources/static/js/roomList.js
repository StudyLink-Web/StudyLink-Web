if (message != null) {
    alert(message);
}

// 상태 코드 → 화면용 텍스트 매핑
const statusMap = {
    TEMP: '임시',
    PENDING: '답변 전',
    IN_PROGRESS: '답변 중',
    ANSWERED: '답변 완료',
    COMPLETED: '종료'
};

// 개인방
const privateContainer = document.getElementById('privateContainer');

privateRoomList.forEach(room => {
    const link = document.createElement('a');
    link.href = `/room/enterRoom?roomId=${room.roomId}`;
    link.className = 'room-card';

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

    privateContainer.appendChild(link);
});

// 공개방
const publicContainer = document.getElementById('publicContainer');

roomList.forEach(room => {
    const link = document.createElement('a');
    link.href = `/room/enterRoom?roomId=${room.roomId}`;
    link.className = 'room-card';

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

    publicContainer.appendChild(link);
});