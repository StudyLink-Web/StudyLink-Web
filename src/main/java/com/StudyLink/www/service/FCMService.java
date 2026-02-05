package com.StudyLink.www.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

        public String sendNotification(String token, String title, String body) {
                Notification notification = Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build();

                // 📍 호환성 문제 방지를 위해 기본 설정으로 단순화
                Message message = Message.builder()
                                .setToken(token)
                                .setNotification(notification)
                                .putData("title", title)
                                .putData("body", body)
                                .build();

                try {
                        log.info("🚀 [FCMService] Sending notification to token: {}...",
                                        token.substring(0, Math.min(token.length(), 20)));
                        log.info("🚀 [FCMService] Title: {}, Body: {}", title, body);
                        String response = FirebaseMessaging.getInstance().send(message);
                        log.info("✅ 푸시 알림 전송 성공: " + response);
                        return response;
                } catch (Exception e) {
                        log.error("❌ 푸시 알림 전송 실패: ", e);
                        return "Error: " + e.getMessage();
                }
        }
}
