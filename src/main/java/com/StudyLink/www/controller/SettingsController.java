package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SettingsController (설정 관리 API 컨트롤러)
 * 사용자 설정 및 알림 관리 API 처리
 *
 * 담당 기능:
 * - 전체 설정 조회
 * - 테마 변경
 * - 언어 변경
 * - 알림 설정 (이메일, 푸시, SMS)
 * - 개인정보 처리방침 동의
 * - 서비스 이용약관 동의
 * - 마케팅 정보 수신 동의
 * - 프로필 공개 여부 설정
 * - 알림 유형별 설정
 * - 설정 초기화
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Slf4j
public class SettingsController {

    private final SettingsService settingsService;
    private final UserRepository userRepository;

    /**
     * 전체 설정 조회
     * GET /api/settings
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 전체 설정 JSON
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSettings(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> allSettings = settingsService.getAllSettings(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", allSettings);

            log.info("✅ 전체 설정 조회: userId={}", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 전체 설정 조회 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 테마 변경
     * PUT /api/settings/theme
     *
     * @param request 요청 데이터 (themeMode: LIGHT 또는 DARK)
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/theme")
    public ResponseEntity<Map<String, Object>> changeTheme(
            @RequestBody ChangeThemeRequest request,
            Authentication authentication) {
        try {
            if (request.getThemeMode() == null || request.getThemeMode().isEmpty()) {
                throw new IllegalArgumentException("테마를 선택하세요");
            }

            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.changeTheme(
                    user.getUserId(),
                    request.getThemeMode()
            );

            log.info("✅ 테마 변경: userId={}, theme={}", user.getUserId(), request.getThemeMode());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 테마 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 테마 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 언어 변경
     * PUT /api/settings/language
     *
     * @param request 요청 데이터 (language: KO, EN, JA)
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/language")
    public ResponseEntity<Map<String, Object>> changeLanguage(
            @RequestBody ChangeLanguageRequest request,
            Authentication authentication) {
        try {
            if (request.getLanguage() == null || request.getLanguage().isEmpty()) {
                throw new IllegalArgumentException("언어를 선택하세요");
            }

            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.changeLanguage(
                    user.getUserId(),
                    request.getLanguage()
            );

            log.info("✅ 언어 변경: userId={}, language={}", user.getUserId(), request.getLanguage());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 언어 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 언어 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 전체 알림 활성화/비활성화
     * PUT /api/settings/notifications
     *
     * @param request 요청 데이터 (enabled: true/false)
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/notifications")
    public ResponseEntity<Map<String, Object>> setNotificationsEnabled(
            @RequestBody SetNotificationsRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.setNotificationsEnabled(
                    user.getUserId(),
                    request.isEnabled()
            );

            log.info("✅ 전체 알림 설정 변경: userId={}, enabled={}", user.getUserId(), request.isEnabled());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 전체 알림 설정 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 알림 채널 설정 (이메일, 푸시, SMS)
     * PUT /api/settings/notification-channels
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/notification-channels")
    public ResponseEntity<Map<String, Object>> updateNotificationChannels(
            @RequestBody UpdateNotificationChannelsRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.updateNotificationChannels(
                    user.getUserId(),
                    request.isEmailEnabled(),
                    request.isPushEnabled(),
                    request.isSmsEnabled()
            );

            log.info("✅ 알림 채널 설정 변경: userId={}", user.getUserId());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 알림 채널 설정 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 이메일 알림 설정
     * PUT /api/settings/email-notifications
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/email-notifications")
    public ResponseEntity<Map<String, Object>> setEmailNotifications(
            @RequestBody SetEmailNotificationsRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.setEmailNotifications(
                    user.getUserId(),
                    request.isEnabled()
            );

            log.info("✅ 이메일 알림 설정 변경: userId={}, enabled={}", user.getUserId(), request.isEnabled());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 이메일 알림 설정 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 푸시 알림 설정
     * PUT /api/settings/push-notifications
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/push-notifications")
    public ResponseEntity<Map<String, Object>> setPushNotifications(
            @RequestBody SetPushNotificationsRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.setPushNotifications(
                    user.getUserId(),
                    request.isEnabled()
            );

            log.info("✅ 푸시 알림 설정 변경: userId={}, enabled={}", user.getUserId(), request.isEnabled());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 푸시 알림 설정 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * SMS 알림 설정
     * PUT /api/settings/sms-notifications
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/sms-notifications")
    public ResponseEntity<Map<String, Object>> setSmsNotifications(
            @RequestBody SetSmsNotificationsRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.setSmsNotifications(
                    user.getUserId(),
                    request.isEnabled()
            );

            log.info("✅ SMS 알림 설정 변경: userId={}, enabled={}", user.getUserId(), request.isEnabled());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ SMS 알림 설정 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 개인정보 처리방침 동의
     * PUT /api/settings/privacy-policy
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/privacy-policy")
    public ResponseEntity<Map<String, Object>> setPrivacyPolicyAgree(
            @RequestBody SetPrivacyPolicyRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.setPrivacyPolicyAgree(
                    user.getUserId(),
                    request.isAgree()
            );

            log.info("✅ 개인정보 처리방침 동의 변경: userId={}, agree={}", user.getUserId(), request.isAgree());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 개인정보 처리방침 동의 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 서비스 이용약관 동의
     * PUT /api/settings/terms-of-service
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/terms-of-service")
    public ResponseEntity<Map<String, Object>> setTermsOfServiceAgree(
            @RequestBody SetTermsOfServiceRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.setTermsOfServiceAgree(
                    user.getUserId(),
                    request.isAgree()
            );

            log.info("✅ 서비스 이용약관 동의 변경: userId={}, agree={}", user.getUserId(), request.isAgree());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 서비스 이용약관 동의 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 마케팅 정보 수신 동의
     * PUT /api/settings/marketing
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/marketing")
    public ResponseEntity<Map<String, Object>> setMarketingAgree(
            @RequestBody SetMarketingAgreeRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.setMarketingAgree(
                    user.getUserId(),
                    request.isAgree()
            );

            log.info("✅ 마케팅 정보 수신 동의 변경: userId={}, agree={}", user.getUserId(), request.isAgree());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 마케팅 정보 수신 동의 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 프로필 공개 여부 설정
     * PUT /api/settings/profile-visibility
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/profile-visibility")
    public ResponseEntity<Map<String, Object>> setProfilePublic(
            @RequestBody SetProfileVisibilityRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.setProfilePublic(
                    user.getUserId(),
                    request.isPublic()
            );

            log.info("✅ 프로필 공개 설정 변경: userId={}, public={}", user.getUserId(), request.isPublic());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 프로필 공개 설정 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 알림 유형별 설정
     * PUT /api/settings/notification-type
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PutMapping("/notification-type")
    public ResponseEntity<Map<String, Object>> setNotificationTypeEnabled(
            @RequestBody SetNotificationTypeRequest request,
            Authentication authentication) {
        try {
            if (request.getNotificationType() == null || request.getNotificationType().isEmpty()) {
                throw new IllegalArgumentException("알림 유형을 입력하세요");
            }

            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.setNotificationTypeEnabled(
                    user.getUserId(),
                    request.getNotificationType(),
                    request.isEnabled()
            );

            log.info("✅ 알림 유형 설정 변경: userId={}, type={}, enabled={}",
                    user.getUserId(), request.getNotificationType(), request.isEnabled());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 알림 유형 설정 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 알림 유형 설정 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 알림 설정 조회
     * GET /api/settings/notifications
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 알림 설정 목록 JSON
     */
    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> getNotificationSettings(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            List<Map<String, Object>> notificationSettings = settingsService.getNotificationSettings(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", notificationSettings);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 알림 설정 조회 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 필수 동의 여부 확인
     * GET /api/settings/check-required-consents
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 필수 동의 완료 여부 JSON
     */
    @GetMapping("/check-required-consents")
    public ResponseEntity<Map<String, Object>> checkRequiredConsents(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            boolean hasRequiredConsents = settingsService.checkRequiredConsents(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasRequiredConsents", hasRequiredConsents);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 필수 동의 여부 확인 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 설정 초기화
     * POST /api/settings/reset
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 초기화 결과 JSON
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetSettingsToDefault(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = settingsService.resetSettingsToDefault(user.getUserId());

            log.info("✅ 설정 초기화: userId={}", user.getUserId());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 설정 초기화 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========== Request 클래스 ==========

    /**
     * 테마 변경 요청
     */
    public static class ChangeThemeRequest {
        private String themeMode;

        public String getThemeMode() { return themeMode; }
        public void setThemeMode(String themeMode) { this.themeMode = themeMode; }
    }

    /**
     * 언어 변경 요청
     */
    public static class ChangeLanguageRequest {
        private String language;

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    /**
     * 전체 알림 설정 요청
     */
    public static class SetNotificationsRequest {
        private boolean enabled;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * 알림 채널 설정 요청
     */
    public static class UpdateNotificationChannelsRequest {
        private boolean emailEnabled;
        private boolean pushEnabled;
        private boolean smsEnabled;

        public boolean isEmailEnabled() { return emailEnabled; }
        public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }

        public boolean isPushEnabled() { return pushEnabled; }
        public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }

        public boolean isSmsEnabled() { return smsEnabled; }
        public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }
    }

    /**
     * 이메일 알림 설정 요청
     */
    public static class SetEmailNotificationsRequest {
        private boolean enabled;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * 푸시 알림 설정 요청
     */
    public static class SetPushNotificationsRequest {
        private boolean enabled;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * SMS 알림 설정 요청
     */
    public static class SetSmsNotificationsRequest {
        private boolean enabled;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * 개인정보 처리방침 동의 요청
     */
    public static class SetPrivacyPolicyRequest {
        private boolean agree;

        public boolean isAgree() { return agree; }
        public void setAgree(boolean agree) { this.agree = agree; }
    }

    /**
     * 서비스 이용약관 동의 요청
     */
    public static class SetTermsOfServiceRequest {
        private boolean agree;

        public boolean isAgree() { return agree; }
        public void setAgree(boolean agree) { this.agree = agree; }
    }

    /**
     * 마케팅 정보 수신 동의 요청
     */
    public static class SetMarketingAgreeRequest {
        private boolean agree;

        public boolean isAgree() { return agree; }
        public void setAgree(boolean agree) { this.agree = agree; }
    }

    /**
     * 프로필 공개 여부 설정 요청
     */
    public static class SetProfileVisibilityRequest {
        private boolean isPublic;

        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    }

    /**
     * 알림 유형별 설정 요청
     */
    public static class SetNotificationTypeRequest {
        private String notificationType;
        private boolean enabled;

        public String getNotificationType() { return notificationType; }
        public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
