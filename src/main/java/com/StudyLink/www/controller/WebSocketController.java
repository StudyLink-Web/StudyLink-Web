package com.StudyLink.www.controller;

import com.StudyLink.www.dto.MessageDTO;
import com.StudyLink.www.dto.RoomFileDTO;
import com.StudyLink.www.handler.RoomFileHandler;
import com.StudyLink.www.service.MessageService;
import com.StudyLink.www.service.RoomFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/room")  // ← /room/* 제거
public class WebSocketController {

    private final MessageService messageService;
    private final RoomFileService roomFileService;
    private final RoomFileHandler roomFileHandler;

    @MessageMapping("/text")
    @SendTo("/topic/text")
    public MessageDTO sendTextMessage(MessageDTO message) {
        MessageDTO messageDTO = messageService.insert(message);
        log.info(">>> messageDTO {}", messageDTO);
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
        log.info(">>> enterRoom");
        return message;
    }

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
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> saveFile(
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
            return new ResponseEntity<>("1", HttpStatus.OK);

        } catch (Exception e) {
            log.error("❌ File upload failed", e);
            return new ResponseEntity<>("0", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
