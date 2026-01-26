package com.StudyLink.www.controller;

import com.StudyLink.www.dto.NotificationDTO;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.AuthService;
import com.StudyLink.www.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * 알림 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(Authentication authentication) {
        return getCurrentUserId(authentication)
                .map(userId -> ResponseEntity.ok(notificationService.getNotifications(userId)))
                .orElse(ResponseEntity.status(401).build());
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        return getCurrentUserId(authentication)
                .map(userId -> ResponseEntity.ok(notificationService.getUnreadCount(userId)))
                .orElse(ResponseEntity.status(401).build());
    }

    /**
     * 특정 알림 읽음 처리
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(Authentication authentication, @PathVariable("id") Long notificationId) {
        return getCurrentUserId(authentication)
                .map(userId -> {
                    notificationService.markAsRead(notificationId, userId);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        return getCurrentUserId(authentication)
                .map(userId -> {
                    notificationService.markAllAsRead(userId);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }

    /**
     * 테스트용 알림 생성 API (나중에 제거 가능)
     */
    @PostMapping("/test")
    public ResponseEntity<Void> createTestNotification(Authentication authentication,
            @RequestBody Map<String, String> payload) {
        return getCurrentUserId(authentication)
                .map(userId -> {
                    String message = payload.getOrDefault("message", "테스트 알림입니다!");
                    String type = payload.getOrDefault("type", "SYSTEM");
                    notificationService.createNotification(userId, type, message, null);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }

    private Optional<Long> getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return Optional.empty();

        String rawId = authentication.getName();

        // OAuth2 처리 로직 (DashboardRestController와 동일)
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
            Map<String, Object> attributes = token.getPrincipal().getAttributes();
            if (attributes.containsKey("email")) {
                rawId = (String) attributes.get("email");
            } else if (attributes.get("response") instanceof Map<?, ?> responseMap) {
                if (responseMap.containsKey("email"))
                    rawId = (String) responseMap.get("email");
            } else if (attributes.get("kakao_account") instanceof Map<?, ?> kakaoMap) {
                if (kakaoMap.containsKey("email"))
                    rawId = (String) kakaoMap.get("email");
            }
        }

        final String finalIdentifier = rawId;
        return authService.getUserByEmail(finalIdentifier)
                .map(Users::getUserId)
                .or(() -> userRepository.findByUsername(finalIdentifier).map(Users::getUserId));
    }
}
