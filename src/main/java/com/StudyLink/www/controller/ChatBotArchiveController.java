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
    public ResponseEntity<List<ChatBotArchiveDTO.SessionResponse>> getSessions(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        String email = getEmailFromPrincipal(principal);
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
        
        return ResponseEntity.ok(sessionService.getSessionsByUser(user.getUserId()));
    }

    // 2. 특정 세션의 메시지 내역 조회
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatBotArchiveDTO.MessageResponse>> getMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getMessagesBySession(sessionId));
    }

    // 3. 새 대화 세션 생성
    @PostMapping("/sessions")
    public ResponseEntity<Long> createSession(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        String email = getEmailFromPrincipal(principal);
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
        
        return ResponseEntity.ok(sessionService.createSession(user.getUserId()));
    }

    // [유틸리티] Principal에서 이메일 추출
    private String getEmailFromPrincipal(java.security.Principal principal) {
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
            return token.getPrincipal().getAttribute("email");
        }
        return principal.getName(); // 일반 로그인은 username이 곧 이메일이거나 고유식별자
    }

    // 4. 대화 세션 삭제
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId);
        return ResponseEntity.ok().build();
    }
}
