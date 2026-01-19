package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "isLogin", false,
                        "isMentor", false
                ));
            }

            String identifier = null;

            if (auth instanceof OAuth2AuthenticationToken token) {
                Object emailObj = token.getPrincipal().getAttribute("email");
                if (emailObj != null) identifier = String.valueOf(emailObj);
            }

            if (identifier == null || identifier.isBlank()) {
                identifier = auth.getName();
            }

            if (identifier == null || identifier.isBlank() || "anonymousUser".equalsIgnoreCase(identifier)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "isLogin", false,
                        "isMentor", false
                ));
            }

            Users user = authService.findByIdentifier(identifier);

            String role = (user.getRole() == null) ? "" : user.getRole().trim().toUpperCase();
            boolean isMentor = role.equals("MENTOR") || role.equals("ROLE_MENTOR");

            return ResponseEntity.ok(Map.of(
                    "isLogin", true,
                    "isMentor", isMentor,
                    "userId", user.getUserId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "nickname", user.getNickname(),
                    "role", user.getRole()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "isLogin", false,
                    "isMentor", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ /api/auth/me 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "isLogin", false,
                    "isMentor", false,
                    "error", "SERVER_ERROR",
                    "message", "사용자 정보 조회 중 오류가 발생했습니다."
            ));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
        log.info("📝 회원가입 요청: {}", request.getEmail());

        try {
            if (request.getRole() == null || request.getRole().isEmpty()) {
                log.warn("❌ 역할(role) 선택 안 됨");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_ROLE",
                        "message", "역할(role)은 필수입니다."
                ));
            }

            if (!isValidEmail(request.getEmail())) {
                log.warn("❌ 유효하지 않은 이메일: {}", request.getEmail());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_EMAIL",
                        "message", "유효한 이메일을 입력하세요."
                ));
            }

            if (!isValidPassword(request.getPassword())) {
                log.warn("❌ 비밀번호 길이 부족");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_PASSWORD",
                        "message", "비밀번호는 8자 이상이어야 합니다."
                ));
            }

            if (!request.getRole().equals("STUDENT") && !request.getRole().equals("MENTOR")) {
                log.warn("❌ 유효하지 않은 역할: {}", request.getRole());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_ROLE",
                        "message", "역할은 STUDENT 또는 MENTOR여야 합니다."
                ));
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                log.warn("❌ 이름 입력 안 됨");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_NAME",
                        "message", "이름을 입력해주세요."
                ));
            }

            if (request.getNickname() == null || request.getNickname().length() < 2 || request.getNickname().length() > 20) {
                log.warn("❌ 닉네임 길이 오류");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_NICKNAME",
                        "message", "닉네임은 2자 이상 20자 이하여야 합니다."
                ));
            }

            Users user = authService.signup(
                    request.getEmail(),
                    request.getPassword(),
                    request.getName(),
                    request.getNickname(),
                    request.getRole()
            );

            log.info("✅ 회원가입 성공: {} (역할: {})", user.getEmail(), user.getRole());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "userId", user.getUserId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "nickname", user.getNickname(),
                    "role", user.getRole(),
                    "message", "회원가입이 완료되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.error("❌ 회원가입 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "SIGNUP_ERROR",
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("❌ 회원가입 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "SERVER_ERROR",
                    "message", "회원가입 중 오류가 발생했습니다."
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        log.info("🔐 로그인 요청: {}", request.getEmail());

        try {
            Users user = authService.login(request.getEmail(), request.getPassword());

            log.info("✅ 로그인 성공: {} (역할: {})", user.getEmail(), user.getRole());

            return ResponseEntity.ok(Map.of(
                    "userId", user.getUserId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "nickname", user.getNickname(),
                    "role", user.getRole(),
                    "redirectUrl", "/dashboard",
                    "message", "로그인 성공"
            ));

        } catch (IllegalArgumentException e) {
            log.warn("❌ 로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "LOGIN_FAILED",
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("❌ 로그인 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "SERVER_ERROR",
                    "message", "로그인 중 오류가 발생했습니다."
            ));
        }
    }

    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("🔍 이메일 중복 확인: {}", email);

        if (email == null || !isValidEmail(email)) {
            log.warn("❌ 유효하지 않은 이메일: {}", email);
            return ResponseEntity.badRequest().body(Map.of(
                    "available", false,
                    "message", "유효한 이메일을 입력하세요."
            ));
        }

        boolean available = authService.isEmailAvailable(email);

        log.info("✅ 이메일 확인 완료: {} (가능: {})", email, available);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "✅ 사용 가능한 이메일입니다." : "❌ 이미 가입된 이메일입니다."
        ));
    }

    @PostMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        log.info("🔍 닉네임 중복 확인: {}", nickname);

        if (nickname == null || nickname.length() < 2 || nickname.length() > 20) {
            log.warn("❌ 닉네임 길이 오류: {}", nickname);
            return ResponseEntity.badRequest().body(Map.of(
                    "available", false,
                    "message", "닉네임은 2자 이상 20자 이하여야 합니다."
            ));
        }

        boolean available = authService.isNicknameAvailable(nickname);

        log.info("✅ 닉네임 확인 완료: {} (가능: {})", nickname, available);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "✅ 사용 가능한 닉네임입니다." : "❌ 이미 사용 중인 닉네임입니다."
        ));
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static class SignupRequest {
        private String email;
        private String password;
        private String name;
        private String nickname;
        private String role;

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
