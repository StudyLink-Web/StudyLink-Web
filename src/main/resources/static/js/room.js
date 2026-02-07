const roomId = roomDTO.roomId;

const timerDisplay = document.getElementById('timerDisplay');

function updateDisplay(seconds) {
    const min = Math.floor(seconds / 60).toString().padStart(2, '0');
    const sec = (seconds % 60).toString().padStart(2, '0');
    timerDisplay.textContent = `${min}:${sec}`;
}

function startTimer() {
    updateDisplay(timeLeft);
    const timer = setInterval(() => {
        timeLeft--;
        updateDisplay(timeLeft);
        if (timeLeft <= 0) {
            clearInterval(timer);
            alert('시간 종료!');
            window.href="/room/list";
        }
    }, 1000);
}

if (roomDTO.status === "IN_PROGRESS") {
    startTimer();
}



if (message != null) {
    alert(message);
}



// 웹소켓 연결 끊김 탐지(일정 주기마다 서버에 ping을 보냄 -> 서버로부터 pong을 응답 받음, pong이 안오면 끊김으로 판단)
let socket;
let lastPong = Date.now();
let heartbeatInterval;
let reconnecting = false;
const HEARTBEAT_INTERVAL = 2000; // 2초마다 ping
const TIMEOUT = 6000; // 3초 동안 응답 없으면 끊김으로 판단

function startHeartbeat() {
    stopHeartbeat(); // 중복 방지
    lastPong = Date.now();

    heartbeatInterval = setInterval(() => {
        const now = Date.now();

        if (!stompClient || !stompClient.connected) {
            console.warn("STOMP 연결 끊김 감지");
            forceReconnect();
            return;
        }

        if (now - lastPong > TIMEOUT) {
            console.warn("pong 타임아웃");
            forceReconnect();
            return;
        }

        safeSend("/app/ping", {
            senderId,
            roomId
        });
    }, HEARTBEAT_INTERVAL);
}

function stopHeartbeat() {
    clearInterval(heartbeatInterval);
}

function forceReconnect() {
    stopHeartbeat();

    try {
        stompClient?.disconnect();
    } catch (e) {}

    try {
        socket?.close();
    } catch (e) {}

    stompClient = null;
    socket = null;

    attemptReconnect();
}

function attemptReconnect() {
    if (reconnecting) return;
    reconnecting = true;

    console.log("🔁 재연결 시도");

    setTimeout(() => {
        reconnecting = false;
        connect();
    }, 3000);
}

function connect() {
    socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnect, onError);
}

function onError(err) {
    console.error("STOMP 연결 실패", err);
    forceReconnect();
}

