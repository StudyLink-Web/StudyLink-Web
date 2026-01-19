package com.StudyLink.www.repository;

import com.StudyLink.www.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    /**
     * 특정 사용자의 모든 알림 조회 (최신순)
     */
    List<UserNotification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 사용자의 읽지 않은 알림 조회
     */
    List<UserNotification> findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 사용자의 알림 페이징 조회
     */
    Page<UserNotification> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 사용자의 읽지 않은 알림 개수
     */
    long countByUser_UserIdAndIsReadFalse(Long userId);

    /**
     * 특정 사용자의 특정 유형 알림 조회
     */
    List<UserNotification> findByUser_UserIdAndNotificationType(Long userId, String notificationType);

    /**
     * 특정 사용자의 특정 채널 알림 조회
     */
    List<UserNotification> findByUser_UserIdAndNotificationChannel(Long userId, String channel);

    /**
     * 알림 ID로 조회
     */
    Optional<UserNotification> findByNotificationIdAndUser_UserId(Long notificationId, Long userId);

    /**
     * 특정 사용자의 모든 읽지 않은 알림 표시
     */
    @Query("UPDATE UserNotification un SET un.isRead = true, un.readAt = CURRENT_TIMESTAMP " +
            "WHERE un.user.userId = :userId AND un.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);

    /**
     * 특정 사용자의 특정 유형 알림 삭제 (설정에 따라)
     */
    void deleteByUser_UserIdAndNotificationType(Long userId, String notificationType);
}
