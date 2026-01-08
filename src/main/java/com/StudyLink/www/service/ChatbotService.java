package com.StudyLink.www.service;

import com.StudyLink.www.dto.ChatbotDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    // 파이썬 서버 주소 (Hugging Face 클라우드 주소)
    private final String AI_SERVER_URL = "https://yaimbot23-chatbot-docker.hf.space/chat";

    public ChatbotDTO.Response getChatResponse(String query) {
        ChatbotDTO.Request request = ChatbotDTO.Request.builder()
                .query(query)
                .build();

        try {
            return restTemplate.postForObject(AI_SERVER_URL, request, ChatbotDTO.Response.class);
        } catch (Exception e) {
            return ChatbotDTO.Response.builder()
                    .answer("죄송합니다. AI 서버와 통신하는 중 문제가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }
}
