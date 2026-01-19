package com.StudyLink.www.service;

import com.StudyLink.www.dto.ChatBotArchiveDTO;
import com.StudyLink.www.entity.ChatBotMessage;
import com.StudyLink.www.entity.ChatBotSession;

import java.util.List;

public interface ChatBotSessionService {

    // 세션 엔티티 -> DTO
    default ChatBotArchiveDTO.SessionResponse convertSessionToDto(ChatBotSession session) {
        return ChatBotArchiveDTO.SessionResponse.builder()
                .sessionId(session.getSessionId())
                .title(session.getTitle())
                .updatedAt(session.getUpdatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    // 메시지 엔티티 -> DTO
    default ChatBotArchiveDTO.MessageResponse convertMessageToDto(ChatBotMessage message) {
        return ChatBotArchiveDTO.MessageResponse.builder()
                .messageId(message.getMessageId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    // 1. 새로운 챗봇 세션 생성
    Long createSession(Long userId);

    // 2. 사용자별 세션 목록 조회
    List<ChatBotArchiveDTO.SessionResponse> getSessionsByUser(Long userId);

    // 3. 특정 세션의 메시지 내역 조회
    List<ChatBotArchiveDTO.MessageResponse> getMessagesBySession(Long sessionId);

    // 4. 메시지 저장 (사용자/봇 공통)
    void saveMessage(Long sessionId, String role, String content);

    // 5. 세션 제목 업데이트
    void updateSessionTitle(Long sessionId, String title);

    // 6. 세션 삭제
    void deleteSession(Long sessionId);
}
