package com.StudyLink.www.service;

import com.StudyLink.www.dto.ChatBotArchiveDTO;
import com.StudyLink.www.entity.ChatBotMessage;
import com.StudyLink.www.entity.ChatBotSession;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.ChatBotMessageRepository;
import com.StudyLink.www.repository.ChatBotSessionRepository;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@lombok.extern.slf4j.Slf4j
public class ChatBotSessionServiceImpl implements ChatBotSessionService {

    private final ChatBotSessionRepository sessionRepository;
    private final ChatBotMessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    public Long createSession(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        ChatBotSession session = ChatBotSession.builder()
                .user(user)
                .title("새로운 대화")
                .build();
        
        return sessionRepository.save(session).getSessionId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatBotArchiveDTO.SessionResponse> getSessionsByUser(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return sessionRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream()
                .map(this::convertSessionToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatBotArchiveDTO.MessageResponse> getMessagesBySession(Long sessionId) {
        ChatBotSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));
        
        return messageRepository.findBySessionOrderByCreatedAtAsc(session)
                .stream()
                .map(this::convertMessageToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void saveMessage(Long sessionId, String role, String content) {
        log.info("[ARCHIVE] 메시지 저장 시도 - Session ID: {}, Role: {}", sessionId, role);
        ChatBotSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));
        
        ChatBotMessage message = ChatBotMessage.builder()
                .session(session)
                .role(role.toUpperCase())
                .content(content)
                .build();
        
        messageRepository.save(message);

        // 첫 질문일 경우 제목 자동 생성 (단순 요약)
        String currentTitle = session.getTitle();
        if ("USER".equalsIgnoreCase(role) && (currentTitle == null || currentTitle.trim().equals("새로운 대화"))) {
            String newTitle = content.trim();
            if (newTitle.length() > 20) {
                newTitle = newTitle.substring(0, 17) + "...";
            }
            session.setTitle(newTitle);
            sessionRepository.saveAndFlush(session); // 즉시 갱신
        } else {
            // 세션의 업데이트 시간을 갱신 (@LastModifiedDate 반영)
            sessionRepository.saveAndFlush(session);
        }
    }

    @Override
    public void updateSessionTitle(Long sessionId, String title) {
        ChatBotSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));
        session.setTitle(title);
        sessionRepository.saveAndFlush(session); // 즉시 갱신
    }

    @Override
    public void deleteSession(Long sessionId) {
        ChatBotSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));
        
        // [방어 코드] Cascade가 동작하지 않을 경우를 대비해 수동으로 관계 끊기 및 삭제
        session.getMessages().clear(); // 고아 객체 제거(orphanRemoval=true)를 활용하거나
        // 만약 orphanRemoval이 안 먹히면 messageRepository.deleteAll(session.getMessages()) 필요
        // 여기서는 엔티티 관계를 끊고 저장해서 반영
        sessionRepository.save(session);
        
        sessionRepository.delete(session);
    }

}
