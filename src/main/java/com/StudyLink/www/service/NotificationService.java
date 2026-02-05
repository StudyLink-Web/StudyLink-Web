package com.StudyLink.www.service;

import com.StudyLink.www.dto.NotificationDTO;
import com.StudyLink.www.entity.UserNotification;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.PushTokenRepository;
import com.StudyLink.www.repository.UserNotificationRepository;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushTokenRepository pushTokenRepository;
    private final FCMService fcmService;

    /**
     * 특정 사용자의 모든 알림 조회
     */
    public List<NotificationDTO> getNotifications(Long userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalse(userId);
    }

    /**
     * 특정 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findByNotificationIdAndUser_UserId(notificationId, userId)
                .ifPresent(notification -> {
                    notification.setIsRead(true);
                    notification.setReadAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * 새로운 알림 생성 및 저장
     */
    @Transactional
    public void createNotification(Long userId, String type, String message, Long relatedId) {
        userRepository.findById(userId).ifPresent(user -> {
            UserNotification notification = UserNotification.builder()
                    .user(user)
                    .notificationType(type)
                    .message(message)
                    .relatedId(relatedId)
                    .isRead(false)
                    .isEnabled(true)
                    .build();
            notificationRepository.save(notification);
            log.info("Notification created for user {}: {}", userId, message);

            // 🚀 푸시 알림 발송 추가
            String pushTitle = getPushTitle(type);
            var tokens = pushTokenRepository.findAllByUsername(user.getUsername());
            log.info("🚀 User {} (id: {}) has {} push tokens. Sending push...", user.getUsername(), userId,
                    tokens.size());

            tokens.forEach(tokenEntity -> {
                try {
                    fcmService.sendNotification(tokenEntity.getToken(), pushTitle, message);
                } catch (Exception e) {
                    log.error("❌ Failed to send push to token for user {}: {}", user.getUsername(), e.getMessage());
                }
            });
        });
    }

    private String getPushTitle(String type) {
        return switch (type) {
            case "EXCHANGE_COMPLETED" -> "환전 승인 완료";
            case "EXCHANGE_REJECTED" -> "환전 신청 반려";
            case "EXCHANGE_REQUESTED" -> "환전 신청 알림";
            case "ROOM_EXPIRED" -> "방 만료 알림";
            case "ANSWER_RECEIVED" -> "새로운 답변";
            case "COMMENT_RECEIVED" -> "새로운 댓글";
            case "FOLLOW_RECEIVED" -> "새로운 팔로우";
            case "PAYMENT_COMPLETED" -> "결제 완료";
            case "SYSTEM" -> "StudyLink 공지사항";
            default -> "StudyLink 알림";
        };
    }

    private NotificationDTO convertToDTO(UserNotification notification) {
        return NotificationDTO.builder()
                .id(notification.getNotificationId())
                .type(notification.getNotificationType())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .relatedId(notification.getRelatedId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
