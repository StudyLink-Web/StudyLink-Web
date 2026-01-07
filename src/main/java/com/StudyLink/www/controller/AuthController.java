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
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            // 1단계: role이 null인지 먼저 확인
            if (request.getRole() == null || request.getRole().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_ROLE",
                        "message", "역할(role)은 필수입니다."
                ));
            }

            // 2단계: 입력값 검증
            if (!isValidUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_USERNAME",
                        "message", "아이디는 영문, 숫자, _, -만 사용 가능합니다. (최소 3자)"
                ));
            }

            if (!isValidPassword(request.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_PASSWORD",
                        "message", "비밀번호는 8자 이상이어야 합니다."
                ));
            }

            // 3단계: 역할 검증 (이제 null 체크가 위에 있어서 안전함)
            if (!request.getRole().equals("STUDENT") && !request.getRole().equals("MENTOR")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_ROLE",
                        "message", "역할은 STUDENT 또는 MENTOR여야 합니다."
                ));
            }

            // 회원가입 처리
            Users user = authService.signup(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getName(),
                    request.getPhone(),
                    request.getGradeYear(),
                    request.getInterests(),
                    request.getRole()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Users user = authService.login(request.getUsername(), request.getPassword());

            return ResponseEntity.ok(Map.of(
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
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
     * POST /api/auth/check-username - 아이디 중복 확인
     */
    @PostMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        boolean available = authService.isUsernameAvailable(username);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다."
        ));
    }

    /**
     * POST /api/auth/check-email - 이메일 중복 확인
     */
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean available = authService.isEmailAvailable(email);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "사용 가능한 이메일입니다." : "이미 가입된 이메일입니다."
        ));
    }

    // ========== Validation 메서드 ==========

    private boolean isValidUsername(String username) {
        if (username == null || username.length() < 3) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_-]+$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    // ========== DTO 클래스 ==========

    public static class SignupRequest {
        private String username;
        private String password;
        private String email;
        private String name;
        private String phone;
        private String gradeYear;
        private String interests;
        private String role;  // 'STUDENT' 또는 'MENTOR'

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getGradeYear() { return gradeYear; }
        public void setGradeYear(String gradeYear) { this.gradeYear = gradeYear; }

        public String getInterests() { return interests; }
        public void setInterests(String interests) { this.interests = interests; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
