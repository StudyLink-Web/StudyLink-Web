package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/signup - 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
        try {
            // 1단계: role이 null인지 먼저 확인
            if (request.getRole() == null || request.getRole().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_ROLE",
                        "message", "역할(role)은 필수입니다."
                ));
            }

            // 2단계: 입력값 검증
            if (!isValidEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_EMAIL",
                        "message", "유효한 이메일을 입력하세요."
                ));
            }

            if (!isValidPassword(request.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_PASSWORD",
                        "message", "비밀번호는 8자 이상이어야 합니다."
                ));
            }

            // 3단계: 역할 검증
            if (!request.getRole().equals("STUDENT") && !request.getRole().equals("MENTOR")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_ROLE",
                        "message", "역할은 STUDENT 또는 MENTOR여야 합니다."
                ));
            }

            // 회원가입 처리
            Users user = authService.signup(
                    request.getEmail(),
                    request.getPassword(),
                    request.getName(),
                    request.getNickname(),
                    request.getRole()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "userId", user.getUserId(),  // ✅ 수정: getUser_id() → getUserId()
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "role", user.getRole(),
                    "message", "회원가입이 완료되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "SIGNUP_ERROR",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("회원가입 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "SERVER_ERROR",
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * POST /api/auth/login - 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Users user = authService.login(request.getEmail(), request.getPassword());

            return ResponseEntity.ok(Map.of(
                    "userId", user.getUserId(),  // ✅ 수정: getUser_id() → getUserId()
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "role", user.getRole(),
                    "redirectUrl", "/dashboard",
                    "message", "로그인 성공"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "LOGIN_FAILED",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("로그인 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "SERVER_ERROR",
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * POST /api/auth/check-email - 이메일 중복 확인
     */
    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean available = authService.isEmailAvailable(email);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "사용 가능한 이메일입니다." : "이미 가입된 이메일입니다."
        ));
    }

    /**
     * POST /api/auth/check-nickname - 닉네임 중복 확인
     */
    @PostMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        boolean available = authService.isNicknameAvailable(nickname);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다."
        ));
    }

    // ========== Validation 메서드 ==========

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    // ========== DTO 클래스 ==========

    public static class SignupRequest {
        private String email;
        private String password;
        private String name;
        private String nickname;
        private String role; // 'STUDENT' 또는 'MENTOR'

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
