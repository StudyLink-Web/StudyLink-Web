const roomId = roomDTO.roomId;

if (message != null) {
    alert(message);
}

// 웹소켓 연결 끊김 탐지(일정 주기마다 서버에 ping을 보냄 -> 서버로부터 pong을 응답 받음, pong이 안오면 끊김으로 판단)
let socket;
let lastPong = Date.now();
let heartbeatInterval;
const HEARTBEAT_INTERVAL = 1000; // 1초마다 ping
const TIMEOUT = 3000; // 3초 동안 응답 없으면 끊김으로 판단

function startHeartbeat() {
    lastPong = Date.now();
    heartbeatInterval = setInterval(() => {
        // pong 응답 없으면 강제 끊김 처리
        if (Date.now() - lastPong > TIMEOUT) {
            console.log('웹소켓 끊김 감지');
            stompClient.disconnect(() => {
                stopHeartbeat();
                attemptReconnect();
            });
        } else {
            // 서버에 ping 전송 (STOMP로 메시지 보내기)
            if (stompClient && stompClient.connected) {
                stompClient.send("/app/ping", {}, JSON.stringify({senderId: senderId}));
            }
        }
    }, HEARTBEAT_INTERVAL);
}

function stopHeartbeat() {
    clearInterval(heartbeatInterval);
}

function attemptReconnect() {
    console.log('재연결 시도 중...');
    setTimeout(() => {
        connect(); // 재연결 시도
    }, 3000); // 3초 후 재시도
}

