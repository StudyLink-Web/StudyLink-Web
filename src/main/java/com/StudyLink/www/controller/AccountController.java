package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AccountController (계정 관리 API 컨트롤러)
 * 사용자 계정 관련 API 처리
 *
 * 담당 기능:
 * - 계정 정보 조회
 * - 비밀번호 변경
 * - 이메일 변경
 * - 휴대폰 번호 변경
 * - 계정 활성화/비활성화
 * - 계정 삭제 (회원 탈퇴)
 * - 계정 상태 조회
 * - 이메일 중복 확인
 * - 이메일 인증 상태 업데이트
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    /**
     * 계정 정보 조회
     * GET /api/account
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 계정 정보 JSON
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAccountInfo(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> accountInfo = accountService.getAccountInfo(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", accountInfo);

            log.info("✅ 계정 정보 조회: userId={}", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 계정 정보 조회 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 비밀번호 변경
     * POST /api/account/change-password
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            // 입력값 검증
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
                throw new IllegalArgumentException("현재 비밀번호를 입력하세요");
            }
            if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                throw new IllegalArgumentException("새 비밀번호를 입력하세요");
            }
            if (request.getConfirmPassword() == null || request.getConfirmPassword().isEmpty()) {
                throw new IllegalArgumentException("비밀번호 확인을 입력하세요");
            }

            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = accountService.changePassword(
                    user.getUserId(),
                    request.getCurrentPassword(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );

            log.info("✅ 비밀번호 변경 완료: userId={}", user.getUserId());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 비밀번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 비밀번호 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 이메일 변경
     * POST /api/account/change-email
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PostMapping("/change-email")
    public ResponseEntity<Map<String, Object>> changeEmail(
            @RequestBody ChangeEmailRequest request,
            Authentication authentication) {
        try {
            if (request.getNewEmail() == null || request.getNewEmail().isEmpty()) {
                throw new IllegalArgumentException("새 이메일을 입력하세요");
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요");
            }

            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = accountService.changeEmail(
                    user.getUserId(),
                    request.getNewEmail(),
                    request.getPassword()
            );

            log.info("✅ 이메일 변경 완료: userId={}, newEmail={}", user.getUserId(), request.getNewEmail());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 이메일 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 이메일 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 휴대폰 번호 변경
     * POST /api/account/change-phone
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 변경 결과 JSON
     */
    @PostMapping("/change-phone")
    public ResponseEntity<Map<String, Object>> changePhone(
            @RequestBody ChangePhoneRequest request,
            Authentication authentication) {
        try {
            if (request.getNewPhone() == null || request.getNewPhone().isEmpty()) {
                throw new IllegalArgumentException("새 휴대폰 번호를 입력하세요");
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요");
            }

            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = accountService.changePhone(
                    user.getUserId(),
                    request.getNewPhone(),
                    request.getPassword()
            );

            log.info("✅ 휴대폰 번호 변경 완료: userId={}", user.getUserId());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 휴대폰 번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 휴대폰 번호 변경 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 계정 활성화
     * POST /api/account/activate
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 활성화 결과 JSON
     */
    @PostMapping("/activate")
    public ResponseEntity<Map<String, Object>> activateAccount(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = accountService.activateAccount(user.getUserId());

            log.info("✅ 계정 활성화: userId={}", user.getUserId());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 계정 활성화 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 계정 활성화 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 계정 비활성화 (일시 중지)
     * POST /api/account/deactivate
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 비활성화 결과 JSON
     */
    @PostMapping("/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateAccount(
            @RequestBody DeactivateAccountRequest request,
            Authentication authentication) {
        try {
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요");
            }

            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = accountService.deactivateAccount(
                    user.getUserId(),
                    request.getPassword()
            );

            log.info("✅ 계정 비활성화: userId={}", user.getUserId());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 계정 비활성화 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 계정 비활성화 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 계정 삭제 (회원 탈퇴)
     * DELETE /api/account
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 삭제 결과 JSON
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @RequestBody DeleteAccountRequest request,
            Authentication authentication) {
        try {
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요");
            }

            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = accountService.deleteAccount(
                    user.getUserId(),
                    request.getPassword()
            );

            log.info("✅ 계정 삭제 (탈퇴): userId={}, email={}", user.getUserId(), email);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 계정 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 계정 삭제 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 계정 상태 조회
     * GET /api/account/status
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 계정 상태 JSON
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAccountStatus(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> status = accountService.getAccountStatus(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", status);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 계정 상태 조회 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 이메일 중복 확인
     * GET /api/account/check-email
     *
     * @param email 확인할 이메일
     * @return 사용 가능 여부 JSON
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        try {
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("이메일을 입력하세요");
            }

            boolean isAvailable = accountService.isEmailAvailable(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", isAvailable);
            response.put("message", isAvailable ? "사용 가능한 이메일입니다" : "이미 사용 중인 이메일입니다");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 이메일 중복 확인 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 이메일 인증 상태 업데이트
     * POST /api/account/verify-email
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 업데이트 결과 JSON
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(
            @RequestBody VerifyEmailRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> result = accountService.updateEmailVerificationStatus(
                    user.getUserId(),
                    true
            );

            log.info("✅ 이메일 인증 완료: userId={}", user.getUserId());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 이메일 인증 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 비밀번호 재설정 가능 여부 확인
     * GET /api/account/check-password-reset-eligibility
     *
     * @param email 사용자 이메일
     * @return 재설정 가능 여부 JSON
     */
    @GetMapping("/check-password-reset-eligibility")
    public ResponseEntity<Map<String, Object>> checkPasswordResetEligibility(@RequestParam String email) {
        try {
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("이메일을 입력하세요");
            }

            var eligibility = accountService.checkPasswordResetEligibility(email);

            if (eligibility.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "사용자를 찾을 수 없습니다");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", eligibility.get());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 비밀번호 재설정 가능 여부 확인 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========== Request 클래스 ==========

    /**
     * 비밀번호 변경 요청
     */
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }

    /**
     * 이메일 변경 요청
     */
    public static class ChangeEmailRequest {
        private String newEmail;
        private String password;

        public String getNewEmail() { return newEmail; }
        public void setNewEmail(String newEmail) { this.newEmail = newEmail; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * 휴대폰 번호 변경 요청
     */
    public static class ChangePhoneRequest {
        private String newPhone;
        private String password;

        public String getNewPhone() { return newPhone; }
        public void setNewPhone(String newPhone) { this.newPhone = newPhone; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * 계정 비활성화 요청
     */
    public static class DeactivateAccountRequest {
        private String password;

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * 계정 삭제 요청
     */
    public static class DeleteAccountRequest {
        private String password;

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * 이메일 인증 요청
     */
    public static class VerifyEmailRequest {
        private String verificationCode;

        public String getVerificationCode() { return verificationCode; }
        public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    }
}
