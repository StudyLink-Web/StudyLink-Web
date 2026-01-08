package com.StudyLink.www.controller;

import com.StudyLink.www.dto.MessageDTO;
import com.StudyLink.www.handler.RoomFileHandler;
import com.StudyLink.www.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Slf4j
@Controller
public class WebSocketController {
    private final MessageService messageService;
    private final RoomFileHandler fileHandler;

    @MessageMapping("/text")
    @SendTo("/topic/text")
    public MessageDTO sendTextMessage(MessageDTO message) {
        messageService.insert(message);
        log.info(">>> message {}", message);
        return message;
    }
}