function connect() {
    socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        // 구독
        stompClient.subscribe('/topic/pong', function(message) {
            const msg = JSON.parse(message.body);
            if (msg.senderId !== senderId) return;
            lastPong = Date.now(); // pong 도착 시 갱신
        });

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
            handleMessage(msg, drawLine);
            scheduleRender();
        });

        // 지우기
        stompClient.subscribe('/topic/erase', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            handleMessage(msg, eraseInterpolated);
            scheduleRender();
        });

        // 영역 선택 모드 on/off
        stompClient.subscribe('/topic/selectMode', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            if (msg.type === 'selectModeOn') {
                isSelectLocked = true;
                if (selectedTool === 'select') {
                    selectedTool = 'draw'; // 내가 select 중이면 강제로 draw로
                    alert("다른 사람이 선택 모드를 사용합니다. select 모드 종료");
                }
            } else if (msg.type === 'selectModeOff') {
                isSelectLocked = false;
            }
            updateToolUI(); // 여기서 라디오 버튼 잠금/해제
        });

        // select
        stompClient.subscribe('/topic/select', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            handleMessage(msg, objectUpdate);
            scheduleRender();
        });

        // currentAction 초기화
        stompClient.subscribe('/topic/initializeCurrentAction', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            handleMessage(msg, initializeCurrentAction);
        });

        // currentAction 리셋
        stompClient.subscribe('/topic/resetCurrentAction', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            handleMessage(msg, resetCurrentAction);
        });

        // undoStack에 currentAction push
        stompClient.subscribe('/topic/pushToUndoStack', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            handleMessage(msg, pushToUndoStack);
        });

        // undo, redo
        stompClient.subscribe('/topic/undoRedo', function(message){
            const msg = JSON.parse(message.body);
            if (msg.senderId === senderId) return;
            if (msg.type === 'undo') {
                handleMessage(msg, undo);
            } else {
                handleMessage(msg, redo);
            }
            scheduleRender();
        });





        // connect가 비동기함수이므로 연결이 완료된 후 실행되야하는 함수들은 여기 작성(밖에 작성시 연결되기 전에 실행 될 수 있음)
        loadMessage(roomId).then(result => { // 채팅기록 불러오기
            console.log("💬 로드된 메시지 수:", result.length);
            for(let message of result){
                // 서버에서 메시지 읽음 처리
                readMessageToServer(message.messageId);

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
        readDrawData();
        loadUndoRedoStack();
        scheduleRender();

        startHeartbeat();
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
    if (roomFileDTO.fileType === 1) {
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
        fileInput.value = ''; // 선택 파일 초기화
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
canvas.isDrawingMode = false; // 드로잉 모드

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

// 영역선택 관련
let isSelectLocked = false; // 같은 객체를 양쪽에서 이동시키면 충돌위험. 한쪽이 select모드면 다른쪽은 잠금
let isTransform = false;

// 메시지 번호
// undo, redo와 관련된 메시지는 처리 순서가 중요
// 항상 번호 순서대로 처리하기 위한 변수
let lastSeq = 0; // 마지막 처리된 메시지 seq
const pendingQueue = {}; // seq -> message
let mySeq = 1; // 내가 보낸 메시지 번호

// undo, redo
let undoStack = [];
let redoStack = [];
let currentAction = null; // 현재 드래그 중인 액션

// DB 작업 순차 실행용 큐
let undoRedoQueue = Promise.resolve();

// 툴 선택
document.getElementById('btnradio1').addEventListener('click', () => selectTool('draw'));
document.getElementById('btnradio2').addEventListener('click', () => selectTool('erase'));
document.getElementById('btnradio3').addEventListener('click', (e) => {
    if (isSelectLocked) {
        alert("다른 사람이 선택 모드를 사용 중입니다.");
        e.preventDefault(); // 체크 변경 막기
        return;
    }
    selectTool('select');
});

document.getElementById('btnradio4').addEventListener('click', () => safeUndoRedo('undo'));
document.getElementById('btnradio5').addEventListener('click', () => safeUndoRedo('redo'));

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
    // select 모드 잠금 확인
    if (tool === 'select' && isSelectLocked) {
        alert("다른 사람이 선택 모드를 사용 중입니다.");
        return; // 선택 불가
    }
    selectedTool = tool;
    // ui 갱신
    updateToolUI();

    // 박스 선택 도구 선택 시 canvas.selection 활성화
    if (tool === 'select') {
        canvas.selection = true; // 다중 선택 가능
        canvas.getObjects('line').forEach(line => {
            line.selectable = true; // 선택 가능
            line.evented = true; // 마우스 이벤트 가능
        });
        const message = { senderId: senderId, type: 'selectModeOn' }
        safeSend('/app/selectMode', message);
    } else {
        canvas.selection = false;
        canvas.getObjects('line').forEach(line => {
            line.selectable = false;
            line.evented = false;
        });
        const message = { senderId: senderId, type: 'selectModeOff' }
        safeSend('/app/selectMode', message);
    }
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
    const type = msg.type
    if (type === 'draw' || type === 'erase'){
        currentAction = {
            type: type, // 'draw' | 'erase' | 'move' | 'rotate' | 'scale' ...
            targets: [], // 영향을 받은 객체들
            before: null, // 작업 전 상태
            after: null // 작업 후 상태
        };
    }
    if (type === 'select'){
        currentAction = {
            type: type, // 'draw' | 'erase' | 'move' | 'rotate' | 'scale' ...
            targets: [], // 영향을 받은 객체들
            before: null, // 작업 전 상태
            after: null // 작업 후 상태
        };
        captureBeforeState();
    }
}

// 다중 선택 객체 초기 상태 캡쳐
function captureBeforeState() {
    const objects = canvas.getActiveObjects();
    if (!objects.length) return;
    const activeSelection = canvas.getActiveObject();
    currentAction.targets = objects.map(obj => obj);
    currentAction.before = objects.map(obj => {
        return {
            left: activeSelection.left + (activeSelection.width / 2) + obj.left,
            top: activeSelection.top + (activeSelection.height / 2) + obj.top
        };
    });
    console.log(currentAction.before)
}

// 다중 선택 객체 변화 후 상태 캡쳐
function captureAfterState() {
    if (!currentAction.targets.length) return;
    currentAction.after = currentAction.targets.map(obj => {
        return {
            left: activeSelection.left + (activeSelection.width / 2) + obj.left,
            top: activeSelection.top + (activeSelection.height / 2) + obj.top
        };
    });
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
            drawInterpolatedLine({x1: lastPoint.x, y1: lastPoint.y, x2: currentPointer.x, y2: currentPointer.y});
        }
        if (selectedTool === 'erase') {
            eraseInterpolated({x1: lastPoint.x, y1: lastPoint.y, x2: currentPointer.x, y2: currentPointer.y});

            message = {
                roomId: roomId,
                senderId: senderId,
                seq: mySeq++,
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


// 그리기
function drawLine(msg){
    // 색상, 두께 등 나중에 추가하기
    // 길이가 0이면 skip
    if (msg.x1 === msg.x2 && msg.y1 === msg.y2) return;
    const line = new fabric.Line([msg.x1, msg.y1, msg.x2, msg.y2], {
        uuid: msg.uuid,
        stroke: '#000',
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
}

// 선 보간 함수
function drawInterpolatedLine(msg) {
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
        const newObjectId = crypto.randomUUID();
        drawLine({x1: prevX, y1: prevY, x2: x, y2: y, uuid: newObjectId});
        prevX = x;
        prevY = y;
        message = {
            senderId: senderId,
            seq: mySeq++,
            uuid: newObjectId,
            x1: lastPoint.x,
            y1: lastPoint.y,
            x2: currentPointer.x,
            y2: currentPointer.y
        }
        safeSend("/app/draw", message);
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

// select
function objectUpdate(msg){
    const positions = msg.positions;
    positions.forEach(pos => {
        const obj = canvas.getObjects().find(o => o.uuid === pos.uuid);
        if (!obj) return;
        obj.set({ top: pos.top, left: pos.left });
    });
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

// undo, redo 메시지 전송
function sendUndoRedoMessage(type){
    const message = {
        senderId: senderId,
        seq: mySeq++,
        type: type
    }
    safeSend('/app/undoRedo', message)
}


async function readDrawData(){
    try {
        console.log("캔버스 불러오기 시작");
        const response = await fetch(`/room/readDrawData?roomId=${roomId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const err = await response.text();
            alert('불러오기 실패: ' + err);
            return;
        }

        const drawDataList = await response.json();

        // 기존 캔버스 초기화 (선만 지우고 싶다면 line만 삭제)
        canvas.getObjects('line').forEach(line => canvas.remove(line));

        // 받아온 데이터로 캔버스에 선 그리기
        drawDataList.forEach(data => {
            const line = new fabric.Line([data.x1, data.y1, data.x2, data.y2], {
                uuid: data.uuid,
                stroke: '#000',
                strokeWidth: 2,
                selectable: false,
                evented: false,
                strokeLineCap: 'round',
                strokeLineJoin: 'round'
            });
            canvas.add(line);
        });
    } catch (e) {
        alert('서버 연결 실패: ' + e.message);
    }
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

async function loadUndoRedoStack() {
    try {
        const response = await fetch(`/room/loadUndoRedoStack?roomId=${roomId}`, {
            method: 'GET'
        });

        if (!response.ok) {
            console.error('stack DB 저장 실패');
            return;
        }
        result = await response.json();

        undoStack = result.undoStack;
        redoStack = result.redoStack;

    } catch (e) {
        console.error('서버 연결 실패:', e);
    }
}

// 라디오 클릭 방지 + UI 동기화
document.querySelectorAll('input[name="btnradio"]').forEach(radio => {
    radio.addEventListener('click', (e) => {
        if (isSelectLocked && radio.value === 'select') {
            e.preventDefault();
            alert("다른 사람이 select 모드를 사용 중입니다.");
            const drawRadio = document.querySelector('input[name="btnradio"][value="draw"]');
            if (drawRadio) drawRadio.checked = true;
            selectedTool = 'draw';
        }
    });
});

// updateToolUI에서 기존 체크 상태 보정
function updateToolUI() {
    const radios = document.querySelectorAll('input[name="btnradio"]');
    radios.forEach(radio => {
        const label = radio.parentElement;
        if (isSelectLocked && radio.value === 'select') {
            radio.disabled = true;
            label.classList.add('disabled');
            if (radio.checked) {
                radio.checked = false;
                const drawRadio = document.querySelector('input[name="btnradio"][value="draw"]');
                if (drawRadio) drawRadio.checked = true;
                selectedTool = 'draw';
            }
        } else {
            radio.disabled = false;
            label.classList.remove('disabled');
        }
    });
}


document.addEventListener('DOMContentLoaded', () => {
    // 접근 권한 체크. 권한이 있는 사용자만 캔버스, 메시지 이용가능
    const canUseCanvasAndMessage =
        roomDTO.status !== 'PENDING' && roomDTO.status !== 'COMPLETED' &&
        (senderId === roomDTO.studentId || senderId === roomDTO.mentorId);

    // 캔버스 활성/비활성
    if (canUseCanvasAndMessage) {
        canvas.upperCanvasEl.style.pointerEvents = 'auto';
    } else {
        canvas.upperCanvasEl.style.pointerEvents = 'none';
    }

    // 메시지 입력 영역
    const messageTextarea = document.querySelector('textarea[name="message"]');
    const fileInput = document.getElementById('file');
    const sendFileBtn = document.getElementById('sendFileBtn');

    messageTextarea.disabled = !canUseCanvasAndMessage;
    fileInput.disabled = !canUseCanvasAndMessage;
    sendFileBtn.disabled = !canUseCanvasAndMessage;



    // canvas 이벤트 바인딩
    canvas.on('mouse:down', (opt) => {
        isDrawing = selectedTool === 'draw' || selectedTool === 'erase';
        lastPoint = canvas.getPointer(opt.e);
        currentPointer = lastPoint;

        if (isDrawing) {
            initializeCurrentAction({type: selectedTool});

            const message = {
                senderId: senderId,
                seq: mySeq++,
                type: selectedTool
            }
            safeSend('/app/initializeCurrentAction', message);
        }
    });

    canvas.on('mouse:move', (opt) => {
        if (!isDrawing) return;
        currentPointer = canvas.getPointer(opt.e);
    });

    canvas.on('mouse:up', async () => {
        if (!isDrawing) return;
        isDrawing = false;
        currentPointer = null;

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

    // select 이벤트
    canvas.on('selection:created', function(e) {
        initializeCurrentAction({type: selectedTool});
        const message = {
            senderId: senderId,
            seq: mySeq++,
            type: selectedTool
        }
        safeSend('/app/initializeCurrentAction', message);
    });

    canvas.on('object:moving', function (e) { isTransform = true; });

    canvas.on('object:rotating', function (e) { isTransform = true; });

    canvas.on('object:scaling', function (e) { isTransform = true; });

    canvas.on('object:modified', function(e) {
        if (currentAction && currentAction.targets.length > 0) {
            pushToUndoStack();
            const message = {
                senderId: senderId,
                seq: mySeq++
            }
            safeSend('/app/pushToUndoStack', message);

            saveUndoRedoStack();
        }

        resetCurrentAction();

        const message = {
            senderId: senderId,
            seq: mySeq++
        }
        safeSend('/app/resetCurrentAction', message);

        initializeCurrentAction({type: selectedTool});

        const message2 = {
            senderId: senderId,
            seq: mySeq++,
            type: selectedTool
        }
        safeSend('/app/initializeCurrentAction', message2);
    });

    canvas.on('selection:cleared', function(e) {
        resetCurrentAction();
        const message = {
            senderId: senderId,
            seq: mySeq++
        }
        safeSend('/app/resetCurrentAction', message);
    });
});



// WebSocket 연결
connect();

// 기본 도구 draw
selectTool('draw');