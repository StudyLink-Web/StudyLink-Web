const roomId = 1;

function randomNumberString(length) {
    let result = '';
    for (let i = 0; i < length; i++) {
        result += Math.floor(Math.random() * 10); // 0~9
    }
    return Number(result);
}

const senderId = randomNumberString(10);
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
            // 일반적으로 본인 메시지는 무시하지만 messageId를 받기위해 허용
            // if (msg.senderId == senderId){ // 본인 메시지는 무시
            //     return;
            // }
            spreadTextMessage(msg);

            // 메시지 읽음 요청하기(상대 메시지인 경우)
            if (msg.senderId === senderId) return;
            // 실제 db에 is_read true로 바꾸기
            readMessageToServer(msg.messageId);
            // 상대 화면 갱신하도록 메시지 요청
            safeSend("/app/readMessage", {messageId : msg.messageId});
        });

        // 이 요청 받으면 해당 메시지 읽음 처리하기(1 제거)
        stompClient.subscribe('/topic/readMessage', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId == senderId){
                return;
            }
            readMessage(msg.messageId);
        });

        // 이 요청 받으면 모든 메시지에서 1제거(상대방 입장)
        stompClient.subscribe('/topic/enterRoom', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId == senderId){
                return;
            }
            readAllMessage();
        });


        // 캔버스



        // connect가 비동기함수이므로 연결이 완료된 후 실행되야하는 함수들은 여기 작성(밖에 작성시 연결되기 전에 실행 될 수 있음)
        loadMessage(roomId).then(result => { // 채팅기록 불러오기
            for(let message of result){
                if (message.messageType == "TEXT") {
                    spreadTextMessage(message)
                }
            }
            console.log("메시지 로딩")
            safeSend("/app/enterRoom", {roomId: roomId, senderId: senderId})
        });
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

    // 메시지 컨테이너 생성
    const msgContainer = document.createElement('div');
    msgContainer.classList.add('message-container'); // 공통 클래스

    const isMyMessage = message.senderId == senderId;

    // senderId에 따라 클래스 추가
    if (isMyMessage) {
        msgContainer.classList.add('message-container-right');
    } else {
        msgContainer.classList.add('message-container-left');
    }

    // ===== 읽음 표시 (내 메시지 + 안 읽었을 때만) =====
    if (isMyMessage && message.isRead === false) {
        const readSpan = document.createElement('span');
        readSpan.classList.add('read-indicator');
        readSpan.textContent = '1';
        readSpan.dataset.messageId = message.messageId;
        msgContainer.appendChild(readSpan);
    }

    // 메시지 내용
    const msgDiv = document.createElement('div');
    msgDiv.classList.add(isMyMessage ? 'message-right' : 'message-left');

    const contentSpan = document.createElement('span');
    contentSpan.textContent = message.content;
    msgDiv.appendChild(contentSpan);

    msgContainer.appendChild(msgDiv);
    messageArea.appendChild(msgContainer);

    // 스크롤 맨 아래
    messageArea.scrollTop = messageArea.scrollHeight;
}

// 해당 메시지 1지우기(읽음 처리)
function readMessage(messageId){
    // 1. 해당 메시지 요소 찾기
    console.log(messageId)
    const readSpan = document.querySelector(`.read-indicator[data-message-id='${messageId}']`);
    if (readSpan) {
        readSpan.remove(); // 화면에서 '1' 제거
    }
}

// 모든 메시지 1지우기(읽음 처리)
function readAllMessage(){
    // 1. 화면에 있는 모든 read-indicator 요소 선택
    const readSpans = document.querySelectorAll('.read-indicator');

    // 2. 하나씩 제거
    readSpans.forEach(span => span.remove());
}

// 서버로 db is_read 변경 요청
async function readMessageToServer(messageId){
    const url = "/room/readMessage/"+messageId;
    const config = {
        method: 'get'
    };
    const res = await fetch(url, config);
    return res.text();
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
        // spreadTextMessage(message); // 이러면 본인은 messageId가 null임 -> 본인도 브로드캐스트로 받기

        textarea.value = ""; // 전송 후 초기화
        textarea.focus();
    }
})