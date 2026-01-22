package com.StudyLink.www.service;

import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * MyPageService - 마이페이지 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService {

    private final UserRepository userRepository;

    /**
     * 마이페이지 전체 데이터 조회
     */
    public Map<String, Object> getMyPageData(Long userId) {
        Map<String, Object> myPageData = new HashMap<>();

        myPageData.put("user", getUserInfo(userId));
        myPageData.put("settings", getUserSettings(userId));
        myPageData.put("notification", getNotificationInfo(userId));

        return myPageData;
    }

    /**
     * 사용자 정보 조회
     */
    public Map<String, Object> getUserInfo(Long userId) {
        Map<String, Object> userInfo = new HashMap<>();

        var user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userInfo.put("userId", user.getUserId());
            userInfo.put("name", user.getName() != null ? user.getName() : "사용자");
            userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "닉네임");
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone() != null ? user.getPhone() : "");
            userInfo.put("gradeYear", user.getGradeYear() != null ? user.getGradeYear() : "");
            userInfo.put("interests", user.getInterests() != null ? user.getInterests() : "");
            userInfo.put("profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "/img/default-profile.png");
            userInfo.put("isActive", true);
            userInfo.put("emailVerified", true);
            // Role을 String으로 변환하여 추가
            userInfo.put("role", user.getRole() != null ? user.getRole().toString() : "STUDENT");
        }

        return userInfo;
    }

    /**
     * 사용자 설정 조회
     */
    public Map<String, Object> getUserSettings(Long userId) {
        Map<String, Object> settingsData = new HashMap<>();

        settingsData.put("notificationsEnabled", true);
        settingsData.put("emailNotifications", true);
        settingsData.put("pushNotifications", true);
        settingsData.put("smsNotifications", false);
        settingsData.put("themeMode", "LIGHT");
        settingsData.put("language", "KO");
        settingsData.put("profilePublic", true);
        settingsData.put("privacyPolicyAgree", true);
        settingsData.put("termsOfServiceAgree", true);
        settingsData.put("marketingAgree", false);

        return settingsData;
    }

    /**
     * 알림 정보 조회
     */
    public Map<String, Object> getNotificationInfo(Long userId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("unreadCount", getUnreadNotificationCount(userId));
        return notificationData;
    }

    /**
     * 읽지 않은 알림 개수
     */
    public long getUnreadNotificationCount(Long userId) {
        // 실제 구현 시 DB에서 조회
        return 0;
    }

    /**
     * 사용자 역할 조회
     */
    public String getUserRole(Long userId) {
        var user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() != null) {
            // Role Enum을 String으로 변환
            return user.getRole().toString();
        }
        return "STUDENT";  // 기본값도 STUDENT로 변경
    }
}
