const myQuizContainer = document.getElementById('myQuizContainer');
myQuizList.forEach(room => {
    // a 태그가 최상위 div 역할
    const link = document.createElement('a');
    link.href = `/room/enterRoom?roomId=${room.roomId}`;
    link.className = 'grid-item d-flex flex-column justify-content-center align-items-center';
    link.style.textDecoration = 'none';
    link.style.color = 'inherit';

    const subjectP = document.createElement('p');
    subjectP.textContent = room.subjectDTO?.name || '과목 없음';

    const pointP = document.createElement('p');
    pointP.textContent = `${room.point || 0}p`;

    link.appendChild(subjectP);
    link.appendChild(pointP);

    myQuizContainer.appendChild(link);
});