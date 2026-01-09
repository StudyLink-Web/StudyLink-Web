package com.StudyLink.www.controller;

import com.StudyLink.www.dto.MessageDTO;
import com.StudyLink.www.dto.RoomFileDTO;
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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/room")  // ← /room/* 제거
public class WebSocketController {

    private final MessageService messageService;
    private final RoomFileService roomFileService;
    private final RoomFileHandler roomFileHandler;


    // webSocket요청
    @MessageMapping("/sendMessage")
    @SendTo("/topic/sendMessage")
    public MessageDTO sendMessage(MessageDTO message) {
        log.info(">>> messageDTO1 {}", message);
        MessageDTO messageDTO = messageService.insert(message);
        log.info(">>> messageDTO2 {}", messageDTO);
        return messageDTO;
    }

    @MessageMapping("/readMessage")
    @SendTo("/topic/readMessage")
    public MessageDTO readMessage(MessageDTO message) {
        return message;
    }

    @MessageMapping("/enterRoom")
    @SendTo("/topic/enterRoom")
    public MessageDTO enterRoom(MessageDTO message) {
        return message;
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
}
