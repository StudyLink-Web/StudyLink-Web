package com.StudyLink.www.controller;

import com.StudyLink.www.dto.MessageDTO;
import com.StudyLink.www.dto.RoomFileDTO;
import com.StudyLink.www.handler.RoomFileHandler;
import com.StudyLink.www.service.MessageService;
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

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/room/*")
public class WebSocketController {
    private final MessageService messageService;
    private final RoomFileHandler roomFileHandler;

    @MessageMapping("/text")
    @SendTo("/topic/text")
    public MessageDTO sendTextMessage(MessageDTO message) {
        messageService.insert(message);
        log.info(">>> message {}", message);
        return message;
    }


    @PostMapping(value = "/saveFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> saveFile(@RequestParam(name = "file") MultipartFile file) {
        log.info(">>> roomFile {}", file);
        RoomFileDTO roomFileDTO = roomFileHandler.uploadFile(file);
        log.info(">>> roomFileDTO {}", file);
        return roomFileDTO != null ? new ResponseEntity<String>("1", HttpStatus.OK) : new ResponseEntity<String>("0", HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    @GetMapping(value = "/loadFile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
//    @ResponseBody
//    public ResponseEntity<String> loadFile(@RequestParam(name = "file") MultipartFile file) {
//        RoomFileDTO roomFileDTO = roomFileHandler.uploadFile(file);
//        return cno > 0 ? new ResponseEntity<String>("1", HttpStatus.OK) : new ResponseEntity<String>("0", HttpStatus.INTERNAL_SERVER_ERROR);
//    }

}