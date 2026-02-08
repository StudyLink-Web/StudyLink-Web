package com.StudyLink.www.controller;

import com.StudyLink.www.dto.MentorProfileDTO;
import com.StudyLink.www.service.AuthService;
import com.StudyLink.www.service.MentorProfileService;
import com.StudyLink.www.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MentorProfileService mentorProfileService;
    private final AuthService authService;

    /**
     * 홈 페이지 렌더링
     * GET / → templates/index.html
     */
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        // 현재 로그인한 사용자 정보를 모델에 추가
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            authService.getUserByEmail(email).ifPresent(user -> {
                model.addAttribute("user", user);
            });
            model.addAttribute("isAuthenticated", true);
            System.out.println("✅ 로그인 사용자: " + authentication.getName());
        } else {
            model.addAttribute("isAuthenticated", false);
            System.out.println("비로그인 사용자");
        }

        // 🏆 상위 멘토 4명 조회 및 모델 추가
        List<MentorProfileDTO> topMentors = mentorProfileService.getTopMentorDTOs(4);
        model.addAttribute("topMentors", topMentors);

        return "modern_index";
    }

    /**
     * /index 경로도 처리
     */
    @GetMapping("/index")
    public String index(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("user", authentication.getPrincipal());
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
        }
        return "modern_index";
    }

    /**
     * 기존 바닐라 JS 기반 페이지 백업
     * GET /legacy → templates/index.html
     */
    @GetMapping("/legacy")
    public String legacyHome(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("user", authentication.getPrincipal());
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
        }
        return "index";
    }

    /**
     * 리액트 기반 모던 메인 페이지
     * GET /modern → templates/modern_index.html
     */
    @GetMapping("/modern")
    public String modernHome(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("user", authentication.getPrincipal());
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
        }
        return "modern_index";
    }

    /**
     * AI 대입 자소서 페이지 (리액트 라우팅 대응)
     */
    @GetMapping({ "/cover-letter", "/cover_letter", "/pricing", "/mentors", "/mentors/**" })
    public String coverLetter(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("user", authentication.getPrincipal());
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
        }
        return "modern_index";
    }
}
