const privateContainer = document.getElementById('privateContainer');

privateRoomList.forEach(room => {
    const colDiv = document.createElement('div');
    colDiv.className = 'grid-item';

    const subjectP = document.createElement('p');
    subjectP.textContent = room.subjectDTO.name || '과목 없음';

    const pointP = document.createElement('p');
    pointP.textContent = `${room.point || 0}p`;

    colDiv.appendChild(subjectP);
    colDiv.appendChild(pointP);

    privateContainer.appendChild(colDiv);
});


const publicContainer = document.getElementById('publicContainer');

roomList.forEach(room => {
    const colDiv = document.createElement('div');
    colDiv.className = 'grid-item';

    const subjectP = document.createElement('p');
    subjectP.textContent = room.subjectDTO.name || '과목 없음';

    const pointP = document.createElement('p');
    pointP.textContent = `${room.point || 0}p`;

    colDiv.appendChild(subjectP);
    colDiv.appendChild(pointP);

    publicContainer.appendChild(colDiv);
});