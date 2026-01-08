package com.StudyLink.www.controller;

import com.StudyLink.www.dto.ChatbotDTO;
import com.StudyLink.www.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @GetMapping("/room/chatbot")
    public String chatbot() {
        return "chatbot"; // templates/chatbot.html 반환
    }

    @PostMapping("/room/chatbot/send")
    @ResponseBody
    public ChatbotDTO.Response send(@RequestBody ChatbotDTO.Request request) {
        return chatbotService.getChatResponse(request.getQuery());
    }
}
