package com.StudyLink.www.controller;

import com.StudyLink.www.entity.PushToken;
import com.StudyLink.www.repository.PushTokenRepository;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.FCMService;
import com.StudyLink.www.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            @AuthenticationPrincipal UserDetails userDetails) {
        String token = payload.get("token");
        String username = (userDetails != null) ? userDetails.getUsername() : "anonymous";

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

        return "success";
    }

    // 📍 즉시 알림 테스트 API (현재 기기 전용)
    @PostMapping("/test")
    public String testPush(@RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        String token = payload.get("token");
        String title = "StudyLink 테스트";
        String message = "나에게 보낸 테스트 알림입니다! 🚀";

        // Push 발송
        String result = fcmService.sendNotification(token, title, message);

        // DB 저장 (로그인 유저인 경우)
        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                notificationService.createNotification(user.getUserId(), "TEST", message, null);
            });
        }

        return result;
    }

    // 📍 모든 등록된 기기에 알림 보내기 (전역 공지 테스트용)
    @PostMapping("/test-all")
    public String testPushToAll(@RequestBody(required = false) Map<String, String> payload) {
        String title = "StudyLink 공지";
        String message = (payload != null && payload.get("message") != null)
                ? payload.get("message")
                : "서비스를 이용 중인 모든 기기에 발송된 알림입니다! 📢";

        // 1. 모든 토큰 사용자에게 푸시 발송
        pushTokenRepository.findAll().forEach(tokenEntity -> {
            fcmService.sendNotification(tokenEntity.getToken(), title, message);
        });

        // 2. 모든 사용자의 알림 내역에 저장
        userRepository.findAll().forEach(user -> {
            notificationService.createNotification(user.getUserId(), "SYSTEM", message, null);
        });

        return "전체 기기 발송 및 DB 저장 완료";
    }

    // 📍 내 계정으로 로그인된 모든 기기에 알림 보내기
    @PostMapping("/test-mine")
    public String testPushToMine(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "Error: 로그인이 필요합니다.";

        String username = userDetails.getUsername();
        String title = "StudyLink 기기 연동";
        String message = "[" + username + "] 님으로 로그인된 기기에 전달된 알림입니다! 🔗";

        // 1. 내 모든 기기에 푸시 발송
        pushTokenRepository.findAllByUsername(username).forEach(tokenEntity -> {
            fcmService.sendNotification(tokenEntity.getToken(), title, message);
        });

        // 2. 내 알림 내역에 저장
        userRepository.findByUsername(username).ifPresent(user -> {
            notificationService.createNotification(user.getUserId(), "TEST", message, null);
        });

        return username + " 님의 모든 기기에 발송 및 DB 저장 완료";
    }

    // 📍 정식 전체 공지 발송 API
    @PostMapping("/send-notice")
    public String sendNotice(@RequestBody Map<String, String> payload) {
        String title = payload.getOrDefault("title", "StudyLink 공지");
        String message = payload.get("message");

        if (message == null || message.isBlank()) {
            return "Error: 공지 내용을 입력해 주세요.";
        }

        // 1. 모든 기기에 푸시 발송
        pushTokenRepository.findAll().forEach(tokenEntity -> {
            fcmService.sendNotification(tokenEntity.getToken(), title, message);
        });

        // 2. 모든 사용자의 알림 내역에 저장
        userRepository.findAll().forEach(user -> {
            notificationService.createNotification(user.getUserId(), "SYSTEM", message, null);
        });

        return "success";
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
}
