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
@RequestMapping("/room/*")
public class WebSocketController {
    private final MessageService messageService;
    private final RoomFileService roomFileService;
    private final RoomFileHandler roomFileHandler;

    @MessageMapping("/text")
    @SendTo("/topic/text")
    public MessageDTO sendTextMessage(MessageDTO message) {
        messageService.insert(message);
        log.info(">>> message {}", message);
        return message;
    }

    @GetMapping("/loadMessage/{roomId}")
    @ResponseBody
    public List<MessageDTO> loadMessage(@PathVariable long roomId){
        return messageService.loadMessage(roomId);
    }


    @PostMapping(value = "/saveFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> saveFile(@RequestParam(name = "file") MultipartFile file, @RequestParam(name = "roomId") long roomId) {
        RoomFileDTO roomFileDTO = roomFileHandler.uploadFile(file);
        roomFileDTO.setRoomId(roomId);
        roomFileService.insert(roomFileDTO);
        return roomFileDTO != null ? new ResponseEntity<String>("1", HttpStatus.OK) : new ResponseEntity<String>("0", HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    @GetMapping(value = "/loadFile", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @ResponseBody
//    public ResponseEntity<String> loadFile(@RequestParam(name = "file") MultipartFile file) {
//        RoomFileDTO roomFileDTO = roomFileHandler.uploadFile(file);
//        return cno > 0 ? new ResponseEntity<String>("1", HttpStatus.OK) : new ResponseEntity<String>("0", HttpStatus.INTERNAL_SERVER_ERROR);
//    }

}