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
        // 그리기
        stompClient.subscribe('/topic/draw', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            drawInterpolatedLine({x : msg.x1, y : msg.y1}, {x : msg.x2, y : msg.y2});
            scheduleRender();
        });

        // 지우기
        stompClient.subscribe('/topic/erase', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            eraseInterpolated({x : msg.x1, y : msg.y1}, {x : msg.x2, y : msg.y2});
            scheduleRender();
        });

        // currentAction 초기화
        stompClient.subscribe('/topic/initializeCurrentAction', function(message){
            const msg = JSON.parse(message.body);
            console.log("currentAction 초기화")
            if (msg.senderId === senderId) return;
            initializeCurrentAction(msg.type);
        });

        // currentAction 리셋
        stompClient.subscribe('/topic/resetCurrentAction', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            resetCurrentAction();
        });

        // undoStack에 currentAction push
        stompClient.subscribe('/topic/pushToUndoStack', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            pushToUndoStack();
        });

        // undo, redo
        stompClient.subscribe('/topic/undoRedo', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            if (msg.type === 'undo') {
                undo();
            } else {
                redo();
            }
            scheduleRender();
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

// 랜더링 관련
let renderScheduled = false;

// 그리기 관련
let isDrawing = false;
let lastPoint = null;
const DRAW_STEP = 3; // px (작을수록 촘촘), 선 길이 조절
let currentPointer = null;

// 지우기 관련
const ERASE_STEP = 3; // 지우기 점 간격
const ERASE_RADIUS = 10; // 지우개 반경

// redo, undo
const undoStack = [];
const redoStack = [];
let currentAction = null; // 현재 드래그 중인 액션


function selectTool(tool) {
    selectedTool = tool;
}

// 렌더링 요청이 많아도 화면 렌더링은 한 프레임에 1회로 제한
function scheduleRender() {
    if (renderScheduled) return;
    renderScheduled = true;

    requestAnimationFrame(() => {
        canvas.requestRenderAll();
        renderScheduled = false;
    });
}

// currentAction 초기화 함수
function initializeCurrentAction(type){
    currentAction = {
        type: type, // 'draw' | 'erase' | 'move' | 'rotate' | 'scale' ...
        targets: [],   // 영향을 받은 객체들
        before: null,  // 작업 전 상태
        after: null    // 작업 후 상태
    };
}

// currentAction 리셋
function resetCurrentAction(){
    currentAction = null;
}

// updoStack에 currentAction push
function pushToUndoStack(){
    undoStack.push(currentAction);
    redoStack.length = 0; // 새 작업 → redo 초기화
}

// rAF 루프 → 실제 그리기
// 기존에는 mouse:move이벤트가 그리기를 담당했는데 f12(개발자모드)를 키는 등의 이유로 이벤트 빈도가 줄어들면 선이 끊김
// 따라서 이벤트는 좌표만 수집하고 이 함수가 그리기를 담당
// 그리기, 지우기처럼 연속 동작, 프레임마다 실행하는 함수를 포함, undo redo x
function loop() {
    if (isDrawing && currentPointer && lastPoint) {
        if (selectedTool === 'draw') {
            drawInterpolatedLine(lastPoint, currentPointer);

            message = {
                senderId: senderId,
                x1: lastPoint.x,
                y1: lastPoint.y,
                x2: currentPointer.x,
                y2: currentPointer.y
            }
            safeSend("/app/draw", message);
        }
        if (selectedTool === 'erase') {
            eraseInterpolated(lastPoint, currentPointer);

            message = {
                senderId: senderId,
                x1: lastPoint.x,
                y1: lastPoint.y,
                x2: currentPointer.x,
                y2: currentPointer.y
            }
            safeSend("/app/erase", message);
        }

        lastPoint = { ...currentPointer };
        scheduleRender();
    }
    // requestAnimationFrame : rAF
    // 브라우저에서 화면을 다시 그릴 타이밍에 맞춰 함수를 호출하도록 예약하는 JavaScript 함수
    requestAnimationFrame(loop);
}
loop();

// 그리기
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

    if (currentAction && currentAction.type === 'draw') {
        currentAction.targets.push(line);
    }
}

// 선 보간 함수
function drawInterpolatedLine(p1, p2) {
    const dx = p2.x - p1.x;
    const dy = p2.y - p1.y;
    let distance = Math.sqrt(dx * dx + dy * dy);

    // distance가 0이면 한 점 찍기 위해 1로 처리
    if (distance === 0) distance = 1;

    // 최소 1 step 보장
    const steps = Math.max(Math.floor(distance / DRAW_STEP), 1);
    const stepX = dx / steps;
    const stepY = dy / steps;

    // 위에 2개 로직에서 최소 1로 설정하지 않으면 마우스가 느릴때 점이 안찍힘

    let prevX = p1.x;
    let prevY = p1.y;

    for (let i = 1; i <= steps; i++) {
        const x = p1.x + stepX * i;
        const y = p1.y + stepY * i;
        drawLine(prevX, prevY, x, y);
        prevX = x;
        prevY = y;
    }
}