function onConnect(frame) {
    console.log('Connected: ' + frame);

    // 구독
    stompClient.subscribe(`/topic/pong/${roomId}`, function(message) {
        try {
            const msg = JSON.parse(message.body);
            lastPong = Date.now();
        } catch(e) {
            console.error("pong 파싱 에러", e);
        }
    });

    // 채팅창
    stompClient.subscribe(`/topic/sendMessage/${roomId}`, function(message){
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
    stompClient.subscribe(`/topic/readMessage/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        readMessage(msg.messageId);
    });

    // 이 요청 받으면 모든 메시지에서 1제거(상대방 입장)
    stompClient.subscribe(`/topic/enterRoom/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        readAllMessage();
    });



    // 캔버스
    // 동기화
    // 전역변수 초기화
    stompClient.subscribe('/topic/sync/' + roomId, msg => {
        const message = JSON.parse(msg.body);
        if (message.type === 'START') {
            showLoading();
            resetCanvasStateForSync();
        }
        if (message.type === 'DATA') {
            loadCanvas(message.payload.drawData);
            loadUndoRedo(message.payload.undoRedoStack);
            scheduleRender();
        }
        if (message.type === 'END') {
            hideLoading();
        }
    });

    // 그리기
    stompClient.subscribe(`/topic/draw/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, drawLine);
        scheduleRender();
    });

    // 지우기
    stompClient.subscribe(`/topic/erase/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, eraseInterpolated);
        scheduleRender();
    });

    // currentAction 초기화
    stompClient.subscribe(`/topic/initializeCurrentAction/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, initializeCurrentAction);
    });

    // currentAction 리셋
    stompClient.subscribe(`/topic/resetCurrentAction/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, resetCurrentAction);
    });

    // undoStack에 currentAction push
    stompClient.subscribe(`/topic/pushToUndoStack/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, pushToUndoStack);
    });

    // undo, redo
    stompClient.subscribe(`/topic/undoRedo/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        if (msg.type === 'undo') {
            handleMessage(msg, undo);
        } else {
            handleMessage(msg, redo);
        }
        scheduleRender();
    });

    // rectangle
    stompClient.subscribe(`/topic/rectangle/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, drawPreviewRectangle);
        scheduleRender();
    });

    // triangle
    stompClient.subscribe(`/topic/triangle/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, drawPreviewTriangle);
        scheduleRender();
    });

    // circle
    stompClient.subscribe(`/topic/circle/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, drawPreviewCircle);
        scheduleRender();
    });

    // line
    stompClient.subscribe(`/topic/line/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, drawPreviewLine);
        scheduleRender();
    });

    // removePreview
    stompClient.subscribe(`/topic/removePreview/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, removePreview);
        scheduleRender();
    });

    // drawLines
    stompClient.subscribe(`/topic/drawLines/${roomId}`, function(message){
        const msg = JSON.parse(message.body);
        if (msg.senderId === senderId) return;
        handleMessage(msg, drawLines);
        scheduleRender();
    });


    // connect가 비동기함수이므로 연결이 완료된 후 실행되야하는 함수들은 여기 작성(밖에 작성시 연결되기 전에 실행 될 수 있음)
    loadMessage(roomId).then(async result => { // 채팅기록 불러오기
        console.log("💬 로드된 메시지 수:", result.length);

        // 메시지 영역 초기화
        document.getElementById('messageArea').innerHTML = '';

        for(let message of result){
            // 서버에서 메시지 읽음 처리

            if (message.senderId !== senderId) {
                await readMessageToServer(message.messageId);
            }

            if (message.messageType === "TEXT") {
                spreadTextMessage(message);
            } else {
                // await로 순서 보장
                const roomFileDTO = await loadRoomFileDTO(message.fileUuid);
                spreadFileMessage(message, roomFileDTO);
            }
        }
        safeSend("/app/enterRoom", {roomId: roomId})
    }).catch(error => {
        console.error("❌ 메시지 로드 실패:", error);
    });

    startHeartbeat(); // 서버 연결 탐지
}

function safeSend(destination, message) {
    if (stompClient && stompClient.connected) {
        stompClient.send(destination + '/' + roomId, {}, JSON.stringify(message));
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
    if (roomFileDTO.fileType === 1) {
        // 이미지 다운로드 링크 생성
        const downloadLink = document.createElement('a');
        downloadLink.href = `/room/loadFile/${roomFileDTO.uuid}`;
        downloadLink.download = roomFileDTO.fileName;

        // 이미지 생성
        const img = document.createElement('img');
        img.src = `/room/loadFile/${roomFileDTO.uuid}`; // img 태그의 src경로를 브라우저가 자동으로 get요청
        img.classList.add('chat-image');

        // img를 a로 감싸기
        downloadLink.appendChild(img);

        msgDiv.appendChild(downloadLink);

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


// 파일 미리 보기
document.getElementById('file').addEventListener('change', () => {
    const previewContainer = document.getElementById('previewContainer');
    previewContainer.innerHTML = ''; // 기존 미리보기 초기화

    const files = document.getElementById('file').files;
    if (files.length === 0) return;

    previewContainer.style.borderTop = '1px solid #ddd';

    for (const file of files) {
        if (!file.type.startsWith('image/')) continue; // 이미지 파일만 처리

        const img = document.createElement('img');
        img.style.maxWidth = '150px';
        img.style.maxHeight = '150px';
        img.style.margin = '5px';

        const reader = new FileReader();
        reader.onload = e => {
            img.src = e.target.result;
            previewContainer.appendChild(img);
        };
        reader.readAsDataURL(file);

    }
});


// 비동기
// 서버로 db is_read 변경 요청
async function readMessageToServer(messageId){
    // 상태가 IN_PROGRESS, ANSWERED, COMPLETED인경우
    if (roomDTO.status === "TEMP" || roomDTO.status === "PENDING") return;
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


document.addEventListener('keydown', async (e)=> {
    // 엔터만 눌렀고 Shift는 누르지 않은 경우
    // enter + shift는 줄바꿈
    if (e.key === "Enter" && !e.shiftKey) {
        const textarea = document.querySelector('textarea');
        e.preventDefault(); // 기본 줄바꿈 막기
        const msg = textarea.value;

        // 메시지 전송
        if (msg.trim()) {
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

        // 파일 전송
        const fileInput = document.getElementById('file');
        const files = fileInput.files;

        console.log("📁 선택된 파일 개수:", files.length);

        if (files[0]) {
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
            fileInput.value = ''; // 선택 파일 초기화
            document.getElementById('previewContainer').innerHTML = ''; // 파일 미리보기 초기화
        }
    }
})



// ============================================================ 캔버스 ==================================================================
// ============================================================ 캔버스 ==================================================================
// ============================================================ 캔버스 ==================================================================
// 캔버스 관련 전역 변수
const canvas = new fabric.Canvas('canvas');
canvas.renderOnAddRemove = false;
function resizeCanvas() {
    const container = document.querySelector('.canvas-container');

    // 창 크기에서 사이드바 / 여백 빼기
    const canvasWidth = window.innerWidth - 400;
    const canvasHeight = window.innerHeight - 100;

    // 컨테이너 크기 맞추기
    container.style.width = canvasWidth + 'px';
    container.style.height = canvasHeight + 'px';

    // 컨테이너 실제 크기 가져오기
    const containerWidth = container.clientWidth;
    const containerHeight = container.clientHeight;

    // 디바이스 픽셀 비율
    const dpr = window.devicePixelRatio || 1;

    // 캔버스 DOM 픽셀 크기 설정
    const canvasEl = canvas.getElement();
    canvasEl.width = containerWidth * dpr;
    canvasEl.height = containerHeight * dpr;

    // CSS로 화면 표시 크기
    canvasEl.style.width = containerWidth + 'px';
    canvasEl.style.height = containerHeight + 'px';

    // Fabric.js 내부에서 사용하는 viewport scale 설정
    canvas.setWidth(containerWidth * dpr);
    canvas.setHeight(containerHeight * dpr);
    canvas.setZoom(dpr);

    scheduleRender();
}
canvas.isDrawingMode = false; // 드로잉 모드
const SMOOTH_ALPHA = 0.35; // 손떨림 보정(0 ~ 1.0(원본))

// 도구 선택
let selectedTool = 'draw';
let currentShape = null; // rect, circle, triangle, line

// 도형
let shapeCurrentPoint = null;
let isShapeDrawing = false;
let prevShapeCurrentPoint = null;

let rectStartPoint = null;
let previewRect = {}; // 사각형 미리보기

let triangleFirstPoint = null;  // 첫 클릭 위치
let triangleSecondPoint = null;  // 첫 클릭 위치
let previewTriangle = {};

let circleCenterPoint = null;
let previewCircle = {};

let lineStartPoint = null;
let previewLine = {};

// 함수 생성 기능
const GRAPH_SIZE = 20; // 함수 입력값 범위
const FUNCTION_DRAW_STEP = 25; // 픽셀 배율
const LIMIT = GRAPH_SIZE * FUNCTION_DRAW_STEP;
const STEP = 0.1; // 함수 x값 촘촘한 정도


// 캔버스 이동 관련
let isPanning = false;

// 랜더링 관련
let renderScheduled = false;
let lastRenderTime = 0;
const RENDER_INTERVAL = 60; // 100ms마다 1번 랜더링

// 그리기 관련
let isDrawing = false;
let lastPoint = null;
const DRAW_STEP = 20; // px (작을수록 촘촘), 선 길이 조절
const CIRCLE_DRAW_STEP = 5;
let currentPointer = null;
let currentColor = '#000000';

// 지우기 관련
const ERASE_STEP = 5; // 지우기 점 간격
const ERASE_RADIUS = 10; // 지우개 반경

// 영역선택 관련
let isSelectLocked = false; // 같은 객체를 양쪽에서 이동시키면 충돌위험. 한쪽이 select모드면 다른쪽은 잠금
let isTransform = false;

// 메시지 번호
// undo, redo와 관련된 메시지는 처리 순서가 중요
// 항상 번호 순서대로 처리하기 위한 변수
let lastSeq = 0; // 마지막 처리된 메시지 seq
let pendingQueue = {}; // seq -> message
let mySeq = 1; // 내가 보낸 메시지 번호

// undo, redo
let undoStack = [];
let redoStack = [];
let currentAction = null; // 현재 드래그 중인 액션

// DB 작업 순차 실행용 큐
let undoRedoQueue = Promise.resolve();

// 초기화 버튼 클릭
document.getElementById('cleanBtn').addEventListener('click', async () => {
    try {
        await fetch('/room/resetCanvas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ roomId })
        });
        safeSend("/app/enterRoom", {roomId: roomId});
    } catch (err) {
        console.error('DB 초기화 실패:', err);
        return;
    }
});

// 툴 선택
document.getElementById('penBtn').addEventListener('click', () => selectTool('draw'));
document.getElementById('eraseBtn').addEventListener('click', () => selectTool('erase'));
document.getElementById('selectionBtn').addEventListener('click', (e) => selectTool('select'));

document.getElementById('undoBtn').addEventListener('click', () => {selectTool('draw'); safeUndoRedo('undo');});
document.getElementById('redoBtn').addEventListener('click', () => {selectTool('draw'); safeUndoRedo('redo');});

// 색상 선택
const customColorInput = document.getElementById('customColor');

// 팔레트 클릭
document.querySelectorAll('.color-box').forEach(box => {
    box.addEventListener('click', () => {
        currentColor = box.dataset.color;
        customColorInput.value = currentColor; // 🔥 커스텀 컬러도 변경
        setSelected(box);
    });
});

// 커스텀 컬러 변경
customColorInput.addEventListener('input', (e) => {
    currentColor = e.target.value;
    clearSelected(); // 팔레트 선택 해제
});

function setSelected(el) {
    clearSelected();
    el.classList.add('selected');
}

function clearSelected() {
    document.querySelectorAll('.color-box')
        .forEach(b => b.classList.remove('selected'));
}

// 도형 선택
document.querySelectorAll('#shapeDiv .icon').forEach(icon => {
    icon.addEventListener('click', () => {

        // 도형 모드로
        selectTool('shape');

        // 도형 타입 저장
        if (icon.classList.contains('rect')) currentShape = 'rect';
        if (icon.classList.contains('circle')) currentShape = 'circle';
        if (icon.classList.contains('triangle')) currentShape = 'triangle';
        if (icon.classList.contains('line')) currentShape = 'line';

        // 3) UI 선택 표시
        setActiveShape(icon);
    });
});

function setActiveShape(selected) {
    document.querySelectorAll('#shapeDiv .icon')
        .forEach(i => i.classList.remove('active'));

    selected.classList.add('active');
}



document.getElementById('drawBtn').addEventListener('click', () => {
    if (!getValidatedRangesAndFunction()) return;

    const expr = document.getElementById('funcInput').value;
    const xMin = parseFloat(document.getElementById('xRangeLeft').value);
    const xMax = parseFloat(document.getElementById('xRangeRight').value);
    const yMin = parseFloat(document.getElementById('yRangeLeft').value);
    const yMax = parseFloat(document.getElementById('yRangeRight').value);
    const showAxes = document.getElementById('showAxes').checked;

    const safeExpr = getSafeFunc(expr);
    const func = new Function('x', `return ${safeExpr}`);

    initializeCurrentAction({ type: 'draw' });

    let message = {
        senderId: senderId,
        seq: mySeq++,
        type: 'draw'
    }
    safeSend('/app/initializeCurrentAction', message);

    drawGraphWithLines(func, xMin, xMax, yMin, yMax, showAxes, currentColor);
    scheduleRender();
    selectTool('select');

    if (currentAction && currentAction.targets.length > 0) {
        // UI 즉시 반영: undoStack에 push
        pushToUndoStack();

        // pushToUndoStack 메시지는 UI 즉시 전송
        const pushMsg = {
            senderId: senderId,
            seq: mySeq++
        };
        safeSend('/app/pushToUndoStack', pushMsg);

        const actionCopy = JSON.parse(JSON.stringify(currentAction));

        const undoRedoStackDTO = {
            roomId: roomId,
            undoStack: JSON.parse(JSON.stringify(undoStack)),
            redoStack: JSON.parse(JSON.stringify(redoStack))
        };

        undoRedoQueue = undoRedoQueue.then(async () => {
            // DB 저장
            await saveCanvasActionToDB(actionCopy.type, actionCopy.targets.map(t => ({
                uuid: t.uuid,
                stroke: t.stroke,
                x1: t.x1,
                y1: t.y1,
                x2: t.x2,
                y2: t.y2
            })));

            // undo/redo 스택 DB 저장
            await saveUndoRedoStack(undoRedoStackDTO);
        }).catch(console.error);

        // currentAction 리셋 & 메시지 전송
        resetCurrentAction();

        const resetMsg = {
            senderId: senderId,
            seq: mySeq++
        };
        safeSend('/app/resetCurrentAction', resetMsg);
    } else {
        // currentAction 비어있으면 그냥 리셋
        resetCurrentAction();
    }
});

function getSafeFunc(expr) {
    return expr.replace(/\^/g, '**') // x ^ 2 -> x ** 2
               .replace(/(\d)(x)/g, '$1*$2') // 2x → 2*x
               .replace(/\b(sin|cos|tan|asin|acos|atan|log|sqrt|abs|exp)\b/g, 'Math.$1'); // 삼각함수
}

function getValidatedRangesAndFunction() {
    // ---------- 범위 ----------
    const xMin = parseFloat(document.getElementById('xRangeLeft').value);
    const xMax = parseFloat(document.getElementById('xRangeRight').value);
    const yMin = parseFloat(document.getElementById('yRangeLeft').value);
    const yMax = parseFloat(document.getElementById('yRangeRight').value);
    console.log(xMin, xMax, yMin, yMax)

    if ([xMin, xMax, yMin, yMax].some(v => Number.isNaN(v))) {
        alert('x, y 범위는 숫자로 입력해주세요.');
        return false;
    }

    if (xMin >= xMax) {
        alert('x 범위는 최소값이 최대값보다 작아야 합니다.');
        return false;
    }

    if (yMin >= yMax) {
        alert('y 범위는 최소값이 최대값보다 작아야 합니다.');
        return false;
    }


    if (
        Math.abs(xMin) > GRAPH_SIZE || Math.abs(xMax) > GRAPH_SIZE ||
        Math.abs(yMin) > GRAPH_SIZE || Math.abs(yMax) > GRAPH_SIZE
    ) {
        alert('범위가 너무 큽니다.');
        return false;
    }

    // ---------- 함수식 ----------
    const expr = document.getElementById('funcInput').value.trim();
    if (!expr) {
        alert('함수식을 입력해주세요.');
        return false;
    }

    let func;
    try {
        // ^ → ** 변환 (지수 연산 지원)
        const safeExpr = getSafeFunc(expr);
        func = new Function('x', `return ${safeExpr}`);
    } catch (e) {
        alert('함수식 문법이 올바르지 않습니다.');
        return false;
    }

    // ---------- 검증 성공 ----------
    return true;
}

function drawGraphWithLines(func, xMin, xMax, yMin, yMax, showAxes, stroke) {
    const vp = canvas.viewportTransform;
    scaleX = vp[0];
    scaleY = vp[3];
    const offsetX = vp[4] / scaleX;
    const offsetY = vp[5] / scaleY;
    console.log(vp)
    const canvasWidth = canvas.getWidth();
    const canvasHeight = canvas.getHeight();

    const centerX = canvasWidth / 2 / scaleX;
    const centerY = canvasHeight / 2 / scaleY;


    const lines = []; // 그래프 + 축 전부 담는 배열
    const messages = [];
    let prevPoint = null;

    // =====================
    // 그래프 그리기
    // =====================

    for (let i = Math.ceil(xMin/STEP); i <= Math.floor(xMax/STEP); i++) { // 소수점 오차방지
        const x = i * STEP;
        const y = func(x);

        if (!isFinite(y)) {
            prevPoint = null; // 선 연결 끊기
            continue;
        }

        const px = centerX + x * FUNCTION_DRAW_STEP - offsetX;
        const py = centerY - y * FUNCTION_DRAW_STEP - offsetY;
        const currPoint = {x: px, y: py}

        if (prevPoint) {
            if (x > -1 && x < 1) {
                console.log(isValidPoint(prevPoint, centerX, centerY, offsetX, offsetY) || isValidPoint(currPoint, centerX, centerY, offsetX, offsetY));
            }

            if (isValidPoint(prevPoint, centerX, centerY, offsetX, offsetY) || isValidPoint(currPoint, centerX, centerY, offsetX, offsetY)) {
                const { x1, y1, x2, y2 } = clampLine(prevPoint, currPoint, centerX, centerY, offsetX, offsetY);
                const newObjectId = generateUUID();
                const message = {
                    uuid: newObjectId,
                    x1: x1,
                    y1: y1,
                    x2: x2,
                    y2: y2,
                    stroke: stroke
                }
                messages.push(message);
                const line = drawLine(message);
                if (line) {
                    lines.push(line);
                }
            }
        }
        prevPoint = currPoint;
    }

    // =====================
    // 축 그리기 (1 단위, FUNCTION_DRAW_STEP 배 스케일)
    // =====================
    if (showAxes) {
        // =====================
        // X축 (y = 0)
        // =====================
        for (let x = -LIMIT; x <= LIMIT; x += 1) {
            const x1 = centerX + x * FUNCTION_DRAW_STEP - offsetX;
            const x2 = centerX + (x + 1) * FUNCTION_DRAW_STEP - offsetX;
            const y  = centerY - offsetY;

            const { x1: cx1, y1: cy1, x2: cx2, y2: cy2 } = clampLine(
                { x: x1, y: y },
                { x: x2, y: y }, centerX, centerY, offsetX, offsetY
            );
            const newObjectId = generateUUID();
            const message = {
                uuid: newObjectId,
                x1: cx1,
                y1: cy1,
                x2: cx2,
                y2: cy2,
                stroke: '#000000'
            }
            messages.push(message);
            const line = drawLine(message);
            if (line) {
                lines.push(line);
            }
        }

        // =====================
        // Y축 (x = 0)
        // =====================
        for (let y = -LIMIT; y <= LIMIT; y += 1) {
            const y1 = centerY - y * FUNCTION_DRAW_STEP - offsetY;
            const y2 = centerY - (y + 1) * FUNCTION_DRAW_STEP - offsetY;
            const x  = centerX - offsetX;

            const { x1: cx1, y1: cy1, x2: cx2, y2: cy2 } = clampLine(
                { x: x, y: y1 },
                { x: x, y: y2 }, centerX, centerY, offsetX, offsetY
            );
            const newObjectId = generateUUID();
            const message = {
                uuid: newObjectId,
                x1: cx1,
                y1: cy1,
                x2: cx2,
                y2: cy2,
                stroke: '#000000'
            }
            messages.push(message);
            const line = drawLine(message);
            if (line) {
                lines.push(line);
            }
        }
    }

    safeSend("/app/drawLines", messages);

    const activeSelection = new fabric.ActiveSelection(lines, {
        canvas: canvas
    });

    // 선택 상태로 묶기
    canvas.setActiveObject(activeSelection);
}

function isValidPoint(point, centerX, centerY, offsetX, offsetY) {
    return !(point.x < centerX - LIMIT - offsetX || point.x > centerX + LIMIT - offsetX
     || point.y < centerY - LIMIT - offsetY || point.y > centerY + LIMIT - offsetY);
}

function clampLine(prev, curr, centerX, centerY, offsetX, offsetY) {
    let x1 = prev.x, y1 = prev.y;
    let x2 = curr.x, y2 = curr.y;

    // LIMIT 경계
    const minX = centerX - LIMIT - offsetX, maxX = centerX + LIMIT - offsetX;
    const minY = centerY - LIMIT - offsetY, maxY = centerY + LIMIT - offsetY;

    // 직선이 가로로만 영역 벗어난 경우
    if (x1 < minX || x1 > maxX || x2 < minX || x2 > maxX) {
        // 직선 방정식 이용해서 x가 경계일 때 y 계산
        const dx = x2 - x1;
        const dy = y2 - y1;
        if (dx !== 0) {
            if (x1 < minX) y1 = y1 + (minX - x1) * dy / dx, x1 = minX;
            if (x1 > maxX) y1 = y1 + (maxX - x1) * dy / dx, x1 = maxX;
            if (x2 < minX) y2 = y1 + (minX - x1) * dy / dx, x2 = minX;
            if (x2 > maxX) y2 = y1 + (maxX - x1) * dy / dx, x2 = maxX;
        }
    }

    // 세로 방향도 동일하게 처리
    if (y1 < minY || y1 > maxY || y2 < minY || y2 > maxY) {
        const dx = x2 - x1;
        const dy = y2 - y1;
        if (dy !== 0) {
            if (y1 < minY) x1 = x1 + (minY - y1) * dx / dy, y1 = minY;
            if (y1 > maxY) x1 = x1 + (maxY - y1) * dx / dy, y1 = maxY;
            if (y2 < minY) x2 = x1 + (minY - y1) * dx / dy, y2 = minY;
            if (y2 > maxY) x2 = x1 + (maxY - y1) * dx / dy, y2 = maxY;
        }
    }

    return { x1, y1, x2, y2 };
}

// 초기화 함수
function resetCanvasStateForSync() {
    // 1. 캔버스 초기화
    canvas.getObjects().forEach(obj => canvas.remove(obj));

    // 2. undo/redo 초기화
    undoStack = [];
    redoStack = [];
    currentAction = null;

    // 3. 메시지 순서 관련 초기화
    lastSeq = 0;
    pendingQueue = {};
    mySeq = 1;

    // 4. DB 큐 초기화
    undoRedoQueue = Promise.resolve();

    canvas.isDrawingMode = false; // 드로잉 모드

    // 도구 선택
    let selectedTool = 'draw';
    let currentShape = null; // rect, circle, triangle, line

    // 도형
    let shapeCurrentPoint = null;
    let isShapeDrawing = false;
    let prevShapeCurrentPoint = null;

    let rectStartPoint = null;
    let previewRect = {}; // 사각형 미리보기

    let triangleFirstPoint = null;  // 첫 클릭 위치
    let triangleSecondPoint = null;  // 첫 클릭 위치
    let previewTriangle = {};

    let circleCenterPoint = null;
    let previewCircle = {};

    let lineStartPoint = null;
    let previewLine = {};

    // 캔버스 이동 관련
    let isPanning = false;

    // 랜더링 관련
    let renderScheduled = false;
    let lastRenderTime = 0;

    // 그리기 관련
    let isDrawing = false;
    let lastPoint = null;
    let currentPointer = null;
    let currentColor = '#000000';
}

function safeUndoRedo(actionType) {
    if (actionType === 'undo') {
        undo();                  // undo + DB
        sendUndoRedoMessage('undo');   // 메시지 전송
    } else {
        redo();                  // redo + DB
        sendUndoRedoMessage('redo');
    }
}

// 도구 선택 함수
function selectTool(tool) {
    selectedTool = tool;

    // 박스 선택 도구 선택 시 canvas.selection 활성화
    if (tool === 'select') {
        canvas.selection = false; // 다중 선택 가능
        canvas.getObjects('line').forEach(line => {
            line.selectable = false; // 선택 가능
            line.evented = false; // 마우스 이벤트 가능
        });
    } else {
        canvas.discardActiveObject();
        canvas.selection = false;

        // 남은 개별 선 객체도 비선택/이벤트 불가로 설정
        canvas.getObjects('line').forEach(line => {
            line.selectable = false;
            line.evented = false;
        });
    }
    scheduleRender();
}

// 렌더링 요청이 많아도 화면 렌더링은 일정 프레임에 1회로 제한
function scheduleRender() {
    const now = performance.now();
    if (!renderScheduled && now - lastRenderTime >= RENDER_INTERVAL) {
        renderScheduled = true;
        requestAnimationFrame(() => {
            canvas.requestRenderAll();
            lastRenderTime = performance.now();
            renderScheduled = false;
        });
    }
}

// 메시지가 번호순서대로 처리되도록하는 함수
function handleMessage(msg, callback) {
    const seq = msg.seq;
    console.log(msg, lastSeq);
    // seq가 없으면 바로 처리 (순서가 중요하지 않은 메시지)
    if (seq === undefined || seq === null) {
        callback(msg);
        return;
    }
    if (seq === lastSeq + 1) {
        // 바로 처리
        callback(msg);
        lastSeq++;
        // 대기 중인 다음 메시지 처리
        while (pendingQueue[lastSeq + 1]) {
            console.log(msg, lastSeq);
            const next = pendingQueue[lastSeq + 1];
            delete pendingQueue[lastSeq + 1];
            next.callback(next.msg);
            lastSeq++;
        }
    } else if (seq > lastSeq + 1) {
        // 순서 안 맞으면 큐에 대기
        pendingQueue[seq] = { msg, callback };
    } else {
        // 이미 처리된 메시지
        console.log("⚠️ 중복 메시지", seq);
    }
}

// currentAction 초기화 함수
function initializeCurrentAction(msg){
    if (currentAction && currentAction.targets) {
        pushToUndoStack();

        // pushToUndoStack 메시지는 UI 즉시 전송
        const pushMsg = {
            senderId: senderId,
            seq: mySeq++
        };
        safeSend('/app/pushToUndoStack', pushMsg);

        const actionCopy = JSON.parse(JSON.stringify(currentAction));

        const undoRedoStackDTO = {
            roomId: roomId,
            undoStack: JSON.parse(JSON.stringify(undoStack)),
            redoStack: JSON.parse(JSON.stringify(redoStack))
        };

        undoRedoQueue = undoRedoQueue.then(async () => {
            // DB 저장
            await saveCanvasActionToDB(actionCopy.type, actionCopy.targets.map(t => ({
                uuid: t.uuid,
                stroke: t.stroke,
                x1: t.x1,
                y1: t.y1,
                x2: t.x2,
                y2: t.y2
            })));

            // undo/redo 스택 DB 저장
            await saveUndoRedoStack(undoRedoStackDTO);
        }).catch(console.error);

        // currentAction 리셋 & 메시지 전송
        resetCurrentAction();

        const resetMsg = {
            senderId: senderId,
            seq: mySeq++
        };
        safeSend('/app/resetCurrentAction', resetMsg);
    }
    const type = msg.type
    if (type === 'draw' || type === 'erase'){
        currentAction = {
            type: type, // 'draw' | 'erase' | 'move' | 'rotate' | 'scale' ...
            targets: [], // 영향을 받은 객체들
            before: [], // 작업 전 상태
            after: [] // 작업 후 상태
        };
    }
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
        const smooth = stabilize(lastPoint, currentPointer);
        if (selectedTool === 'draw') {
            drawInterpolatedLine({x1: lastPoint.x, y1: lastPoint.y, x2: smooth.x, y2: smooth.y}, currentColor);
        }
        if (selectedTool === 'erase') {
            eraseInterpolated({x1: lastPoint.x, y1: lastPoint.y, x2: smooth.x, y2: smooth.y});

            message = {
                roomId: roomId,
                senderId: senderId,
                seq: mySeq++,
                x1: lastPoint.x,
                y1: lastPoint.y,
                x2: smooth.x,
                y2: smooth.y
            }
            safeSend("/app/erase", message);
        }
        lastPoint = { ...smooth };
        scheduleRender();
    }

    // 이동, 회전 스케일된 객체 좌표 업데이트 및 메시지 전송
    if (isTransform) {
        const activeSelection = canvas.getActiveObject();
        if (currentAction && currentAction.targets.length > 0) {
            const positions = currentAction.targets.map(obj => ({
                uuid: obj.uuid,
                left: activeSelection.left + (activeSelection.width / 2) + obj.left,
                top: activeSelection.top + (activeSelection.height / 2) + obj.top
            }));

            // 객체 이동, 회전, 스케일 메시지 전송
            const message = {
                senderId: senderId,
                seq: mySeq++,
                positions: positions
            };
            safeSend("/app/select", message);
        }
        // 이거 false안하면 transform 끝난 시점에도 계속 메시지 송신
        isTransform = false;
    }

    if (isShapeDrawing && shapeCurrentPoint) {
        // 이전 포인터가 없거나 좌표가 달라졌을 때만 처리
        if (currentShape === 'rect' && rectStartPoint) {
            if (!prevShapeCurrentPoint ||
                prevShapeCurrentPoint.x !== shapeCurrentPoint.x ||
                prevShapeCurrentPoint.y !== shapeCurrentPoint.y) {

                message = {
                    senderId: senderId,
                    seq: mySeq++,
                    uuid: generateUUID(),
                    stroke: currentColor,
                    x1: rectStartPoint.x,
                    y1: rectStartPoint.y,
                    x2: shapeCurrentPoint.x,
                    y2: shapeCurrentPoint.y
                };

                drawPreviewRectangle(message);
                safeSend("/app/rectangle", message);
                scheduleRender();

                prevShapeCurrentPoint = { ...shapeCurrentPoint }; // 좌표 저장
            }
        }
        if (currentShape === "triangle" && triangleFirstPoint) {
            if (!prevShapeCurrentPoint ||
                prevShapeCurrentPoint.x !== shapeCurrentPoint.x ||
                prevShapeCurrentPoint.y !== shapeCurrentPoint.y) {

                if (!triangleSecondPoint) {
                    const message = {
                        senderId,
                        seq: mySeq++,
                        uuid: generateUUID(),
                        stroke: currentColor,
                        x1: triangleFirstPoint.x,
                        y1: triangleFirstPoint.y,
                        x2: shapeCurrentPoint.x,
                        y2: shapeCurrentPoint.y
                    };

                    drawPreviewLine(message);
                    safeSend("/app/line", message);
                    scheduleRender();

                    prevShapeCurrentPoint = { ...shapeCurrentPoint };
                } else {
                    const message = {
                        senderId,
                        seq: mySeq++,
                        uuid: generateUUID(),
                        stroke: currentColor,
                        x1: triangleFirstPoint.x,
                        y1: triangleFirstPoint.y,
                        x2: triangleSecondPoint.x,
                        y2: triangleSecondPoint.y,
                        x3: shapeCurrentPoint.x,
                        y3: shapeCurrentPoint.y
                    };

                    drawPreviewTriangle(message);
                    safeSend("/app/triangle", message);
                    scheduleRender();

                    prevShapeCurrentPoint = { ...shapeCurrentPoint };
                }
            }
        }

        if (currentShape === "circle" && circleCenterPoint) {
            if (!prevShapeCurrentPoint ||
                prevShapeCurrentPoint.x !== shapeCurrentPoint.x ||
                prevShapeCurrentPoint.y !== shapeCurrentPoint.y) {

                message = {
                    senderId: senderId,
                    seq: mySeq++,
                    uuid: generateUUID(),
                    stroke: currentColor,
                    centerX: circleCenterPoint.x,
                    centerY: circleCenterPoint.y,
                    x: shapeCurrentPoint.x,
                    y: shapeCurrentPoint.y
                };

                drawPreviewCircle(message);
                safeSend("/app/circle", message);
                scheduleRender();

                prevShapeCurrentPoint = { ...shapeCurrentPoint }; // 좌표 저장
            }
        }

        if (currentShape === "line" && lineStartPoint) {
            if (!prevShapeCurrentPoint ||
                prevShapeCurrentPoint.x !== shapeCurrentPoint.x ||
                prevShapeCurrentPoint.y !== shapeCurrentPoint.y) {

                message = {
                    senderId: senderId,
                    seq: mySeq++,
                    uuid: generateUUID(),
                    stroke: currentColor,
                    x1: lineStartPoint.x,
                    y1: lineStartPoint.y,
                    x2: shapeCurrentPoint.x,
                    y2: shapeCurrentPoint.y
                };

                drawPreviewLine(message);
                safeSend("/app/line", message);
                scheduleRender();

                prevShapeCurrentPoint = { ...shapeCurrentPoint }; // 좌표 저장
            }
        }
    } else {
        prevShapeCurrentPoint = null; // 드로잉 끝나면 초기화
    }



    // requestAnimationFrame : rAF
    // 브라우저에서 화면을 다시 그릴 타이밍에 맞춰 함수를 호출하도록 예약하는 JavaScript 함수
    requestAnimationFrame(loop);
}
loop();


// db 저장 함수
async function saveCanvasActionToDB(actionType, payload) {
    try {
        const response = await fetch('/room/saveCanvasAction', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                roomId: roomId,
                actionType: actionType, // 'draw', 'erase', 'select'
                payload: payload      // draw: line 배열, erase: line uuid 배열, select: 객체 위치 정보 등
            })
        });
        if (!response.ok) {
            console.error('DB 저장 실패');
        }
    } catch (e) {
        console.error('서버 연결 실패:', e);
    }
}

function generateUUID() {
    if (crypto && crypto.randomUUID) {
        return crypto.randomUUID();
    } else {
        // fallback (간단한 임시 UUID, 충돌 가능성 거의 없음)
        return 'xxxx-xxxx-xxxx-xxxx'.replace(/[x]/g, () =>
            Math.floor(Math.random() * 16).toString(16)
        );
    }
}

// 그리기 보정
function stabilize(prev, curr) {
    return {
        x: prev.x + (curr.x - prev.x) * SMOOTH_ALPHA,
        y: prev.y + (curr.y - prev.y) * SMOOTH_ALPHA
    };
}

// 그리기
function drawLine(msg){
    // 색상, 두께 등 나중에 추가하기
    // 길이가 0이면 skip
    if (msg.x1 === msg.x2 && msg.y1 === msg.y2) return;
    const line = new fabric.Line([msg.x1, msg.y1, msg.x2, msg.y2], {
        uuid: msg.uuid,
        stroke: msg.stroke || "#000",
        strokeWidth: 2,
        selectable: false,
        evented: false,
        strokeLineCap: 'round', // 끝점 둥글게
        strokeLineJoin: 'round' // 연결점 부드럽게
    });
    canvas.add(line);

    // 직렬화용 정보만 currentAction.targets에 저장
    if (currentAction && currentAction.type === 'draw') {
        currentAction.targets.push({
            uuid: msg.uuid,
            x1: msg.x1,
            y1: msg.y1,
            x2: msg.x2,
            y2: msg.y2,
            stroke: line.stroke,
            strokeWidth: line.strokeWidth
        });
    }
    return line;
}

// 선 보간 함수
function drawInterpolatedLine(msg, stroke) {
    const p1 = {x: msg.x1, y:msg.y1}
    const p2 = {x: msg.x2, y:msg.y2}
    const dx = p2.x - p1.x;
    const dy = p2.y - p1.y;

    let distance = Math.sqrt(dx * dx + dy * dy);
    if (distance === 0) distance = 1; // 최소 1 step 보장

    const steps = Math.max(Math.floor(distance / DRAW_STEP), 1);
    const stepX = dx / steps;
    const stepY = dy / steps;

    let prevX = p1.x;
    let prevY = p1.y;

    for (let i = 1; i <= steps; i++) {
        const x = p1.x + stepX * i;
        const y = p1.y + stepY * i;
        const newObjectId = generateUUID();
        drawLine({x1: prevX, y1: prevY, x2: x, y2: y, uuid: newObjectId, stroke: stroke});
        message = {
            senderId: senderId,
            seq: mySeq++,
            uuid: newObjectId,
            x1: prevX,
            y1: prevY,
            x2: x,
            y2: y,
            stroke: stroke
        }
        safeSend("/app/draw", message);

        prevX = x;
        prevY = y;
    }
}

// 지우기
function eraseLine(x, y, threshold = 10) {
    const objects = canvas.getObjects('line');
    const toRemove = [];

    objects.forEach(line => {
        const [x1, y1, x2, y2] = line.get('points') || [line.x1, line.y1, line.x2, line.y2];
        const dist = distancePointToLine(x, y, x1, y1, x2, y2);
        if (dist <= threshold) {
            toRemove.push(line);

            // currentAction.targets에도 저장
            if (currentAction && currentAction.type === 'erase') {
                currentAction.targets.push({
                    uuid: line.uuid,
                    x1: line.x1,
                    y1: line.y1,
                    x2: line.x2,
                    y2: line.y2,
                    stroke: line.stroke,
                    strokeWidth: line.strokeWidth
                });
            }
        }
    });

    // Canvas에서 제거
    toRemove.forEach(line => canvas.remove(line));
}

// 지우개 보간 함수
function eraseInterpolated(msg) {
    const p1 = {x: msg.x1, y:msg.y1}
    const p2 = {x: msg.x2, y:msg.y2}
    const dx = p2.x - p1.x;
    const dy = p2.y - p1.y;

    const distance = Math.sqrt(dx * dx + dy * dy);
    if (distance === 0) return [];

    const steps = Math.ceil(distance / ERASE_STEP);

    let removeLines = [];
    for (let i = 0; i <= steps; i++) {
        const x = p1.x + (dx / steps) * i;
        const y = p1.y + (dy / steps) * i;
        eraseLine(x, y, ERASE_RADIUS);
    }
}

// 점(x0,y0)과 선(x1,y1)-(x2,y2) 사이 최소 거리 계산 함수
function distancePointToLine(x0, y0, x1, y1, x2, y2) {
    const A = x0 - x1;
    const B = y0 - y1;
    const C = x2 - x1;
    const D = y2 - y1;

    const dot = A * C + B * D;
    const len_sq = C * C + D * D;

    let param = -1;
    if (len_sq !== 0) param = dot / len_sq;

    let xx, yy;
    if (param < 0) {
        xx = x1; yy = y1;
    } else if (param > 1) {
        xx = x2; yy = y2;
    } else {
        xx = x1 + param * C;
        yy = y1 + param * D;
    }

    const dx = x0 - xx;
    const dy = y0 - yy;
    return Math.sqrt(dx * dx + dy * dy);
}

function undo() {
    if (undoStack.length === 0) return;

    const action = undoStack.pop();

    switch (action.type) {
        case 'draw':
            // 그린 것 제거
            action.targets.forEach(t => {
                const obj = canvas.getObjects().find(o => o.uuid === t.uuid);
                if (obj) canvas.remove(obj);
            });

            // DB에서도 제거
            if (action.targets.length){
                const copiedTargets = [...action.targets];
                undoRedoQueue = undoRedoQueue.then(async () => {
                    try {
                        await saveCanvasActionToDB('erase', copiedTargets);
                    } catch (e) {
                        console.error('DB 저장 실패:', e);
                    }
                })
            }
            break;

        case 'erase':
            // 지운 것 복구
            action.targets.forEach(t => {
                // 이미 있으면 skip
                if (canvas.getObjects().some(o => o.uuid === t.uuid)) return;

                const line = new fabric.Line(
                    [t.x1, t.y1, t.x2, t.y2],
                    {
                        uuid: t.uuid,
                        stroke: t.stroke || '#000',
                        strokeWidth: t.strokeWidth || 2,
                        selectable: false,
                        evented: false,
                        strokeLineCap: 'round',
                        strokeLineJoin: 'round'
                    }
                );
                canvas.add(line);
            });
            // DB에서도 복구
            if (action.targets.length){
                const copiedTargets = [...action.targets];
                undoRedoQueue = undoRedoQueue.then(async () => {
                    try {
                        await saveCanvasActionToDB('draw', copiedTargets);
                    } catch (e) {
                        console.error('DB 저장 실패:', e);
                    }
                })
            }
            break;

        case 'select':
            action.targets.forEach((t, idx) => {
                const obj = canvas.getObjects().find(o => o.uuid === t.uuid);
                if (!obj) return;

                const state = action.before[idx];
                obj.set({ left: state.left, top: state.top });
                obj.setCoords();
            });
            break;
    }

    redoStack.push(action);
    scheduleRender();

    const undoRedoStackDTO = {
        roomId: roomId,
        undoStack: JSON.parse(JSON.stringify(undoStack)),
        redoStack: JSON.parse(JSON.stringify(redoStack))
    };

    undoRedoQueue = undoRedoQueue.then(async () => {
        try {
            saveUndoRedoStack(undoRedoStackDTO);
        } catch (e) {
            console.error('DB 저장 실패:', e);
        }
    })
}

function redo() {
    if (redoStack.length === 0) return;

    const action = redoStack.pop();

    switch (action.type) {
        case 'draw':
            // 다시 그리기
            action.targets.forEach(t => {
                if (canvas.getObjects().some(o => o.uuid === t.uuid)) return;

                const line = new fabric.Line(
                    [t.x1, t.y1, t.x2, t.y2],
                    {
                        uuid: t.uuid,
                        stroke: t.stroke || '#000',
                        strokeWidth: t.strokeWidth || 2,
                        selectable: false,
                        evented: false,
                        strokeLineCap: 'round',
                        strokeLineJoin: 'round'
                    }
                );
                canvas.add(line);
            });
            // DB 반영: draw 액션 저장
            if (action.targets.length){
                const copiedTargets = [...action.targets];
                undoRedoQueue = undoRedoQueue.then(async () => {
                    try {
                        await saveCanvasActionToDB('draw', copiedTargets);
                    } catch (e) {
                        console.error('DB 저장 실패:', e);
                    }
                })
            }

            break;

        case 'erase':
            // 다시 지우기
            action.targets.forEach(t => {
                const obj = canvas.getObjects().find(o => o.uuid === t.uuid);
                if (obj) canvas.remove(obj);
            });
            // DB 반영: erase 액션 저장
            if (action.targets.length){
                const copiedTargets = [...action.targets];
                undoRedoQueue = undoRedoQueue.then(async () => {
                    try {
                        await saveCanvasActionToDB('erase', copiedTargets);
                    } catch (e) {
                        console.error('DB 저장 실패:', e);
                    }
                })
            }


        case 'select':
            action.targets.forEach((t, idx) => {
                const obj = canvas.getObjects().find(o => o.uuid === t.uuid);
                if (!obj) return;

                const state = action.after[idx];
                obj.set({ left: state.left, top: state.top });
                obj.setCoords();
            });
            break;
    }

    undoStack.push(action);
    scheduleRender();

    const undoRedoStackDTO = {
        roomId: roomId,
        undoStack: JSON.parse(JSON.stringify(undoStack)),
        redoStack: JSON.parse(JSON.stringify(redoStack))
    };

    undoRedoQueue = undoRedoQueue.then(async () => {
        try {
            saveUndoRedoStack(undoRedoStackDTO);
        } catch (e) {
            console.error('DB 저장 실패:', e);
        }
    })
}

// 사각형 미리보기 그리기
function drawPreviewRectangle(msg) {
    // 미리보기 사각형이 이미 있으면 제거
    if (previewRect[msg.senderId]) {
        canvas.remove(previewRect[msg.senderId]);
        previewRect[msg.senderId] = null;
    }
    console.log(previewRect);
    const left = Math.min(msg.x1, msg.x2);
    const top = Math.min(msg.y1, msg.y2);
    const width = Math.abs(msg.x2 - msg.x1);
    const height = Math.abs(msg.y2 - msg.y1);

    previewRectangle = new fabric.Rect({
        uuid: msg.uuid,
        left: left,
        top: top,
        width: width,
        height: height,
        fill: 'transparent',
        stroke: msg.stroke,
        strokeWidth: 2,
        selectable: false,
        evented: false,
        strokeLineCap: 'round',
        strokeLineJoin: 'round'
    });
    canvas.add(previewRectangle);
    previewRect[msg.senderId] = previewRectangle;
}

// 사각형 그리기
function finalizeRectangle(msg) {
    console.log("미리보기 사각형 제거")
    console.log(previewRect)
    console.log(previewRect[msg.senderId])
    // 미리보기 사각형 제거
    if (previewRect[msg.senderId]) {
        canvas.remove(previewRect[msg.senderId]);
        previewRect[msg.senderId] = null;
    }

    const left = Math.min(msg.x1, msg.x2);
    const top = Math.min(msg.y1, msg.y2);
    const right = Math.max(msg.x1, msg.x2);
    const bottom = Math.max(msg.y1, msg.y2);

    const corners = [
        { x: left,  y: top },    // top-left
        { x: right, y: top },    // top-right
        { x: right, y: bottom }, // bottom-right
        { x: left,  y: bottom }  // bottom-left
    ];

    const messages = [];

    // 4변에 대해 작은 선으로 분할
    for (let i = 0; i < 4; i++) {
        const start = corners[i];
        const end = corners[(i + 1) % 4];

        const dx = end.x - start.x;
        const dy = end.y - start.y;
        const distance = Math.hypot(dx, dy);
        const steps = Math.max(Math.floor(distance / DRAW_STEP), 1);
        const stepX = dx / steps;
        const stepY = dy / steps;

        for (let j = 0; j < steps; j++) {
            const x1 = start.x + stepX * j;
            const y1 = start.y + stepY * j;
            const x2 = start.x + stepX * (j + 1);
            const y2 = start.y + stepY * (j + 1);

            const newObjectId = generateUUID();
            const message = {
                uuid: newObjectId,
                x1: x1,
                y1: y1,
                x2: x2,
                y2: y2,
                stroke: currentColor
            }
            drawLine(message);
            messages.push(message);
        }
    }
    safeSend("/app/removePreview", {senderId: senderId, seq: mySeq++});
    safeSend("/app/drawLines", {senderId: senderId, seq: mySeq++, messages: messages});
}

function drawPreviewTriangle(msg) {
    if (previewTriangle[msg.senderId]) {
        canvas.remove(previewTriangle[msg.senderId]);
        previewTriangle[msg.senderId] = null;
    }

    if (previewLine[msg.senderId]) {
        canvas.remove(previewLine[msg.senderId]);
        previewLine[msg.senderId] = null;
    }

    previewTri = new fabric.Polygon([
        { x: msg.x1, y: msg.y1 },
        { x: msg.x2, y: msg.y2 },
        { x: msg.x3, y: msg.y3 }
    ], {
        fill: 'transparent',
        stroke: msg.stroke,
        strokeWidth: 2,
        selectable: false,
        evented: false,
        strokeLineCap: 'round',
        strokeLineJoin: 'round'
    });
    // objectCaching: false

    canvas.add(previewTri);
    previewTriangle[msg.senderId] = previewTri;
}

function finalizeTriangle(msg) {
    // preview 제거
    if (previewTriangle[msg.senderId]) {
        canvas.remove(previewTriangle[msg.senderId]);
        previewTriangle[msg.senderId] = null;
    }

    if (previewLine[msg.senderId]) {
        canvas.remove(previewLine[msg.senderId]);
        previewLine[msg.senderId] = null;
    }

    const points = [
        {x: msg.x1, y: msg.y1}, // 첫 클릭
        {x: msg.x2, y: msg.y2}, // 두 번째 클릭
        {x: msg.x3, y: msg.y3}  // mouse up 지점
    ];

    const messages = [];

    // 삼각형의 3변을 순회
    for (let i = 0; i < 3; i++) {
        const start = points[i];
        const end = points[(i + 1) % 3];

        const dx = end.x - start.x;
        const dy = end.y - start.y;
        const distance = Math.hypot(dx, dy);

        const steps = Math.max(Math.floor(distance / DRAW_STEP), 1);
        const stepX = dx / steps;
        const stepY = dy / steps;

        // 한 변을 작은 선들로 분할
        for (let j = 0; j < steps; j++) {
            const x1 = start.x + stepX * j;
            const y1 = start.y + stepY * j;
            const x2 = start.x + stepX * (j + 1);
            const y2 = start.y + stepY * (j + 1);

            const newObjectId = generateUUID();
            const message = {
                uuid: newObjectId,
                x1: x1,
                y1: y1,
                x2: x2,
                y2: y2,
                stroke: currentColor
            }
            drawLine(message);
            messages.push(message);
        }
    }
    safeSend("/app/removePreview", {senderId: senderId, seq: mySeq++});
    safeSend("/app/drawLines", {senderId: senderId, seq: mySeq++, messages: messages});
}

function drawPreviewLine(msg) {
    // 이전 미리보기 제거
    if (previewLine[msg.senderId]) {
        canvas.remove(previewLine[msg.senderId]);
        previewLine[msg.senderId] = null;
    }

    // 새로운 선 그리기
    previewL = new fabric.Line(
        [msg.x1, msg.y1, msg.x2, msg.y2],
        {
            uuid: msg.uuid,
            stroke: msg.stroke || "#000",
            strokeWidth: 2,
            selectable: false,
            evented: false,
            strokeLineCap: 'round', // 끝점 둥글게
            strokeLineJoin: 'round' // 연결점 부드럽게
        }
    );
    canvas.add(previewL);
    previewLine[msg.senderId] = previewL;
}

function finalizeLine(msg) {
    // 미리보기 제거
    if (previewLine[msg.senderId]) {
        canvas.remove(previewLine[msg.senderId]);
        previewLine[msg.senderId] = null;
    }

    const dx = msg.x2 - msg.x1;
    const dy = msg.y2 - msg.y1;
    const distance = Math.hypot(dx, dy);

    const steps = Math.max(Math.floor(distance / DRAW_STEP), 1);
    const stepX = dx / steps;
    const stepY = dy / steps;

    const messages = [];

    // DRAW_STEP 단위로 분할
    for (let j = 0; j < steps; j++) {
        const x1 = msg.x1 + stepX * j;
        const y1 = msg.y1 + stepY * j;
        const x2 = msg.x1 + stepX * (j + 1);
        const y2 = msg.y1 + stepY * (j + 1);

        const newObjectId = generateUUID();
        const message = {
            uuid: newObjectId,
            x1: x1,
            y1: y1,
            x2: x2,
            y2: y2,
            stroke: currentColor
        }
        drawLine(message);
        messages.push(message);
    }
    safeSend("/app/removePreview", {senderId: senderId, seq: mySeq++});
    safeSend("/app/drawLines", {senderId: senderId, seq: mySeq++, messages: messages});
}

// 미리보기 원
function drawPreviewCircle(msg) {
    // 이전 미리보기 제거
    if (previewCircle[msg.senderId]) {
        canvas.remove(previewCircle[msg.senderId]);
        previewCircle[msg.senderId] = null;
    }

    // 반지름 계산
    const dx = msg.x - msg.centerX;
    const dy = msg.y - msg.centerY;
    const radius = Math.hypot(dx, dy);

    previewCir = new fabric.Circle({
        left: msg.centerX - radius,
        top: msg.centerY - radius,
        radius: radius,
        fill: 'transparent',
        stroke: msg.stroke || "#000",
        strokeWidth: 2,
        selectable: false,
        evented: false
    });

    canvas.add(previewCir);
    previewCircle[msg.senderId] = previewCir;
}

// 확정 원
function finalizeCircle(msg) {
    // 이전 미리보기 제거
    if (previewCircle[msg.senderId]) {
        canvas.remove(previewCircle[msg.senderId]);
        previewCircle[msg.senderId] = null;
    }

    const dx = msg.x - msg.centerX;
    const dy = msg.y - msg.centerY;
    const radius = Math.hypot(dx, dy);

    const steps = Math.max(Math.floor(2 * Math.PI * radius / CIRCLE_DRAW_STEP), 1); // 원 둘레 DRAW_STEP 단위
    const angleStep = (2 * Math.PI) / steps;

    const messages = [];

    for (let i = 0; i < steps; i++) {
        const angle1 = angleStep * i;
        const angle2 = angleStep * (i + 1);

        const x1 = msg.centerX + radius * Math.cos(angle1);
        const y1 = msg.centerY + radius * Math.sin(angle1);
        const x2 = msg.centerX + radius * Math.cos(angle2);
        const y2 = msg.centerY + radius * Math.sin(angle2);

        const newObjectId = generateUUID();
        message = {
            uuid: newObjectId,
            x1: x1,
            y1: y1,
            x2: x2,
            y2: y2,
            stroke: currentColor
        }
        drawLine(message);
        messages.push(message);
    }
    safeSend("/app/removePreview", {senderId: senderId, seq: mySeq++});
    safeSend("/app/drawLines", {senderId: senderId, seq: mySeq++, messages: messages});
}

function removePreview(msg) {
    if (previewRect[msg.senderId]) {
        canvas.remove(previewRect[msg.senderId]);
        previewRect[msg.senderId] = null;
    }

    if (previewTriangle[msg.senderId]) {
        canvas.remove(previewTriangle[msg.senderId]);
        previewTriangle[msg.senderId] = null;
    }

    if (previewLine[msg.senderId]) {
        canvas.remove(previewLine[msg.senderId]);
        previewLine[msg.senderId] = null;
    }

    if (previewCircle[msg.senderId]) {
        canvas.remove(previewCircle[msg.senderId]);
        previewCircle[msg.senderId] = null;
    }
}

function drawLines(msg) {
    msg.messages.forEach(message => drawLine(message));
}

// undo, redo 메시지 전송
function sendUndoRedoMessage(type){
    const message = {
        senderId: senderId,
        seq: mySeq++,
        type: type
    }
    safeSend('/app/undoRedo', message)
}

// draw_data
function loadCanvas(drawDataList) {
    if (!drawDataList || !Array.isArray(drawDataList)) return;

    // 기존 캔버스 초기화
    canvas.getObjects('line').forEach(line => canvas.remove(line));

    // 받아온 데이터로 캔버스에 선 그리기
    drawDataList.forEach(data => {
        const line = new fabric.Line([data.x1, data.y1, data.x2, data.y2], {
            uuid: data.uuid,
            stroke: data.stroke,
            strokeWidth: 2,
            selectable: false,
            evented: false,
            strokeLineCap: 'round',
            strokeLineJoin: 'round'
        });
        canvas.add(line);
    });
}

async function saveUndoRedoStack(undoRedoStackDTO) {
    try {
        const response = await fetch(`/room/saveUndoRedoStack?roomId=${roomId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(undoRedoStackDTO)
        });
        if (!response.ok) {
            console.error('stack DB 저장 실패');
        }
    } catch (e) {
        console.error('서버 연결 실패:', e);
    }
}

