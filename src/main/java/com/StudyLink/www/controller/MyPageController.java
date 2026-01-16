package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/my-page")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final MyPageService myPageService;
    private final UserRepository userRepository;

    /**
     * 마이페이지 메인 페이지
     * username으로 조회하도록 변경
     */
    @GetMapping
    public String myPage(Authentication authentication, Model model) {
        try {
            log.info("════════════════════════════════════════════════════════════");
            log.info("🚀 마이페이지 요청 시작");
            log.info("════════════════════════════════════════════════════════════");

            if (authentication == null) {
                log.error("❌ Authentication is null!");
                return "redirect:/login";
            }

            String username = authentication.getName();
            log.info("🔍 authentication.getName(): {}", username);
            log.info("🔍 Principal 타입: {}", authentication.getPrincipal().getClass().getSimpleName());

            // ⭐ 직접 principal에서 attributes 추출
            Users user = null;

            try {
                var principal = authentication.getPrincipal();
                log.info("🔍 [DEBUG] Principal: {}", principal);

                // OAuth2User 타입 확인
                if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                    org.springframework.security.oauth2.core.user.OAuth2User oauth2Principal =
                            (org.springframework.security.oauth2.core.user.OAuth2User) principal;

                    Map<String, Object> attributes = oauth2Principal.getAttributes();
                    log.info("🔍 [DEBUG] OAuth2 attributes: {}", attributes.keySet());

                    // username을 attributes에서 추출
                    String actualUsername = (String) attributes.get("username");
                    log.info("🔍 [DEBUG] attributes에서 추출한 username: {}", actualUsername);

                    if (actualUsername != null && !actualUsername.isEmpty()) {
                        // attributes의 username으로 조회
                        var userOpt = userRepository.findByUsername(actualUsername);
                        if (userOpt.isPresent()) {
                            user = userOpt.get();
                            log.info("✅ attributes의 username으로 사용자 찾음: {}", actualUsername);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("❌ Principal 파싱 오류: {}", e.getMessage());
            }

            // ⭐ attributes에서 못 찾으면 기존 로직 시도
            if (user == null) {
                // 1️⃣ authentication.getName()으로 시도 (이메일)
                var userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                    log.info("✅ username으로 사용자 찾음: {}", username);
                } else {
                    // 2️⃣ 이메일로 시도
                    userOpt = userRepository.findByEmail(username);
                    if (userOpt.isPresent()) {
                        user = userOpt.get();
                        log.info("✅ email으로 사용자 찾음: {}", username);
                    }
                }
            }

            // ⭐ 여전히 못 찾으면 에러
            if (user == null) {
                log.error("❌ 사용자를 찾을 수 없습니다: {}", username);
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다");
            }

            log.info("✅ 사용자 조회 성공!");
            log.info("   - userId: {}", user.getUserId());
            log.info("   - email: {}", user.getEmail());
            log.info("   - username: {}", user.getUsername());
            log.info("   - name: {}", user.getName());

            // 마이페이지 데이터 조회
            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());
            myPageData.put("user", user);

            model.addAttribute("user", user);
            model.addAttribute("myPageData", myPageData);
            model.addAttribute("activeTab", "profile");

            log.info("════════════════════════════════════════════════════════════");
            log.info("✅ 마이페이지 접속 성공: userId={}, email={}", user.getUserId(), user.getEmail());
            log.info("════════════════════════════════════════════════════════════");

            return "mypage/my-page";

        } catch (Exception e) {
            log.error("❌ 마이페이지 오류: {}", e.getMessage());
            log.error("📍 스택 트레이스:", e);
            e.printStackTrace();
            return "redirect:/login";
        }
    }


    @GetMapping("/profile")
    public String profileTab(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            // ⭐ 수정: OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // ⭐ 추가: myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            model.addAttribute("user", user);
            model.addAttribute("myPageData", myPageData);
            model.addAttribute("activeTab", "profile");

            log.info("✅ 프로필 탭 접속: userId={}", user.getUserId());
            return "mypage/my-page";

        } catch (Exception e) {
            log.error("❌ 프로필 탭 오류: {}", e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/account")
    public String accountTab(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            // ⭐ 수정: OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // ⭐ 추가: myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            model.addAttribute("user", user);
            model.addAttribute("myPageData", myPageData);
            model.addAttribute("activeTab", "account");

            log.info("✅ 계정 탭 접속: userId={}", user.getUserId());
            return "mypage/my-page";

        } catch (Exception e) {
            log.error("❌ 계정 탭 오류: {}", e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/notifications")
    public String notificationsTab(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            // ⭐ 수정: OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // ⭐ 추가: myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            model.addAttribute("user", user);
            model.addAttribute("myPageData", myPageData);
            model.addAttribute("activeTab", "notifications");

            log.info("✅ 알림 설정 탭 접속: userId={}", user.getUserId());
            return "mypage/my-page";

        } catch (Exception e) {
            log.error("❌ 알림 설정 탭 오류: {}", e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/settings")
    public String settingsTab(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            // ⭐ 수정: OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // ⭐ 추가: myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            model.addAttribute("user", user);
            model.addAttribute("myPageData", myPageData);
            model.addAttribute("activeTab", "settings");

            log.info("✅ 설정 탭 접속: userId={}", user.getUserId());
            return "mypage/my-page";

        } catch (Exception e) {
            log.error("❌ 설정 탭 오류: {}", e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/api/data")
    @ResponseBody
    public Map<String, Object> getMyPageData(Authentication authentication) {
        try {
            String username = authentication.getName();
            // ⭐ 수정: OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // ⭐ 추가: myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", myPageData);

            log.info("✅ 마이페이지 데이터 조회: userId={}", user.getUserId());
            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 마이페이지 데이터 조회 오류: {}", e.getMessage());
            e.printStackTrace();
            return errorResponse;
        }
    }

    @GetMapping("/api/user-info")
    @ResponseBody
    public Map<String, Object> getUserInfo(Authentication authentication) {
        try {
            String username = authentication.getName();
            // ⭐ 수정: OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> userInfo = myPageService.getUserInfo(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userInfo);

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return errorResponse;
        }
    }

    @GetMapping("/api/settings")
    @ResponseBody
    public Map<String, Object> getUserSettings(Authentication authentication) {
        try {
            String username = authentication.getName();
            // ⭐ 수정: OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> settings = myPageService.getUserSettings(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", settings);

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return errorResponse;
        }
    }

    @GetMapping("/api/unread-notifications")
    @ResponseBody
    public Map<String, Object> getUnreadNotificationCount(Authentication authentication) {
        try {
            String username = authentication.getName();
            // ⭐ 수정: OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            long unreadCount = myPageService.getUnreadNotificationCount(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unreadCount", unreadCount);

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return errorResponse;
        }
    }

    @GetMapping("/api/role")
    @ResponseBody
    public Map<String, Object> getUserRole(Authentication authentication) {
        try {
            String username = authentication.getName();
            // ⭐ 수정: OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            String role = myPageService.getUserRole(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("role", role);

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return errorResponse;
        }
    }

    @GetMapping("/profile/{userId}")
    public String viewUserProfile(@PathVariable Long userId, Model model) {
        try {
            Users targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            model.addAttribute("targetUser", targetUser);

            log.info("✅ 사용자 프로필 조회: userId={}", userId);
            return "profile/public-profile";

        } catch (Exception e) {
            log.error("❌ 사용자 프로필 조회 오류: {}", e.getMessage());
            e.printStackTrace();
            return "redirect:/";
        }
    }

    private boolean hasAccessToMyPage(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser");
    }
}