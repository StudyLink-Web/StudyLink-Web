package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.StudentVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/auth/student-verification")
@RequiredArgsConstructor
@Slf4j
public class StudentVerificationController {

    private final StudentVerificationService verificationService;
    private final UserRepository userRepository;

    /**
     * 대학생 인증 페이지 표시
     * 로그인 필수!
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String showVerificationPage(Model model) {
        // 현재 로그인한 사용자 정보 추가
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            String displayName = username;

            // ⭐ 추가: OAuth2 사용자인 경우 principal에서 직접 name 가져오기
            if (auth instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) auth;
                OAuth2User principal = oauth2Auth.getPrincipal();

                String nameFromOAuth = (String) principal.getAttribute("name");
                if (nameFromOAuth != null && !nameFromOAuth.isEmpty()) {
                    displayName = nameFromOAuth;
                    log.info("✅ OAuth2에서 name 직접 가져옴: {}", displayName);
                }
            }

            // OAuth2가 아닌 경우 또는 name이 없으면 DB에서 조회
            if (displayName.equals(username)) {
                Optional<Users> userOpt = userRepository.findByUsername(username)
                        .or(() -> userRepository.findByEmail(username));

                if (userOpt.isPresent()) {
                    Users user = userOpt.get();
                    displayName = user.getName();
                    model.addAttribute("username", user.getUsername());
                    model.addAttribute("name", displayName);  // ⭐ 추가
                    log.info("✅ 사용자 정보 전달: name={}, username={}",
                            displayName, user.getUsername());
                } else {
                    model.addAttribute("username", username);
                    model.addAttribute("name", displayName);
                }
            } else {
                model.addAttribute("username", username);
                model.addAttribute("name", displayName);
                log.info("✅ 사용자 정보 전달 (OAuth2): name={}, username={}", displayName, username);
            }
        }

        model.addAttribute("title", "대학생 인증");
        return "auth/student-verification";
    }


    /**
     * AJAX: 학교 이메일 중복 확인
     * ⭐ 로그인 필수!
     */
    @GetMapping("/check-email")
    @PreAuthorize("isAuthenticated()")  // ⭐ 로그인한 사용자만 접근 가능
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkSchoolEmail(@RequestParam String email) {
        log.info("🔍 학교 이메일 확인: {}", email);
        Map<String, Object> response = verificationService.checkSchoolEmailAvailability(email);
        return ResponseEntity.ok(response);
    }

    /**
     * AJAX: 인증 이메일 요청
     * ⭐ 로그인 필수!
     */
    @PostMapping("/request-verification")
    @PreAuthorize("isAuthenticated()")  // ⭐ 로그인한 사용자만 접근 가능
    @ResponseBody
    public ResponseEntity<Map<String, Object>> requestVerification(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");

        // 현재 로그인 사용자 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            log.info("📧 인증 이메일 요청: {} (사용자: {})", email, auth.getName());
        }

        Map<String, Object> response = verificationService.requestEmailVerification(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 인증 링크 클릭 (GET)
     * ⭐ 이 부분은 로그인 불필요! (토큰으로 인증)
     */
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        log.info("✅ 이메일 인증 요청: {}", token);

        Map<String, Object> result = verificationService.verifyEmail(token);

        model.addAttribute("success", result.get("success"));
        model.addAttribute("message", result.get("message"));
        model.addAttribute("code", result.get("code"));

        // 성공 시 추가 정보 표시
        if ((boolean) result.get("success")) {
            model.addAttribute("schoolEmail", result.get("schoolEmail"));
            model.addAttribute("role", result.get("role"));
            model.addAttribute("verifiedAt", result.get("verifiedAt"));
        }

        return "auth/verification-result";
    }

    /**
     * AJAX: 인증 상태 조회
     * ⭐ 수정됨: Repository를 통해 안전하게 사용자 조회
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")  // 로그인한 사용자만 접근 가능
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVerificationStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 현재 로그인 사용자 정보 조회
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                response.put("verified", false);
                response.put("message", "로그인이 필요합니다");
                response.put("code", "NOT_AUTHENTICATED");
                return ResponseEntity.ok(response);
            }

            // ⭐ 수정됨: Repository를 통해 안전하게 조회
            String username = auth.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username));

            if (userOpt.isEmpty()) {
                response.put("verified", false);
                response.put("message", "사용자 정보를 조회할 수 없습니다");
                response.put("code", "USER_NOT_FOUND");
                return ResponseEntity.ok(response);
            }

            Long userId = userOpt.get().getUserId();
            response = verificationService.getVerificationStatus(userId);
            response.put("code", "SUCCESS");

        } catch (Exception e) {
            log.error("❌ 인증 상태 조회 중 오류", e);
            response.put("verified", false);
            response.put("message", "상태 조회 중 오류가 발생했습니다");
            response.put("code", "SERVER_ERROR");
        }

        return ResponseEntity.ok(response);
    }



    /**
     * ⭐ 테스트용: 이메일 토큰 초기화 (재인증 요청 가능하게 함)
     * 개발 환경에서만 사용!
     */
    @PostMapping("/reset-token")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetVerificationToken(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        log.warn("⚠️ [테스트용] 이메일 토큰 초기화 요청: {}", email);

        Map<String, Object> response = verificationService.resetVerificationToken(email);
        return ResponseEntity.ok(response);
    }
}
