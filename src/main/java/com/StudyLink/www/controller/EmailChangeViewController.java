package com.StudyLink.www.controller;

import com.StudyLink.www.dto.VerifyEmailChangeResult;
import com.StudyLink.www.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EmailChangeViewController {

    private final AccountService accountService;

    // ✅ 이메일 링크 클릭 시 여기로 들어오게 만들 것
    @GetMapping("/account/change-email/confirm")
    public String confirmChangeEmail(
            @RequestParam("token") String token,
            @RequestParam("username") String username,
            Model model
    ) {
        try {
            VerifyEmailChangeResult result = accountService.confirmEmailChangeResult(token, username);

            model.addAttribute("title", "이메일이 변경되었습니다.");
            model.addAttribute("maskedEmail", result.maskedEmail());
            model.addAttribute("isUniversityEmail", result.isUniversityEmail());

            return "verify/email_change_success";

        } catch (IllegalArgumentException e) {
            model.addAttribute("reason", e.getMessage());
            return "verify/email_change_fail";

        } catch (Exception e) {
            log.error("❌ 이메일 변경 confirm 오류", e);
            model.addAttribute("reason", "서버 오류가 발생했습니다.");
            return "verify/email_change_fail";
        }
    }
}
