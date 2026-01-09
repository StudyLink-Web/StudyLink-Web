package com.StudyLink.www.controller;

import com.StudyLink.www.dto.ChatBotArchiveDTO;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.ChatBotSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chatbot/archive")
@RequiredArgsConstructor
public class ChatBotArchiveController {

    private final ChatBotSessionService sessionService;
    private final UserRepository userRepository;

    // 1. 사용자별 대화 세션 목록 조회
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatBotArchiveDTO.SessionResponse>> getSessions(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        Users user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return ResponseEntity.ok(sessionService.getSessionsByUser(user.getUserId()));
    }

    // 2. 특정 세션의 메시지 내역 조회
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatBotArchiveDTO.MessageResponse>> getMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getMessagesBySession(sessionId));
    }

    // 3. 새 대화 세션 생성
    @PostMapping("/sessions")
    public ResponseEntity<Long> createSession(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        Users user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return ResponseEntity.ok(sessionService.createSession(user.getUserId()));
    }

    // 4. 대화 세션 삭제
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId);
        return ResponseEntity.ok().build();
    }
}
