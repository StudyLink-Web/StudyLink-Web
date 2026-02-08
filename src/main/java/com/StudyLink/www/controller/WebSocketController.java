package com.StudyLink.www.controller;

import com.StudyLink.www.dto.*;
import com.StudyLink.www.entity.DrawData;
import com.StudyLink.www.entity.UndoRedoStack;
import com.StudyLink.www.service.DrawDataService;
import com.StudyLink.www.sync.RoomSyncManager;
import com.StudyLink.www.webSocketMessage.*;
import com.StudyLink.www.handler.RoomFileHandler;
import com.StudyLink.www.service.MessageService;
import com.StudyLink.www.service.RoomFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/room")  // ← /room/* 제거
public class WebSocketController {
    private final MessageService messageService;
    private final RoomFileService roomFileService;
    private final RoomFileHandler roomFileHandler;
    private final SimpMessagingTemplate messagingTemplate;
    private final DrawDataService drawDataService;
    private final RoomSyncManager roomSyncManager;


    // webSocket요청
    // ================== 끊김 탐지 ==================
    @MessageMapping("/ping/{roomId}")
    public void ping(@DestinationVariable long roomId, SenderMessage message) {
        String destination = "/topic/pong/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    // ================== 채팅창 ==================
    @MessageMapping("/sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable long roomId, MessageDTO message) {
        log.info(">>> messageDTO1 {}", message);
        MessageDTO messageDTO = messageService.insert(message);
        log.info(">>> messageDTO2 {}", messageDTO);

        String destination = "/topic/sendMessage/" + roomId;
        messagingTemplate.convertAndSend(destination, messageDTO);
    }

    @MessageMapping("/readMessage/{roomId}")
    public void readMessage(@DestinationVariable long roomId, MessageDTO message) {
        String destination = "/topic/readMessage/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/enterRoom/{roomId}")
    public void enterRoom(@DestinationVariable long roomId, EnterRoomMessage message) {
        // 1️⃣ 방 동기화 시작
        roomSyncManager.startSync(roomId);

        // 2️⃣ 로딩 시작 알림
        messagingTemplate.convertAndSend(
                "/topic/sync/" + roomId,
                new SyncMessage(roomId, "START", null)
        );

        // 3️⃣ DB에서 최신 데이터 조회
        List<DrawDataDTO> drawData = drawDataService.findByRoomId(roomId);
        UndoRedoStack stack = drawDataService.getUndoRedoStack(roomId);

        SyncPayload payload = new SyncPayload(drawData, stack);

        // 4️⃣ 데이터 전송
        messagingTemplate.convertAndSend(
                "/topic/sync/" + roomId,
                new SyncMessage(roomId, "DATA", payload)
        );

        // 5️⃣ 동기화 종료
        roomSyncManager.endSync(roomId);

        messagingTemplate.convertAndSend(
                "/topic/sync/" + roomId,
                new SyncMessage(roomId, "END", null)
        );
    }




    // ================== 캔버스 ==================
    @MessageMapping("/draw/{roomId}")
    public void drawMessage(@DestinationVariable long roomId, DrawMessage message) {
        String destination = "/topic/draw/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/erase/{roomId}")
    public void eraseMessage(@DestinationVariable long roomId, EraseMessage message) {
        String destination = "/topic/erase/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/initializeCurrentAction/{roomId}")
    public void initializeCurrentAction(@DestinationVariable long roomId, InitailizeCurrentActionMessage message) {
        String destination = "/topic/initializeCurrentAction/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/resetCurrentAction/{roomId}")
    public void resetCurrentAction(@DestinationVariable long roomId, SenderMessage message) {
        String destination = "/topic/resetCurrentAction/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/pushToUndoStack/{roomId}")
    public void pushToUndoStack(@DestinationVariable long roomId, SenderMessage message) {
        String destination = "/topic/pushToUndoStack/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/undoRedo/{roomId}")
    public void undoRedoMessage(@DestinationVariable long roomId, UndoRedoMessage message) {
        String destination = "/topic/undoRedo/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/rectangle/{roomId}")
    public void rectangleMessage(@DestinationVariable long roomId, RectangleMessage message) {
        String destination = "/topic/rectangle/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/triangle/{roomId}")
    public void triangleMessage(@DestinationVariable long roomId, TriangleMessage message) {
        String destination = "/topic/triangle/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/line/{roomId}")
    public void lineMessage(@DestinationVariable long roomId, LineMessage message) {
        String destination = "/topic/line/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/circle/{roomId}")
    public void circleMessage(@DestinationVariable long roomId, CircleMessage message) {
        String destination = "/topic/circle/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/drawLines/{roomId}")
    public void drawLinesMessage(@DestinationVariable long roomId, DrawLinesMessage message) {
        String destination = "/topic/drawLines/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/removePreview/{roomId}")
    public void removePreviewMessage(@DestinationVariable long roomId, SenderMessage message) {
        String destination = "/topic/removePreview/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/translation/{roomId}")
    public void translationMessage(@DestinationVariable long roomId, translationMessage message) {
        String destination = "/topic/translation/" + roomId;
        messagingTemplate.convertAndSend(destination, message);
    }



    // 비동기 요청
    @GetMapping("/readMessage/{messageId}")
    @ResponseBody
    public MessageDTO readMessage(@PathVariable long messageId){
        return messageService.readMessage(messageId);
    }

    @GetMapping("/loadMessage/{roomId}")
    @ResponseBody
    public List<MessageDTO> loadMessage(@PathVariable long roomId) {
        log.info("📥 Loading messages for roomId: {}", roomId);
        return messageService.loadMessage(roomId);
    }

    @PostMapping(value = "/saveFile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<RoomFileDTO> saveFile(
            @RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "roomId") long roomId) {

        log.info("📤 File upload request received");
        log.info("   - File: {}", file.getOriginalFilename());
        log.info("   - Size: {} bytes", file.getSize());
        log.info("   - RoomId: {}", roomId);

        try {
            RoomFileDTO roomFileDTO = roomFileHandler.uploadFile(file);
            roomFileDTO.setRoomId(roomId);
            roomFileService.insert(roomFileDTO);

            log.info("✅ File saved successfully: {}", file.getOriginalFilename());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON) // ✅ Content-Type 명시
                    .body(roomFileDTO);

        } catch (Exception e) {
            log.error("❌ File upload failed", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/loadFile/{uuid}")
    @ResponseBody
    public ResponseEntity<Resource> loadFile(@PathVariable String uuid) {
        try {
            RoomFileDTO roomFileDTO = roomFileService.loadFile(uuid);
            log.info(">>> roomFileDTO {}", roomFileDTO);
            File file = roomFileHandler.loadFile(roomFileDTO);
            log.info(">>> file {}", file);
            // Resource로 파일 반환
            Resource resource = new UrlResource(file.toURI());
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream"; // 기본값
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + roomFileDTO.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/loadRoomFileDTO/{uuid}")
    @ResponseBody
    public RoomFileDTO loadRoomFileDTO(@PathVariable String uuid){
        return roomFileService.loadFile(uuid);
    }




    // 몽고디비
    @PostMapping("/saveCanvasAction")
    public ResponseEntity<String> saveCanvasAction(@RequestBody CanvasActionRequestDTO request) {
        try {
            String actionType = request.getActionType();

            if ("draw".equals(actionType)) {
                List<DrawData> drawDataList = new ArrayList<>();

                for (Map<String, Object> line : request.getPayload()) {
                    DrawData drawData = DrawData.builder()
                            .roomId(request.getRoomId())
                            .senderId(request.getSenderId())
                            .uuid((String) line.get("uuid"))
                            .stroke((String) line.get("stroke"))
                            .x1(Double.parseDouble(line.get("x1").toString()))
                            .y1(Double.parseDouble(line.get("y1").toString()))
                            .x2(Double.parseDouble(line.get("x2").toString()))
                            .y2(Double.parseDouble(line.get("y2").toString()))
                            .build();
                    drawDataList.add(drawData);
                }
                drawDataService.draw(drawDataList);

            } else if ("erase".equals(actionType)) {
                List<DrawData> drawDataList = new ArrayList<>();

                for (Map<String, Object> line : request.getPayload()) {
                    DrawData drawData = DrawData.builder()
                            .roomId(request.getRoomId())
                            .uuid((String) line.get("uuid"))
                            .build();
                    drawDataList.add(drawData);
                }
                drawDataService.erase(drawDataList);
            }
            return ResponseEntity.ok("1");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("0");
        }
    }

    @GetMapping("/readDrawData")
    public ResponseEntity<List<DrawDataDTO>> readDrawData(@RequestParam long roomId) {
        List<DrawDataDTO> list = drawDataService.findByRoomId(roomId);
        return ResponseEntity.ok(list);
    }

    // 액션 저장 (draw, erase, select 등)
    @PostMapping("/saveUndoRedoStack")
    public ResponseEntity<String> saveUndoRedoStack(@RequestBody UndoRedoStackDTO undoRedoStackDTO) {
        try {
            drawDataService.pushUndoRedoStack(undoRedoStackDTO);
            return ResponseEntity.ok("1");
        } catch (Exception e) {
            log.info(">>> error {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("0");
        }

    }

    // 방 재연결 시 undo/redo 스택 불러오기
    @GetMapping("/loadUndoRedoStack")
    public ResponseEntity<UndoRedoStackDTO> loadUndoRedoStack(@RequestParam long roomId) {
        try {
            UndoRedoStack stack = drawDataService.getUndoRedoStack(roomId);
            return ResponseEntity.ok(new UndoRedoStackDTO(stack));
        } catch (Exception e) {
            log.info(">>> error {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 초기화
    @PostMapping("resetCanvas")
    public ResponseEntity<Void> resetCanvas(@RequestBody Map<String, Long> request) {
        try {
            drawDataService.removeRoom(request.get("roomId"));
            return ResponseEntity.ok().build(); // 200 OK만 반환, body 없음
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 실패 시 500
        }
    }
}
