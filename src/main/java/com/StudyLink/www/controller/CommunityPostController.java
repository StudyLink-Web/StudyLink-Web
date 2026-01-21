package com.StudyLink.www.controller;

import com.StudyLink.www.dto.CommunityPostDTO;
import com.StudyLink.www.service.CommunityPostService;
import com.StudyLink.www.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/community/post")
public class CommunityPostController {

    private final CommunityPostService postService;
    private final UserService userService;

    private boolean isLogin(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private Long loginUserId(Authentication auth) {
        if (!isLogin(auth)) return null;
        String usernameOrEmail = auth.getName();
        return userService.findUserIdByUsername(usernameOrEmail);
    }

    // ✅ 글쓰기 폼
    @GetMapping("/register")
    public String registerForm(Model model, Authentication auth, RedirectAttributes ra) {
        Long uid = loginUserId(auth);
        if (uid == null) return "redirect:/login";

        // ✅ 템플릿에서 th:object="${post}" 이므로 key는 무조건 "post"
        model.addAttribute("post", new CommunityPostDTO());
        return "community/post_register";
    }

    // ✅ 글 등록 처리
    @PostMapping("/register")
    public String register(@ModelAttribute("post") CommunityPostDTO dto,
                           Authentication auth,
                           RedirectAttributes ra) {
        Long uid = loginUserId(auth);
        if (uid == null) return "redirect:/login";

        try {
            Long postId = postService.register(dto, uid);
            return "redirect:/community/post/detail?postId=" + postId;
        } catch (Exception e) {
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect:/community/post/register";
        }
    }

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int pageNo, Model model) {
        model.addAttribute("page", postService.list(pageNo));
        return "community/post_list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam Long postId, Model model) {
        model.addAttribute("post", postService.detail(postId));
        return "community/post_detail";
    }
}
