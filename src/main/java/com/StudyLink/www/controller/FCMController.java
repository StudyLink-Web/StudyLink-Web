package com.StudyLink.www.controller;

import com.StudyLink.www.entity.PushToken;
import com.StudyLink.www.repository.PushTokenRepository;
import com.StudyLink.www.service.FCMService;
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
                    }
                );

        return "success";
    }

    // 📍 즉시 알림 테스트 API (현재 기기 전용)
    @PostMapping("/test")
    public String testPush(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        return fcmService.sendNotification(token, "StudyLink 테스트", "나에게 보낸 테스트 알림입니다! 🚀");
    }

    // 📍 모든 등록된 기기에 알림 보내기 (전체 서비스 공지 테스트용)
    @PostMapping("/test-all")
    public String testPushToAll() {
        pushTokenRepository.findAll().forEach(tokenEntity -> {
            fcmService.sendNotification(tokenEntity.getToken(), 
                "StudyLink 공지", 
                "서비스를 이용 중인 모든 기기에 발송된 알림입니다! 📢");
        });
        return "전체 기기 발송 요청 완료";
    }

    // 📍 내 계정으로 로그인된 모든 기기에 알림 보내기 (진짜 기기 연동 테스트!)
    @PostMapping("/test-mine")
    public String testPushToMine(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "Error: 로그인이 필요합니다.";
        
        String username = userDetails.getUsername();
        pushTokenRepository.findAllByUsername(username).forEach(tokenEntity -> {
            fcmService.sendNotification(tokenEntity.getToken(), 
                "StudyLink 기기 연동", 
                "[" + username + "] 님으로 로그인된 기기에 전달된 알림입니다! 🔗");
        });
        return username + " 님의 모든 기기에 발송 완료";
    }
}
