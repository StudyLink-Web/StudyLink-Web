const roomId = 1;

function randomNumberString(length) {
    let result = '';
    for (let i = 0; i < length; i++) {
        result += Math.floor(Math.random() * 10); // 0~9
    }
    return result;
}

const senderId = randomNumberString(16);
console.log("senderId =", senderId);


function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        // 구독
        // 채팅창
        stompClient.subscribe('/topic/text', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId == senderId){ // 본인 메시지는 무시
                return;
            }
            spreadTextMessage(msg);
        });


        // 캔버스
    });
}

function safeSend(destination, message) {
    if (stompClient && stompClient.connected) {
        stompClient.send(destination, {}, JSON.stringify(message));
    }
}

// 채팅창 관련 함수
function spreadTextMessage(message){
    const messageArea = document.getElementById('messageArea');

    // 메시지 div 생성
    const msgDiv = document.createElement('div');
    msgDiv.classList.add('message'); // 공통 스타일

    // senderId에 따라 클래스 추가 (오른쪽/왼쪽)
    if (message.senderId == senderId) {
        msgDiv.classList.add('message-right'); // 내 메시지
    } else {
        msgDiv.classList.add('message-left'); // 상대 메시지
    }

    // 메시지 내용
    msgDiv.textContent = message.content;

    // 메시지 영역에 추가
    messageArea.appendChild(msgDiv);

    // 스크롤을 맨 아래로
    messageArea.scrollTop = messageArea.scrollHeight;
}

async function loadMessage(roomId){
    const url = "/room/loadMessage/"+roomId;
    const config = {
        method: 'get'
    };
    const res = await fetch(url, config);
    return res.json();
}


async function sendFile(formData){
    const url = "/room/saveFile";
    const config = {
        method: 'post',
        body: formData
    };
    const res = await fetch(url, config);
    return res.text();
}


// 캔버스 관련 함수

connect(); // webSocket 연결
loadMessage(roomId).then(result => { // 채팅기록 불러오기
    for(let message of result){
        if (message.messageType == "TEXT") {
            spreadTextMessage(message)
        }
    }
});

document.addEventListener('click', async (e)=>{
    if (e.target.id == 'sendFileBtn'){
        const fileInput = document.getElementById('file');
        const files = fileInput.files;

        if (!files[0]) {
            alert("파일을 선택해주세요!");
            return;
        }

        for (let file of files){
            const formData = new FormData();
            formData.append("file", file);
            formData.append("roomId", roomId); // roomId도 같이 전송

            const result = await sendFile(formData); // 순차 업로드
            if (result === "1") {
                console.log(`파일 ${file.name} 업로드 성공`);
                // 여기서 WebSocket 메시지 보내도 OK
            } else {
                console.log(`파일 ${file.name} 업로드 실패`);
            }
        }

    }
})

document.addEventListener('keydown', (e)=> {
    // 엔터만 눌렀고 Shift는 누르지 않은 경우
    // enter + shift는 줄바꿈
    if (e.key === "Enter" && !e.shiftKey) {
        const textarea = document.querySelector('textarea');
        e.preventDefault(); // 기본 줄바꿈 막기
        const msg = textarea.value;
        if (!msg.trim()) return;

        const message = {
            roomId: roomId,
            senderId: senderId,
            messageType: "TEXT",
            content: msg,
            isRead: false
        }

        // WebSocket 전송
        safeSend("/app/text", message);

        // 본인은 바로 반영
        spreadTextMessage(message);

        textarea.value = ""; // 전송 후 초기화
        textarea.focus();
    }
})