// 지우기
function eraseLine(x, y, threshold = 10) {
    // threshold: 지울 기준 거리(px)

    const objects = canvas.getObjects('line'); // 모든 Line 객체 가져오기
    const toRemove = [];

    objects.forEach(line => {
        const [x1, y1, x2, y2] = line.get('points') || [line.x1, line.y1, line.x2, line.y2];

        // 점과 선 사이 최소 거리 계산
        const dist = distancePointToLine(x, y, x1, y1, x2, y2);

        if (dist <= threshold) {
            toRemove.push(line);

            if (
                currentAction &&
                currentAction.type === 'erase' &&
                !currentAction.targets.includes(line)
            ) {
                currentAction.targets.push(line);
            }
        }
    });

    toRemove.forEach(line => canvas.remove(line));
}

// 지우개 보간 함수
function eraseInterpolated(p1, p2) {
    const dx = p2.x - p1.x;
    const dy = p2.y - p1.y;
    const distance = Math.sqrt(dx * dx + dy * dy);

    if (distance === 0) return;

    const steps = Math.ceil(distance / ERASE_STEP);

    for (let i = 0; i <= steps; i++) {
        const x = p1.x + (dx / steps) * i;
        const y = p1.y + (dy / steps) * i;
        eraseLine(x, y, ERASE_RADIUS);
    }
}

// 점(x0,y0)과 선(x1,y1)-(x2,y2) 사이 최소 거리 계산 함수
function distancePointToLine(x0, y0, x1, y1, x2, y2) {
    const A = x0 - x1; // 점 -> 선분 시작점 벡터
    const B = y0 - y1;

    const C = x2 - x1; // 선분 벡터
    const D = y2 - y1;

    const dot = A * C + B * D; // 점 벡터 · 선분 벡터 (dot product)
    const len_sq = C * C + D * D; // 선분 길이^2
    let param = -1;

    if (len_sq !== 0) param = dot / len_sq; // 점을 선분에 투영한 비율 (t)

    let xx, yy;

    if (param < 0) {
        xx = x1;
        yy = y1;
    } else if (param > 1) {
        xx = x2;
        yy = y2;
    } else {
        xx = x1 + param * C;
        yy = y1 + param * D;
    }

    const dx = x0 - xx;
    const dy = y0 - yy;
    return Math.sqrt(dx * dx + dy * dy);
}

// undo
function undo() {
    if (undoStack.length === 0) return;

    const action = undoStack.pop();

    if (action.type === 'draw') {
        action.targets.forEach(obj => canvas.remove(obj));
    }

    if (action.type === 'erase') {
        action.targets.forEach(obj => canvas.add(obj));
    }

    redoStack.push(action);
    scheduleRender();
}

// redo
function redo() {
    if (redoStack.length === 0) return;

    const action = redoStack.pop();

    if (action.type === 'draw') {
        action.targets.forEach(obj => canvas.add(obj));
    }

    if (action.type === 'erase') {
        action.targets.forEach(obj => canvas.remove(obj));
    }

    undoStack.push(action);
    scheduleRender();
}

// undo, redo 메시지 전송
function sendUndoRedoMessage(type){
    const message = {
        senderId: senderId,
        type: type // undo, redo
    }
    safeSend('/app/undoRedo', message)
}


canvas.on('mouse:down', (opt) => {
    isDrawing = selectedTool === 'draw' || selectedTool === 'erase';
    lastPoint = canvas.getPointer(opt.e);
    currentPointer = lastPoint;

    if (isDrawing) {
        initializeCurrentAction(selectedTool);

        const message = {
            senderId: senderId,
            type: selectedTool
        }
        safeSend('/app/initializeCurrentAction', message);
    }
});

canvas.on('mouse:move', (opt) => {
    if (!isDrawing) return;
    currentPointer = canvas.getPointer(opt.e);
});

canvas.on('mouse:up', () => {
    isDrawing = false;
    currentPointer = null;

    if (currentAction && currentAction.targets.length > 0) {
        pushToUndoStack();

        const message = {
            senderId: senderId
        }
        safeSend('/app/pushToUndoStack', message)
    }

    resetCurrentAction();

    const message = {
        senderId: senderId
    }
    safeSend('/app/resetCurrentAction', {})
});



// webSocket 연결
connect();