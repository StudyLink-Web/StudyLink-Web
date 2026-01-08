package com.StudyLink.www.controller;

import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.dto.MessageDTO;
import com.StudyLink.www.handler.FileHandler;
import com.StudyLink.www.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Controller
public class WebSocketController {
    private final MessageService messageService;
    private final FileHandler fileHandler;

    @MessageMapping("/text")
    @SendTo("/topic/text")
    public MessageDTO sendTextMessage(MessageDTO message, @RequestParam(name = "files", required = false) MultipartFile file) {
        if (file != null) {
            // 핸들러 호출
            FileDTO fileDTO = fileHandler.uploadFile(file);
            message.setFileUuid(fileDTO.getUuid());
        }
        messageService.insert(message);
        return message;
    }
}