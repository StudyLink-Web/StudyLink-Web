package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class LoginController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String login(Authentication authentication,
                        @RequestParam(required = false) String error,
                        @RequestParam(required = false) String expired,
                        Model model) {

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (expired != null) {
            model.addAttribute("expired", "세션이 만료되었습니다. 다시 로그인해주세요.");
        }

        return "login/login";
    }

    @GetMapping("/signup")
    public String signup(Authentication authentication) {

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        return "signup/signup";
    }

    // ✅ 기존 GET /api/auth/me 매핑 제거 (AuthController의 /api/auth/me 와 충돌 방지)
    // 필요하면 다른 URL로 제공
    @GetMapping("/user/profile")
    public String profile(Authentication authentication, Model model) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String identifier = authentication.getName();

        Users user = authService.findByIdentifier(identifier);
        model.addAttribute("user", user);

        log.info("✅ 현재 로그인된 사용자: {}", identifier);
        return "user/profile";
    }

    @GetMapping("/logout")
    public String logout() {
        log.info("✅ 로그아웃 처리");
        return "redirect:/";
    }
}
