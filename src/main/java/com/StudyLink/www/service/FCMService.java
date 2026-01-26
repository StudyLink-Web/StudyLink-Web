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

        // 📍 iOS(APNS) 전용 설정 강화
        com.google.firebase.messaging.ApnsConfig apnsConfig = com.google.firebase.messaging.ApnsConfig.builder()
                .setAps(com.google.firebase.messaging.Aps.builder()
                        .setSound("default")
                        .setBadge(1)
                        .setContentAvailable(true)
                        .build())
                .putHeader("apns-priority", "10") // 즉시 발송 우선순위
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .setApnsConfig(apnsConfig)
                .setAndroidConfig(com.google.firebase.messaging.AndroidConfig.builder()
                        .setPriority(com.google.firebase.messaging.AndroidConfig.Priority.HIGH)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("✅ 푸시 알림 전송 성공: " + response);
            return response;
        } catch (Exception e) {
            log.error("❌ 푸시 알림 전송 실패: ", e);
            return "Error: " + e.getMessage();
        }
    }
}
