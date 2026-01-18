package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.entity.UserSettings;
import com.StudyLink.www.entity.UserNotification;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.repository.UserSettingsRepository;
import com.StudyLink.www.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SettingsService (설정 관리 서비스)
 * 사용자 설정 및 알림 관리
 *
 * 담당 기능:
 * - 테마 설정 (라이트/다크 모드)
 * - 언어 설정
 * - 알림 설정 (이메일, 푸시, SMS)
 * - 개인정보 처리방침 동의
 * - 서비스 이용약관 동의
 * - 마케팅 정보 수신 동의
 * - 프로필 공개 여부 설정
 * - 알림 유형별 활성화/비활성화
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UserNotificationRepository userNotificationRepository;

    /**
     * 사용자 설정 전체 조회
     *
     * @param userId 사용자 ID
     * @return 설정 정보 맵
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllSettings(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        Map<String, Object> allSettings = new HashMap<>();

        // 테마 및 언어 설정
        allSettings.put("themeMode", settings.getThemeMode());
        allSettings.put("language", settings.getLanguage());

        // 알림 설정
        Map<String, Object> notificationSettings = new HashMap<>();
        notificationSettings.put("notificationsEnabled", settings.getNotificationsEnabled());
        notificationSettings.put("emailNotifications", settings.getEmailNotifications());
        notificationSettings.put("pushNotifications", settings.getPushNotifications());
        notificationSettings.put("smsNotifications", settings.getSmsNotifications());
        allSettings.put("notifications", notificationSettings);

        // 동의 설정
        Map<String, Object> consentSettings = new HashMap<>();
        consentSettings.put("privacyPolicyAgree", settings.getPrivacyPolicyAgree());
        consentSettings.put("termsOfServiceAgree", settings.getTermsOfServiceAgree());
        consentSettings.put("marketingAgree", settings.getMarketingAgree());
        allSettings.put("consents", consentSettings);

        // 기타 설정
        allSettings.put("profilePublic", settings.getProfilePublic());
        allSettings.put("lastLoginAt", settings.getLastLoginAt());

        log.info("✅ 설정 전체 조회: userId={}", userId);
        return allSettings;
    }

    /**
     * 테마 변경
     *
     * @param userId 사용자 ID
     * @param themeMode 테마 (LIGHT 또는 DARK)
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> changeTheme(Long userId, String themeMode) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        // 테마 검증
        if (!themeMode.equals("LIGHT") && !themeMode.equals("DARK")) {
            throw new IllegalArgumentException("테마는 LIGHT 또는 DARK여야 합니다");
        }

        settings.setThemeMode(themeMode);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 테마 변경: userId={}, theme={}", userId, themeMode);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "테마가 변경되었습니다");
        response.put("themeMode", themeMode);
        return response;
    }

    /**
     * 언어 변경
     *
     * @param userId 사용자 ID
     * @param language 언어 (KO, EN, JA)
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> changeLanguage(Long userId, String language) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        // 언어 검증
        if (!language.matches("^(KO|EN|JA)$")) {
            throw new IllegalArgumentException("지원하는 언어: KO(한국어), EN(영어), JA(일본어)");
        }

        settings.setLanguage(language);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 언어 변경: userId={}, language={}", userId, language);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "언어가 변경되었습니다");
        response.put("language", language);
        return response;
    }

    /**
     * 전체 알림 활성화/비활성화
     *
     * @param userId 사용자 ID
     * @param enabled 활성화 여부
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> setNotificationsEnabled(Long userId, boolean enabled) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setNotificationsEnabled(enabled);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 전체 알림 설정 변경: userId={}, enabled={}", userId, enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", enabled ? "알림이 활성화되었습니다" : "알림이 비활성화되었습니다");
        response.put("notificationsEnabled", enabled);
        return response;
    }

    /**
     * 이메일 알림 활성화/비활성화
     *
     * @param userId 사용자 ID
     * @param enabled 활성화 여부
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> setEmailNotifications(Long userId, boolean enabled) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setEmailNotifications(enabled);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 이메일 알림 설정 변경: userId={}, enabled={}", userId, enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", enabled ? "이메일 알림이 활성화되었습니다" : "이메일 알림이 비활성화되었습니다");
        response.put("emailNotifications", enabled);
        return response;
    }

    /**
     * 푸시 알림 활성화/비활성화
     *
     * @param userId 사용자 ID
     * @param enabled 활성화 여부
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> setPushNotifications(Long userId, boolean enabled) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setPushNotifications(enabled);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 푸시 알림 설정 변경: userId={}, enabled={}", userId, enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", enabled ? "푸시 알림이 활성화되었습니다" : "푸시 알림이 비활성화되었습니다");
        response.put("pushNotifications", enabled);
        return response;
    }

    /**
     * SMS 알림 활성화/비활성화
     *
     * @param userId 사용자 ID
     * @param enabled 활성화 여부
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> setSmsNotifications(Long userId, boolean enabled) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setSmsNotifications(enabled);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ SMS 알림 설정 변경: userId={}, enabled={}", userId, enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", enabled ? "SMS 알림이 활성화되었습니다" : "SMS 알림이 비활성화되었습니다");
        response.put("smsNotifications", enabled);
        return response;
    }

    /**
     * 알림 채널별 설정 변경
     * 여러 채널을 한 번에 설정
     *
     * @param userId 사용자 ID
     * @param emailEnabled 이메일 알림 활성화
     * @param pushEnabled 푸시 알림 활성화
     * @param smsEnabled SMS 알림 활성화
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> updateNotificationChannels(
            Long userId,
            boolean emailEnabled,
            boolean pushEnabled,
            boolean smsEnabled) {

        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setEmailNotifications(emailEnabled);
        settings.setPushNotifications(pushEnabled);
        settings.setSmsNotifications(smsEnabled);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 알림 채널 설정 변경: userId={}, email={}, push={}, sms={}",
                userId, emailEnabled, pushEnabled, smsEnabled);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "알림 채널이 변경되었습니다");
        response.put("emailNotifications", emailEnabled);
        response.put("pushNotifications", pushEnabled);
        response.put("smsNotifications", smsEnabled);
        return response;
    }

    /**
     * 개인정보 처리방침 동의
     *
     * @param userId 사용자 ID
     * @param agree 동의 여부
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> setPrivacyPolicyAgree(Long userId, boolean agree) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setPrivacyPolicyAgree(agree);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 개인정보 처리방침 동의 변경: userId={}, agree={}", userId, agree);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", agree ? "동의되었습니다" : "동의 해제되었습니다");
        response.put("privacyPolicyAgree", agree);
        return response;
    }

    /**
     * 서비스 이용약관 동의
     *
     * @param userId 사용자 ID
     * @param agree 동의 여부
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> setTermsOfServiceAgree(Long userId, boolean agree) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setTermsOfServiceAgree(agree);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 서비스 이용약관 동의 변경: userId={}, agree={}", userId, agree);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", agree ? "동의되었습니다" : "동의 해제되었습니다");
        response.put("termsOfServiceAgree", agree);
        return response;
    }

    /**
     * 마케팅 정보 수신 동의
     *
     * @param userId 사용자 ID
     * @param agree 동의 여부
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> setMarketingAgree(Long userId, boolean agree) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setMarketingAgree(agree);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 마케팅 정보 수신 동의 변경: userId={}, agree={}", userId, agree);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", agree ? "마케팅 정보를 수신하시겠습니다" : "마케팅 정보 수신을 거부하셨습니다");
        response.put("marketingAgree", agree);
        return response;
    }

    /**
     * 프로필 공개 여부 설정
     *
     * @param userId 사용자 ID
     * @param isPublic 공개 여부
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> setProfilePublic(Long userId, boolean isPublic) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setProfilePublic(isPublic);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);

        log.info("✅ 프로필 공개 설정 변경: userId={}, public={}", userId, isPublic);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", isPublic ? "프로필이 공개되었습니다" : "프로필이 비공개되었습니다");
        response.put("profilePublic", isPublic);
        return response;
    }

    /**
     * 특정 알림 유형 활성화/비활성화
     *
     * @param userId 사용자 ID
     * @param notificationType 알림 유형
     * @param enabled 활성화 여부
     * @return 변경 결과 맵
     */
    @Transactional
    public Map<String, Object> setNotificationTypeEnabled(
            Long userId,
            String notificationType,
            boolean enabled) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 기존 설정 조회
        List<UserNotification> notifications = userNotificationRepository
                .findByUser_UserIdAndNotificationType(userId, notificationType);

        if (notifications.isEmpty()) {
            // 없으면 새로 생성
            UserNotification newNotification = UserNotification.builder()
                    .user(user)
                    .notificationType(notificationType)
                    .isEnabled(enabled)
                    .build();
            userNotificationRepository.save(newNotification);
        } else {
            // 있으면 업데이트
            for (UserNotification notification : notifications) {
                notification.setIsEnabled(enabled);
                userNotificationRepository.save(notification);
            }
        }

        log.info("✅ 알림 유형 설정 변경: userId={}, type={}, enabled={}", userId, notificationType, enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "알림 설정이 변경되었습니다");
        response.put("notificationType", notificationType);
        response.put("enabled", enabled);
        return response;
    }

    /**
     * 사용자 알림 설정 조회
     *
     * @param userId 사용자 ID
     * @return 알림 설정 목록
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getNotificationSettings(Long userId) {
        return userNotificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> {
                    Map<String, Object> settingMap = new HashMap<>();
                    settingMap.put("notificationId", notification.getNotificationId());
                    settingMap.put("notificationType", notification.getNotificationType());
                    settingMap.put("isEnabled", notification.getIsEnabled());
                    settingMap.put("notificationChannel", notification.getNotificationChannel());
                    return settingMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * 기본 설정 생성
     *
     * @param userId 사용자 ID
     * @return 생성된 UserSettings
     */
    @Transactional
    public UserSettings createDefaultSettings(Long userId) {
        // 이미 존재하면 반환
        if (userSettingsRepository.existsByUser_UserId(userId)) {
            return userSettingsRepository.findByUser_UserId(userId).get();
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        UserSettings settings = UserSettings.builder()
                .user(user)
                .themeMode("LIGHT")
                .language("KO")
                .notificationsEnabled(true)
                .emailNotifications(true)
                .pushNotifications(true)
                .smsNotifications(false)
                .marketingAgree(false)
                .privacyPolicyAgree(true)
                .termsOfServiceAgree(true)
                .profilePublic(true)
                .build();

        UserSettings savedSettings = userSettingsRepository.save(settings);
        log.info("✅ 기본 설정 생성: userId={}", userId);
        return savedSettings;
    }

    /**
     * 모든 동의 필수 항목 확인
     *
     * @param userId 사용자 ID
     * @return 필수 동의 완료 여부
     */
    @Transactional(readOnly = true)
    public boolean checkRequiredConsents(Long userId) {
        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return settings.getPrivacyPolicyAgree() && settings.getTermsOfServiceAgree();
    }

    /**
     * 설정 초기화 (기본값으로 복구)
     *
     * @param userId 사용자 ID
     * @return 초기화 결과 맵
     */
    @Transactional
    public Map<String, Object> resetSettingsToDefault(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        UserSettings settings = userSettingsRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        // 기본값으로 복구
        settings.setThemeMode("LIGHT");
        settings.setLanguage("KO");
        settings.setNotificationsEnabled(true);
        settings.setEmailNotifications(true);
        settings.setPushNotifications(true);
        settings.setSmsNotifications(false);
        settings.setMarketingAgree(false);
        settings.setProfilePublic(true);
        settings.setUpdatedAt(LocalDateTime.now());

        userSettingsRepository.save(settings);

        log.info("✅ 설정 초기화: userId={}", userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "설정이 기본값으로 복구되었습니다");
        return response;
    }
}