// redo_undo_stack
function loadUndoRedo(stack) {
    if (!stack) return;

    undoStack = Array.isArray(stack.undoStack) ? stack.undoStack.slice() : [];
    redoStack = Array.isArray(stack.redoStack) ? stack.redoStack.slice() : [];
}

/**
 * 로딩 화면 표시
 */
function showLoading() {
    let loader = document.getElementById('loading-overlay');
    if (!loader) {
        loader = document.createElement('div');
        loader.id = 'loading-overlay';

        const spinner = document.createElement('div');
        spinner.className = 'spinner';
        loader.appendChild(spinner);

        document.body.appendChild(loader);
    }

    loader.style.display = 'flex';
}

/**
 * 로딩 화면 숨기기
 */
function hideLoading() {
    const loader = document.getElementById('loading-overlay');
    if (loader) loader.style.display = 'none';
}

/**
 * 캔버스와 메시지 입력 영역 비활성화
 */
function disableCanvasAndMessage() {
    // 캔버스 상호작용 차단
    if (canvas && canvas.upperCanvasEl) {
        canvas.upperCanvasEl.style.pointerEvents = 'none';
    }

    // 메시지 입력 영역
    const messageTextarea = document.querySelector('textarea[name="message"]');
    const fileInput = document.getElementById('file');
    const sendFileBtn = document.getElementById('sendFileBtn');

    if (messageTextarea) messageTextarea.disabled = true;
    if (fileInput) fileInput.disabled = true;
    if (sendFileBtn) sendFileBtn.disabled = true;
}


