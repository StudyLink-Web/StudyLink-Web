package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.entity.PushToken;
import com.StudyLink.www.repository.PushTokenRepository;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.FCMService;
import com.StudyLink.www.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FCMController {

    private final PushTokenRepository pushTokenRepository;
    private final FCMService fcmService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 📍 토큰 등록 및 갱신 API
    @PostMapping("/token")
    public String registerToken(@RequestBody Map<String, String> payload,
            Authentication authentication) {
        String token = payload.get("token");
        log.info("[FCM] Token registration request received. Token length: {}", token != null ? token.length() : 0);

        String username = getCurrentUserId(authentication)
                .flatMap(userId -> userRepository.findById(userId).map(Users::getUsername))
                .orElse("anonymous");

        log.info("[FCM] Mapping token to username: {}", username);

        boolean isNew = pushTokenRepository.findByToken(token).isEmpty();

        pushTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        existingToken -> {
                            existingToken.setUsername(username);
                            pushTokenRepository.save(existingToken);
                        },
                        () -> {
                            PushToken newToken = PushToken.builder()
                                    .token(token)
                                    .username(username)
                                    .build();
                            pushTokenRepository.save(newToken);
                        });

        return isNew ? "CREATED" : "UPDATED";
    }

    // 📍 즉시 알림 테스트 API (현재 기기 전용)
    @PostMapping("/test")
    public String testPush(@RequestBody Map<String, String> payload,
            Authentication authentication) {
        String token = payload.get("token");
        String title = "StudyLink 테스트";
        String message = "나에게 보낸 테스트 알림입니다! 🚀";

        // Push 발송
        String result = fcmService.sendNotification(token, title, message);

        // DB 저장 (로그인 유저인 경우)
        getCurrentUserId(authentication).ifPresent(userId -> {
            notificationService.createNotification(userId, "TEST", message, null);
        });

        return result;
    }

    // 📍 모든 등록된 기기에 알림 보내기 (전역 공지 테스트용)
    @PostMapping("/test-all")
    public String testPushToAll(@RequestBody(required = false) Map<String, String> payload) {
        String title = "StudyLink 공지";
        String message = (payload != null && payload.get("message") != null)
                ? payload.get("message")
                : "서비스를 이용 중인 모든 기기에 발송된 알림입니다! 📢";

        log.info("📢 [FCMController] test-all 요청 수신: {}", message);

        try {
            // 1. 모든 토큰 사용자에게 푸시 발송
            List<PushToken> tokens = pushTokenRepository.findAll();
            log.info("🚀 총 {}개의 기기에 FCM 푸시 발송 시작", tokens.size());
            tokens.forEach(tokenEntity -> {
                fcmService.sendNotification(tokenEntity.getToken(), title, message);
            });

            // 2. 모든 사용자의 알림 내역에 저장
            List<Users> allUsers = userRepository.findAll();
            log.info("💾 총 {}명의 사용자 DB 알림 내역 저장 시작", allUsers.size());
            allUsers.forEach(user -> {
                notificationService.createNotification(user.getUserId(), "SYSTEM", message, null);
            });

            log.info("✅ [FCMController] test-all 전송 완료");
            return "success";
        } catch (Exception e) {
            log.error("❌ [FCMController] test-all 중 오류 발생", e);
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            return "Fail: " + e.getMessage() + "\nTrace: " + sw.toString();
        }
    }

    // 📍 내 계정으로 로그인된 모든 기기에 알림 보내기
    @PostMapping("/test-mine")
    public String testPushToMine(Authentication authentication) {
        return getCurrentUserId(authentication).map(userId -> {
            Users user = userRepository.findById(userId).get();
            String username = user.getUsername();
            String title = "StudyLink 기기 연동";
            String message = "[" + username + "] 님으로 로그인된 기기에 전달된 알림입니다! 🔗";

            // 1. 내 모든 기기에 푸시 발송
            pushTokenRepository.findAllByUsername(username).forEach(tokenEntity -> {
                fcmService.sendNotification(tokenEntity.getToken(), title, message);
            });

            // 2. 내 알림 내역에 저장
            notificationService.createNotification(userId, "TEST", message, null);

            return username + " 님의 모든 기기에 발송 및 DB 저장 완료";
        }).orElse("Error: 로그인이 필요합니다.");
    }

    // 📍 정식 전체 공지 발송 API
    @PostMapping("/send-notice")
    public String sendNotice(@RequestBody Map<String, String> payload) {
        String title = payload.getOrDefault("title", "StudyLink 공지");
        String message = payload.get("message");

        log.info("📢 전체 공지 발송 요청 수신: title={}, message={}", title, message);

        if (message == null || message.isBlank()) {
            log.warn("❌ 공지 발송 실패: 메시지 내용이 비어있음");
            return "Error: 공지 내용을 입력해 주세요.";
        }

        try {
            // 1. 모든 기기에 푸시 발송
            List<PushToken> tokens = pushTokenRepository.findAll();
            log.info("🚀 총 {}개의 기기에 FCM 푸시 발송 시작", tokens.size());
            tokens.forEach(tokenEntity -> {
                fcmService.sendNotification(tokenEntity.getToken(), title, message);
            });

            // 2. 모든 사용자의 알림 내역에 저장
            List<Users> allUsers = userRepository.findAll();
            log.info("💾 총 {}명의 사용자 DB 알림 내역 저장 시작", allUsers.size());
            allUsers.forEach(user -> {
                try {
                    notificationService.createNotification(user.getUserId(), "SYSTEM", message, null);
                } catch (Exception e) {
                    log.error("❌ 사용자 {} 에게 알림 저장 실패: {}", user.getUserId(), e.getMessage());
                }
            });

            log.info("✅ 전체 공지 발송 절차 완료");
            return "success";
        } catch (Exception e) {
            log.error("❌ 전체 공지 발송 중 치명적 오류 발생", e);
            return "Error: " + e.getMessage();
        }
    }

    // 📍 토큰 삭제 (로그아웃 시 호출)
    @DeleteMapping("/token")
    public String deleteToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token != null) {
            pushTokenRepository.findByToken(token).ifPresent(pushTokenRepository::delete);
            return "deleted";
        }
        return "fail: no token";
    }

    private Optional<Long> getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return Optional.empty();

        String rawId = authentication.getName();

        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
            Map<String, Object> attributes = token.getPrincipal().getAttributes();
            log.info("📍 [FCM] OAuth2 attributes: {}", attributes);
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
        log.info("📍 [FCM] Final identifier for lookup: {}", finalIdentifier);
        return userRepository.findByEmail(finalIdentifier).map(Users::getUserId)
                .or(() -> userRepository.findByUsername(finalIdentifier).map(Users::getUserId));
    }
}
