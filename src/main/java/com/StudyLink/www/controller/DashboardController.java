package com.StudyLink.www.controller;

import com.StudyLink.www.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final AuthService authService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            authService.getUserByEmail(authentication.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
            });
            model.addAttribute("isAuthenticated", true);
        } else {
            return "redirect:/login";
        }
        return "dashboard/dashboard";
    }
}