/**
 * 캔버스와 메시지 입력 영역 활성화
 */
function enableCanvasAndMessage() {
    // 캔버스 상호작용 허용
    if (canvas && canvas.upperCanvasEl) {
        canvas.upperCanvasEl.style.pointerEvents = 'auto';
    }

    // 메시지 입력 영역
    const messageTextarea = document.querySelector('textarea[name="message"]');
    const fileInput = document.getElementById('file');
    const sendFileBtn = document.getElementById('sendFileBtn');

    if (messageTextarea) messageTextarea.disabled = false;
    if (fileInput) fileInput.disabled = false;
    if (sendFileBtn) sendFileBtn.disabled = false;
}

document.addEventListener('DOMContentLoaded', () => {
    window.addEventListener('resize', resizeCanvas);
    resizeCanvas();

    // 접근 권한 체크. 권한이 있는 사용자만 캔버스, 메시지 이용가능
    const canUseCanvasAndMessage =
        roomDTO.status !== 'PENDING' && roomDTO.status !== 'COMPLETED' &&
        (senderId === roomDTO.studentId || senderId === roomDTO.mentorId);

    // 캔버스 활성/비활성
    if (canUseCanvasAndMessage) {
        enableCanvasAndMessage();
    } else {
        disableCanvasAndMessage();
    }


    // canvas 이벤트 바인딩
    canvas.on('mouse:down', (opt) => {
        if (opt.e.altKey) { // Alt 누르고 드래그
            isPanning = true;
            return;
        } else {
            isDrawing = selectedTool === 'draw' || selectedTool === 'erase';
            lastPoint = canvas.getPointer(opt.e);
            currentPointer = lastPoint;
        }

        const pointer = canvas.getPointer(opt.e);

        if (isDrawing) {
            initializeCurrentAction({type: selectedTool});

            const message = {
                senderId: senderId,
                seq: mySeq++,
                type: selectedTool
            }
            safeSend('/app/initializeCurrentAction', message);
        }

        if (selectedTool === 'shape' && currentShape === 'rect') {
            rectStartPoint = pointer;
            shapeCurrentPoint = pointer;
            isShapeDrawing = true;
        }

        if (selectedTool === 'shape' && currentShape === 'triangle') {
            if (!triangleFirstPoint) {
                // 첫 클릭: 첫 점 저장
                isShapeDrawing = true;
                triangleFirstPoint = pointer;
            } else if (!triangleSecondPoint) {
                // 두 번째 클릭: 두 번째 점 저장
                triangleSecondPoint = pointer;
            }
        }

        if (selectedTool === 'shape' && currentShape === 'circle') {
            circleCenterPoint = pointer;
            shapeCurrentPoint = pointer;
            isShapeDrawing = true;
        }

        if (selectedTool === 'shape' && currentShape === 'line') {
            lineStartPoint = pointer;
            shapeCurrentPoint = pointer;
            isShapeDrawing = true;
        }
    });

    canvas.on('mouse:move', (opt) => {
        if (isDrawing) {
            currentPointer = canvas.getPointer(opt.e);
        };

        if (isPanning) {
            const e = opt.e;
            const vpt = canvas.viewportTransform;
            vpt[4] += e.movementX;
            vpt[5] += e.movementY;
            scheduleRender();
        }

        if (isShapeDrawing) {
            const pointer = canvas.getPointer(opt.e);
            shapeCurrentPoint = pointer;
        }
    });

    canvas.on('mouse:up', async (opt) => {
        if (isDrawing) {
            isDrawing = false;
            currentPointer = null;
        }

        if (isPanning) {
            isPanning = false;
        }

        if (isShapeDrawing) {
            const pointer = canvas.getPointer(opt.e);
            shapeCurrentPoint = pointer;

            if (currentShape === "rect" && rectStartPoint) {
                initializeCurrentAction({type: 'draw'});

                let message = {
                    senderId: senderId,
                    seq: mySeq++,
                    type: 'draw'
                }
                safeSend('/app/initializeCurrentAction', message);

                message = {
                    senderId: senderId,
                    uuid: generateUUID(),
                    stroke: currentColor,
                    x1: rectStartPoint.x,
                    y1: rectStartPoint.y,
                    x2: shapeCurrentPoint.x,
                    y2: shapeCurrentPoint.y
                };

                finalizeRectangle(message);

                rectStartPoint = null;
                isShapeDrawing = false;
            }

            else if (currentShape === "triangle" && triangleFirstPoint && triangleSecondPoint) {
                initializeCurrentAction({ type: 'draw' });

                let message = {
                    senderId: senderId,
                    seq: mySeq++,
                    type: 'draw'
                }
                safeSend('/app/initializeCurrentAction', message);

                message = {
                    senderId: senderId,
                    uuid: generateUUID(),
                    stroke: currentColor,
                    x1: triangleFirstPoint.x,
                    y1: triangleFirstPoint.y,
                    x2: triangleSecondPoint.x,
                    y2: triangleSecondPoint.y,
                    x3: shapeCurrentPoint.x,
                    y3: shapeCurrentPoint.y
                };

                finalizeTriangle(message);

                // 삼각형 상태 리셋
                triangleFirstPoint = null;
                triangleSecondPoint = null;
                isShapeDrawing = false;
            }

            else if (currentShape === "circle" && circleCenterPoint) {
                initializeCurrentAction({type: 'draw'});

                let message = {
                    senderId: senderId,
                    seq: mySeq++,
                    type: 'draw'
                }
                safeSend('/app/initializeCurrentAction', message);

                message = {
                    senderId: senderId,
                    uuid: generateUUID(),
                    stroke: currentColor,
                    centerX: circleCenterPoint.x,
                    centerY: circleCenterPoint.y,
                    x: shapeCurrentPoint.x,
                    y: shapeCurrentPoint.y
                };

                finalizeCircle(message);

                circleCenterPoint = null;
                isShapeDrawing = false;
            }

            else if (currentShape === "line" && lineStartPoint) {
                initializeCurrentAction({type: 'draw'});

                let message = {
                    senderId: senderId,
                    seq: mySeq++,
                    type: 'draw'
                }
                safeSend('/app/initializeCurrentAction', message);

                message = {
                    senderId: senderId,
                    uuid: generateUUID(),
                    stroke: currentColor,
                    x1: lineStartPoint.x,
                    y1: lineStartPoint.y,
                    x2: shapeCurrentPoint.x,
                    y2: shapeCurrentPoint.y
                };

                finalizeLine(message);

                lineStartPoint = null;
                isShapeDrawing = false;
            }
        }

        // 공통 로직
        if (currentAction && currentAction.targets.length > 0) {
            // UI 즉시 반영: undoStack에 push
            pushToUndoStack();

            // pushToUndoStack 메시지는 UI 즉시 전송
            const pushMsg = {
                senderId: senderId,
                seq: mySeq++
            };
            safeSend('/app/pushToUndoStack', pushMsg);

            const actionCopy = JSON.parse(JSON.stringify(currentAction));

            const undoRedoStackDTO = {
                roomId: roomId,
                undoStack: JSON.parse(JSON.stringify(undoStack)),
                redoStack: JSON.parse(JSON.stringify(redoStack))
            };

            undoRedoQueue = undoRedoQueue.then(async () => {
                // DB 저장
                await saveCanvasActionToDB(actionCopy.type, actionCopy.targets.map(t => ({
                    uuid: t.uuid,
                    stroke: t.stroke,
                    x1: t.x1,
                    y1: t.y1,
                    x2: t.x2,
                    y2: t.y2
                })));

                // undo/redo 스택 DB 저장
                await saveUndoRedoStack(undoRedoStackDTO);
            }).catch(console.error);

            // currentAction 리셋 & 메시지 전송
            resetCurrentAction();

            const resetMsg = {
                senderId: senderId,
                seq: mySeq++
            };
            safeSend('/app/resetCurrentAction', resetMsg);
        } else {
            // currentAction 비어있으면 그냥 리셋
            resetCurrentAction();
        }

        shapeCurrentPoint = null;
        prevShapeCurrentPoint = null;
    });

    // select 이벤트
    canvas.on('selection:created', function(e) {
//        initializeCurrentAction({type: selectedTool});
//        const message = {
//            senderId: senderId,
//            seq: mySeq++,
//            type: selectedTool
//        }
//        safeSend('/app/initializeCurrentAction', message);
    });

    canvas.on('object:moving', function (e) { isTransform = true; });

    canvas.on('object:rotating', function (e) { isTransform = true; });

    canvas.on('object:scaling', function (e) { isTransform = true; });

    canvas.on('object:modified', function(e) {
//        if (currentAction && currentAction.targets.length > 0) {
//            pushToUndoStack();
//            const message = {
//                senderId: senderId,
//                seq: mySeq++
//            }
//            safeSend('/app/pushToUndoStack', message);
//
//            saveUndoRedoStack();
//        }
//
//        resetCurrentAction();
//
//        const message = {
//            senderId: senderId,
//            seq: mySeq++
//        }
//        safeSend('/app/resetCurrentAction', message);
//
//        initializeCurrentAction({type: selectedTool});
//
//        const message2 = {
//            senderId: senderId,
//            seq: mySeq++,
//            type: selectedTool
//        }
//        safeSend('/app/initializeCurrentAction', message2);
    });

    canvas.on('selection:cleared', function(e) {
//        resetCurrentAction();
//        const message = {
//            senderId: senderId,
//            seq: mySeq++
//        }
//        safeSend('/app/resetCurrentAction', message);
    });
});

// 마우스 휠 확대 / 축소
canvas.on('mouse:wheel', function(opt) {
  const delta = opt.e.deltaY;
  let zoom = canvas.getZoom();

  zoom *= 0.999 ** delta;

  if (zoom > 10) zoom = 10;
  if (zoom < 0.2) zoom = 0.2;

  canvas.zoomToPoint({ x: opt.e.offsetX, y: opt.e.offsetY }, zoom);
  opt.e.preventDefault();
  opt.e.stopPropagation();

  scheduleRender();
});

// WebSocket 연결
connect();

// 기본 도구 draw
selectTool('draw');