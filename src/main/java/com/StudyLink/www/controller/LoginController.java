/*package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final UserRepository userRepository;  // ✅ user_repository → userRepository

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "EMAIL_ALREADY_EXISTS",
                    "message", "이미 존재하는 이메일입니다."
            ));
        }

        Users user = Users.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .nickname(request.getNickname())
                .role(request.getRole())
                .createdAt(LocalDateTime.now())  // ✅ created_at → createdAt
                .updatedAt(LocalDateTime.now())  // ✅ updated_at → updatedAt
                .build();

        Users saved = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "userId", saved.getUserId(),  // ✅ getUser_id() → getUserId()
                "email", saved.getEmail(),
                "name", saved.getName(),
                "role", saved.getRole(),
                "message", "회원가입이 완료되었습니다."
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Optional<Users> user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "USER_NOT_FOUND",
                    "message", "존재하지 않는 사용자입니다."
            ));
        }

        Users foundUser = user.get();  // ✅ found_user → foundUser

        if (!foundUser.getPassword().equals(request.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "INVALID_PASSWORD",
                    "message", "비밀번호가 틀렸습니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "userId", foundUser.getUserId(),  // ✅ getUser_id() → getUserId()
                "email", foundUser.getEmail(),
                "name", foundUser.getName(),
                "role", foundUser.getRole(),
                "message", "로그인에 성공했습니다."
        ));
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
*/

package com.StudyLink.www.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // ✅ GET /login - 로그인 페이지 표시
    @GetMapping("/login")
    public String login() {
        return "login/login";  // templates/login/login.html
    }

    // ✅ GET /signup - 회원가입 페이지 표시
    @GetMapping("/signup")
    public String signup() {
        return "signup/signup";  // ✅ 폴더 경로 포함
    }
}
