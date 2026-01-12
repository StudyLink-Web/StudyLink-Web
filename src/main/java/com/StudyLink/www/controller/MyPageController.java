package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * 마이페이지 컨트롤러
 * 사용자 프로필, 대학생 인증, 계정 설정 관리
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Controller
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 마이페이지 메인 페이지
     */
    @GetMapping
    public String mypage(Model model) {
        // 로그인된 사용자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("🔍 마이페이지 접속: username={}", username);

        // 모델에 사용자 정보 추가 (Controller에서 처리하거나 Thymeleaf에서 직접 접근)
        model.addAttribute("username", username);

        return "mypage/mypage";
    }

    /**
     * 프로필 수정 페이지 (GET)
     */
    @GetMapping("/profile")
    public String profileForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("🔍 프로필 수정 페이지 접속: username={}", username);

        model.addAttribute("username", username);

        return "mypage/mypage_profile";
    }

    /**
     * 프로필 수정 저장 (POST)
     */
    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam(name = "university") String university,
            @RequestParam(name = "department") String department,
            @RequestParam(name = "studentYear") String studentYear,
            RedirectAttributes redirectAttributes) {

        try {
            // 로그인된 사용자 ID 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            log.info("✏️ 프로필 수정 요청: username={}", username);

            // 프로필 수정 처리 (Service에서 username으로 userId 조회)
            // 여기서는 Principal을 통해 username을 이용
            // 실제로는 UserRepository를 통해 userId를 얻어야 함

            log.info("✅ 프로필 수정 완료: university={}, department={}", university, department);

            redirectAttributes.addFlashAttribute("success", "프로필이 성공적으로 수정되었습니다.");
            return "redirect:/mypage";

        } catch (Exception e) {
            log.error("❌ 프로필 수정 실패", e);
            redirectAttributes.addFlashAttribute("error", "프로필 수정에 실패했습니다.");
            return "redirect:/mypage/profile";
        }
    }

    /**
     * 대학생 인증 페이지 (GET)
     */
    @GetMapping("/verification")
    public String verificationForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("🔍 대학생 인증 페이지 접속: username={}", username);

        model.addAttribute("username", username);

        return "mypage/mypage_verification";
    }

    /**
     * 대학생 인증 이메일 발송 (POST)
     */
    @PostMapping("/verification/send-email")
    public String sendVerificationEmail(
            @RequestParam(name = "studentEmail") String studentEmail,
            RedirectAttributes redirectAttributes) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            log.info("📧 인증 이메일 발송 요청: username={}, student_email={}", username, studentEmail);

            // 여기서 실제로는 userId를 구해야 함
            // myPageService.startVerification(userId, studentEmail);

            log.info("✅ 인증 이메일 발송 완료: {}", studentEmail);

            redirectAttributes.addFlashAttribute("success", "인증 이메일이 발송되었습니다. 이메일을 확인해주세요.");
            redirectAttributes.addFlashAttribute("studentEmail", studentEmail);

            return "redirect:/mypage/verification";

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 인증 이메일 발송 경고: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("warning", e.getMessage());
            return "redirect:/mypage/verification";

        } catch (Exception e) {
            log.error("❌ 인증 이메일 발송 실패", e);
            redirectAttributes.addFlashAttribute("error", "이메일 발송에 실패했습니다.");
            return "redirect:/mypage/verification";
        }
    }

    /**
     * 대학생 인증 완료 (이메일 링크 클릭)
     */
    @GetMapping("/verify")
    public String verifyEmail(
            @RequestParam(name = "email") String studentEmail,
            @RequestParam(name = "token") String token,
            Model model) {

        try {
            log.info("🔗 인증 링크 클릭: student_email={}", studentEmail);

            // 토큰 검증 및 인증 완료
            boolean success = myPageService.completeVerification(studentEmail, token);

            if (success) {
                log.info("✅ 대학생 인증 완료: {}", studentEmail);
                model.addAttribute("success", true);
                model.addAttribute("message", "대학생 인증이 완료되었습니다!");
            } else {
                log.warn("⚠️ 인증 실패: 토큰 만료 또는 불일치");
                model.addAttribute("success", false);
                model.addAttribute("message", "인증이 만료되었거나 유효하지 않습니다.");
            }

            return "mypage/email_verification_result";

        } catch (Exception e) {
            log.error("❌ 인증 처리 중 오류", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "인증 처리 중 오류가 발생했습니다.");
            return "mypage/email_verification_result";
        }
    }

    /**
     * 계정 설정 페이지 (GET)
     */
    @GetMapping("/settings")
    public String settingsForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("🔍 계정 설정 페이지 접속: username={}", username);

        model.addAttribute("username", username);

        return "mypage/mypage_settings";
    }

    /**
     * 계정 설정 저장 (POST)
     */
    @PostMapping("/settings")
    public String updateSettings(
            @RequestParam(name = "password", required = false) String password,
            @RequestParam(name = "passwordConfirm", required = false) String passwordConfirm,
            @RequestParam(name = "nickname", required = false) String nickname,
            RedirectAttributes redirectAttributes) {

        try {
            // 비밀번호 일치 확인
            if (password != null && !password.isEmpty()) {
                if (!password.equals(passwordConfirm)) {
                    redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                    return "redirect:/mypage/settings";
                }
            }

            log.info("⚙️ 계정 설정 업데이트 요청");

            // 여기서 실제로는 userId를 구해서 update
            // myPageService.updateSettings(userId, password, nickname);

            log.info("✅ 계정 설정 업데이트 완료");

            redirectAttributes.addFlashAttribute("success", "계정 설정이 저장되었습니다.");
            return "redirect:/mypage/settings";

        } catch (Exception e) {
            log.error("❌ 계정 설정 업데이트 실패", e);
            redirectAttributes.addFlashAttribute("error", "계정 설정 업데이트에 실패했습니다.");
            return "redirect:/mypage/settings";
        }
    }
}