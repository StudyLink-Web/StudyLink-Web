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
        stompClient.subscribe('/topic/sendMessage', function(message){
            const msg = JSON.parse(message.body);
            // 일반적으로 본인 메시지는 무시하지만 messageId를 받기위해 허용
            // if (msg.senderId == senderId){ // 본인 메시지는 무시
            //     return;
            // }
            if (msg.messageType === "TEXT") {
                spreadTextMessage(msg);
            } else {
                loadRoomFileDTO(msg.fileUuid).then(result => {
                    spreadFileMessage(msg, result);
                });
            }

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
            if (msg.senderId === senderId) return;
            readMessage(msg.messageId);
        });

        // 이 요청 받으면 모든 메시지에서 1제거(상대방 입장)
        stompClient.subscribe('/topic/enterRoom', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            readAllMessage();
        });



        // 캔버스
        stompClient.subscribe('/topic/draw', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            drawLine(msg.x1, msg.y1, msg.x2, msg.y2);
        });



        // connect가 비동기함수이므로 연결이 완료된 후 실행되야하는 함수들은 여기 작성(밖에 작성시 연결되기 전에 실행 될 수 있음)
        loadMessage(roomId).then(result => { // 채팅기록 불러오기
            console.log("💬 로드된 메시지 수:", result.length);
            for(let message of result){
                if (message.messageType === "TEXT") {
                    spreadTextMessage(message);
                } else {
                    loadRoomFileDTO(message.fileUuid).then(result => {
                        spreadFileMessage(message, result);
                    });
                }
            }
            safeSend("/app/enterRoom", {roomId: roomId, senderId: senderId})
        }).catch(error => {
            console.error("❌ 메시지 로드 실패:", error);
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

    const isMyMessage = message.senderId === senderId;

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

// 파일 화면에 출력
function spreadFileMessage(msg, roomFileDTO) {
    const isMyMessage = msg.senderId === senderId;
    const messageArea = document.getElementById('messageArea');

    const msgContainer = document.createElement('div');
    msgContainer.classList.add('message-container');
    msgContainer.classList.add(
        isMyMessage ? 'message-container-right' : 'message-container-left'
    );

    const msgDiv = document.createElement('div');
    msgDiv.classList.add(isMyMessage ? 'message-right' : 'message-left');

    // ===== 읽음 표시 (내 메시지 + 안 읽었을 때만) =====
    if (isMyMessage && msg.isRead === false) {
        const readSpan = document.createElement('span');
        readSpan.classList.add('read-indicator');
        readSpan.textContent = '1';
        readSpan.dataset.messageId = msg.messageId;
        msgContainer.appendChild(readSpan);
    }

    // 이미지 파일
    if (roomFileDTO.file_type === 1) {
        const img = document.createElement('img');
        img.src = `/room/loadFile/${roomFileDTO.uuid}`; // img 태그의 src경로를 브라우저가 자동으로 get요청
        img.classList.add('chat-image');
        msgDiv.appendChild(img);

        // 이미지가 로드 완료되면 스크롤
        img.onload = () => {
            messageArea.scrollTop = messageArea.scrollHeight;
        };
    }

    // 일반 파일
    else {
        const fileLink = document.createElement('a');
        fileLink.href = `/room/loadFile/${roomFileDTO.uuid}`;
        fileLink.textContent = `📎 ${roomFileDTO.fileName}`;
        fileLink.download = roomFileDTO.fileName;
        msgDiv.appendChild(fileLink);
    }

    msgContainer.appendChild(msgDiv);
    messageArea.appendChild(msgContainer);
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





// 비동기
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


// ✅ 수정: sendFile 함수에 상세한 에러 로깅 추가
async function sendFile(formData){
    const url = "/room/saveFile";

    console.log("🚀 파일 업로드 시작");
    console.log("📍 URL:", url);

    try {
        const res = await fetch(url, {
            method: 'post',
            body: formData
        });

        console.log("📊 응답 상태:", res.status, res.statusText);

        if (!res.ok) {
            const errorText = await res.json();
            console.error("❌ HTTP 에러:", res.status);
            console.error("❌ 응답 내용:", errorText.substring(0, 200));
            return null;
        }

        const result = await res.json();
        console.log("✅ 응답 데이터:", result);
        return result;

    } catch (error) {
        console.error("❌ 네트워크 에러:", error.message);
        return null;
    }
}

async function loadRoomFileDTO(uuid){
    const url = "/room/loadRoomFileDTO/" + uuid;

    console.log("🚀 파일 불러오기 시작");
    console.log("📍 URL:", url);

    try {
        const res = await fetch(url, {
            method: 'get'
        });

        console.log("📊 응답 상태:", res.status, res.statusText);

        if (!res.ok) {
            const errorText = await res.text();
            console.error("❌ HTTP 에러:", res.status);
            console.error("❌ 응답 내용:", errorText.substring(0, 200));
            return null;
        }

        const result = await res.json();
        console.log("✅ 응답 데이터:", result);
        return result;

    } catch (error) {
        console.error("❌ 네트워크 에러:", error.message);
        return null;
    }
}



// 이벤트 리스너
document.addEventListener('click', async (e)=>{
    if (e.target.id === 'sendFileBtn'){
        console.log("🖱️ 파일 전송 버튼 클릭됨");

        const fileInput = document.getElementById('file');
        const files = fileInput.files;

        console.log("📁 선택된 파일 개수:", files.length);

        if (!files[0]) {
            alert("파일을 선택해주세요!");
            return;
        }

        for (let file of files){
            console.log(`📄 파일 정보: ${file.name} (${file.size} bytes, ${file.type})`);

            const formData = new FormData();
            formData.append("file", file);
            formData.append("roomId", roomId); // roomId도 같이 전송

            const result = await sendFile(formData); // 순차 업로드
            if (result != null) {
                console.log(`✅ 파일 ${file.name} 업로드 성공`);
                // 여기서 WebSocket 메시지 보내도 OK
                const message = {
                    roomId: roomId,
                    senderId: senderId,
                    fileUuid: result.uuid,
                    messageType: result.fileType === 1 ? "IMAGE" : "FILE",
                    isRead: false
                }
                safeSend("/app/sendMessage", message);
            } else {
                console.log(`❌ 파일 ${file.name} 업로드 실패`);
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
        safeSend("/app/sendMessage", message);

        textarea.value = ""; // 전송 후 초기화
        textarea.focus();
    }
})



// ============================================================ 캔버스 ==================================================================
// ============================================================ 캔버스 ==================================================================
// ============================================================ 캔버스 ==================================================================
// 캔버스 관련 전역 변수
const canvas = new fabric.Canvas('canvas');

// 도구 선택
let selectedTool = 'draw';

// 그리기 관련
let isDrawing = false;
let lastPoint = null;



function selectTool(tool) {
    selectedTool = tool;
}

function drawLine(x1, y1, x2, y2){ // 색상, 두께 등 나중에 추가하기
    // 길이가 0이면 skip
    if (x1 === x2 && y1 === y2) return;

    const line = new fabric.Line([x1, y1, x2, y2], {
        stroke: '#000',
        strokeWidth: 2,
        selectable: false,
        evented: false,
        strokeLineCap: 'round',  // 끝점 둥글게
        strokeLineJoin: 'round'  // 연결점 부드럽게
    });

    canvas.add(line);
    canvas.renderAll();
}



canvas.on('mouse:down', (opt) => {
    isDrawing = selectedTool === 'draw';
    lastPoint = canvas.getPointer(opt.e);
});

canvas.on('mouse:move', (opt) => {
    if (!isDrawing) return;

    const pointer = canvas.getPointer(opt.e);
    drawLine(lastPoint.x, lastPoint.y, pointer.x, pointer.y);

    // 필요하면 여기서 소켓으로 좌표 전송
    message = {
        senderId: senderId,
        x1: lastPoint.x,
        y1: lastPoint.y,
        x2: pointer.x,
        y2: pointer.y
    }
    safeSend("/app/draw", message);

    canvas.renderAll();
    lastPoint = pointer;
});

canvas.on('mouse:up', () => {
    isDrawing = false;
    currentLine = null;
});






// webSocket 연결
connect();