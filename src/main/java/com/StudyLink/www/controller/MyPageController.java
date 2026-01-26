package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.MyPageService;
import com.StudyLink.www.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/my-page")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final MyPageService myPageService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

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

            // 직접 principal에서 attributes 추출
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

            // attributes에서 못 찾으면 기존 로직 시도
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

            // 여전히 못 찾으면 에러
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

            // header.html에서 사용할 변수들
            model.addAttribute("userName", user.getName());
            model.addAttribute("userRole", user.getRole().toString());

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
            // OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            model.addAttribute("user", user);
            model.addAttribute("myPageData", myPageData);
            model.addAttribute("activeTab", "profile");

            // header.html에서 사용할 변수들
            model.addAttribute("userName", user.getName());
            model.addAttribute("userRole", user.getRole().toString());

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
            // OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            model.addAttribute("user", user);
            model.addAttribute("myPageData", myPageData);
            model.addAttribute("activeTab", "account");

            // header.html에서 사용할 변수들
            model.addAttribute("userName", user.getName());
            model.addAttribute("userRole", user.getRole().toString());

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
            // OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            model.addAttribute("user", user);
            model.addAttribute("myPageData", myPageData);
            model.addAttribute("activeTab", "notifications");

            // header.html에서 사용할 변수들
            model.addAttribute("userName", user.getName());
            model.addAttribute("userRole", user.getRole().toString());

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
            // OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            model.addAttribute("user", user);
            model.addAttribute("myPageData", myPageData);
            model.addAttribute("activeTab", "settings");

            // header.html에서 사용할 변수들
            model.addAttribute("userName", user.getName());
            model.addAttribute("userRole", user.getRole().toString());

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
            // OAuth 사용자 지원
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            Map<String, Object> myPageData = myPageService.getMyPageData(user.getUserId());

            // myPageData에 실제 Users 엔티티 객체 저장
            myPageData.put("user", user);

            // header용 변수들
            myPageData.put("userName", user.getName());
            myPageData.put("userRole", user.getRole().toString());

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
            // OAuth 사용자 지원
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
            // OAuth 사용자 지원
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
            // OAuth 사용자 지원
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
            // OAuth 사용자 지원
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

    // 프로필 사진 업로드
    @PutMapping("/api/profile/image")
    @ResponseBody
    public Map<String, Object> updateProfileImage(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        log.info("═══════════════════════════════════════════════════════════════");
        log.info("🎬 [프로필 사진 업로드] 시작");
        log.info("═══════════════════════════════════════════════════════════════");

        try {
            // 1️⃣ Authentication 확인
            log.info("1️⃣ Authentication 확인");
            if (authentication == null) {
                log.error("❌ Authentication이 null입니다!");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "인증 정보가 없습니다");
                return response;
            }

            String username = authentication.getName();
            log.info("   ✓ username: {}", username);
            log.info("   ✓ isAuthenticated: {}", authentication.isAuthenticated());
            log.info("   ✓ Principal 타입: {}", authentication.getPrincipal().getClass().getSimpleName());

            // 2️⃣ Request Body 확인
            log.info("2️⃣ Request Body 확인");
            log.info("   ✓ request: {}", request);
            if (request == null) {
                log.error("❌ Request가 null입니다!");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "요청 데이터가 없습니다");
                return response;
            }

            String imageData = request.get("profileImageUrl");
            log.info("   ✓ profileImageUrl 키 존재: {}", request.containsKey("profileImageUrl"));
            log.info("   ✓ imageData 길이: {} bytes", imageData != null ? imageData.length() : 0);

            if (imageData != null && imageData.length() > 100) {
                log.info("   ✓ imageData 앞 100글자: {}", imageData.substring(0, 100) + "...");
            }

            if (imageData == null || imageData.isEmpty()) {
                log.error("❌ 이미지 데이터가 없거나 비어있습니다!");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "이미지 데이터가 없습니다");
                return response;
            }

            // 3️⃣ 사용자 조회
            log.info("3️⃣ 사용자 조회 시작");
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        log.warn("   ⚠️  username으로 못 찾음, email으로 시도: {}", username);
                        return userRepository.findByEmail(username)
                                .orElseThrow(() -> {
                                    log.error("❌ 사용자를 찾을 수 없습니다! (username: {}, email 시도: {})", username, username);
                                    return new IllegalArgumentException("사용자를 찾을 수 없습니다");
                                });
                    });

            log.info("   ✓ 사용자 찾음!");
            log.info("   ✓ userId: {}", user.getUserId());
            log.info("   ✓ name: {}", user.getName());
            log.info("   ✓ email: {}", user.getEmail());
            log.info("   ✓ 기존 profileImageUrl: {}", user.getProfileImageUrl() != null ? "있음 (길이: " + user.getProfileImageUrl().length() + ")" : "없음");

            // 4️⃣ 데이터 저장
            log.info("4️⃣ 프로필 이미지 저장 시작");
            user.setProfileImageUrl(imageData);
            log.info("   ✓ setProfileImageUrl 완료");

            Users savedUser = userRepository.save(user);
            log.info("   ✓ userRepository.save() 완료");
            log.info("   ✓ 저장된 userId: {}", savedUser.getUserId());
            log.info("   ✓ 저장된 profileImageUrl 길이: {}", savedUser.getProfileImageUrl() != null ? savedUser.getProfileImageUrl().length() : 0);

            // 5️⃣ 저장된 데이터 검증
            log.info("5️⃣ 저장된 데이터 검증");
            Users verifyUser = userRepository.findById(user.getUserId()).orElse(null);
            if (verifyUser != null && verifyUser.getProfileImageUrl() != null) {
                log.info("   ✓ DB 검증 성공! 저장된 길이: {}", verifyUser.getProfileImageUrl().length());
            } else {
                log.error("❌ DB 검증 실패! 저장된 데이터를 조회할 수 없습니다!");
            }

            // 6️⃣ 응답 생성
            log.info("6️⃣ 응답 생성");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필 사진이 업로드되었습니다");
            response.put("profileImageUrl", imageData.substring(0, Math.min(50, imageData.length())) + "...");
            response.put("userId", user.getUserId());

            log.info("═══════════════════════════════════════════════════════════════");
            log.info("✅ [프로필 사진 업로드] 완료!");
            log.info("═══════════════════════════════════════════════════════════════");

            return response;

        } catch (Exception e) {
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("❌ [프로필 사진 업로드] 오류 발생!");
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("예외 타입: {}", e.getClass().getName());
            log.error("예외 메시지: {}", e.getMessage());
            log.error("스택 트레이스:", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "업로드 실패: " + e.getMessage());
            errorResponse.put("exceptionType", e.getClass().getName());

            return errorResponse;
        }
    }

    // 프로필 사진 삭제
    @DeleteMapping("/api/profile/image")
    @ResponseBody
    public Map<String, Object> deleteProfileImage(Authentication authentication) {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("🎬 [프로필 사진 삭제] 시작");
        log.info("═══════════════════════════════════════════════════════════════");

        try {
            // 1️⃣ 사용자 조회
            log.info("1️⃣ 사용자 조회");
            String username = authentication.getName();
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            log.info("   ✓ userId: {}", user.getUserId());

            // 2️⃣ 파일 삭제
            log.info("2️⃣ 파일 서버에서 삭제");
            fileStorageService.deleteProfileImage(user.getUserId());

            // 3️⃣ DB 업데이트
            log.info("3️⃣ DB 업데이트");
            user.setProfileImageUrl(null);
            userRepository.save(user);
            log.info("   ✓ DB 저장 완료");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필 사진이 삭제되었습니다");

            log.info("═══════════════════════════════════════════════════════════════");
            log.info("✅ [프로필 사진 삭제] 완료!");
            log.info("═══════════════════════════════════════════════════════════════");

            return response;

        } catch (Exception e) {
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("❌ [프로필 사진 삭제] 오류 발생!");
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("예외: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "삭제 실패: " + e.getMessage());

            return errorResponse;
        }
    }

    // 기본 정보 저장
    @PutMapping("/api/profile/comprehensive")
    @ResponseBody
    public Map<String, Object> updateBasicInfo(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Users user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다")));

            user.setName(request.get("name"));
            user.setNickname(request.get("nickname"));
            user.setPhone(request.get("phone"));
            user.setGradeYear(request.get("gradeYear"));
            user.setInterests(request.get("interests"));

            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "기본 정보가 저장되었습니다");

            log.info("✅ 기본 정보 저장 완료: userId={}", user.getUserId());
            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "저장 실패: " + e.getMessage());
            log.error("❌ 기본 정보 저장 오류:", e);
            return errorResponse;
        }
    }

    // 닉네임 중복 확인
    @GetMapping("/api/profile/check-nickname")
    @ResponseBody
    public Map<String, Object> checkNickname(@RequestParam String nickname) {
        try {
            boolean available = !userRepository.existsByNickname(nickname);

            Map<String, Object> response = new HashMap<>();
            response.put("available", available);

            log.info("✅ 닉네임 중복 확인: {} (available={})", nickname, available);
            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("available", false);
            log.error("❌ 닉네임 확인 오류:", e);
            return errorResponse;
        }
    }

    private boolean hasAccessToMyPage(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser");
    }
}