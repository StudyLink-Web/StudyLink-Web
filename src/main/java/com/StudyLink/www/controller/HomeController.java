package com.StudyLink.www.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * 홈 페이지 렌더링
     * GET / → templates/index.html
     */
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        // 현재 로그인한 사용자 정보를 모델에 추가
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("user", authentication.getPrincipal());
        }
        return "index";
    }

    /**
     * /index 경로도 처리
     */
    @GetMapping("/index")
    public String index(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("user", authentication.getPrincipal());
        }
        return "index";
    }
}
