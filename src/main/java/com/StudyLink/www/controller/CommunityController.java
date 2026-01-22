package com.StudyLink.www.controller;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.handler.PageHandler;
import com.StudyLink.www.service.CommunityService;
import com.StudyLink.www.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/community")
public class CommunityController {

    private final CommunityService communityService;
    private final UserService userService;

    private boolean isLogin(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @GetMapping("/register")
    public String register(Authentication authentication, Model model) {
        if (!isLogin(authentication)) {
            return "redirect:/login";
        }

        // 화면에 보여줄 값(원하면 사용)
        model.addAttribute("loginEmail", authentication.getName());

        return "community/register";
    }

    @PostMapping("/register")
    public String register(CommunityDTO communityDTO,
                           Authentication authentication) {

        if (!isLogin(authentication)) {
            return "redirect:/login";
        }

        // ✅ 서버에서 필수값 세팅 (NOT NULL 방지)
        String email = authentication.getName();
        Long userId = userService.findUserIdByUsername(email);

        communityDTO.setUserId(userId);
        communityDTO.setEmail(email);

        // 폼에서 role을 안 받는다면 기본값 세팅
        if (communityDTO.getRole() == null || communityDTO.getRole().isBlank()) {
            communityDTO.setRole("USER");
        }

        Long savedId = communityService.insert(communityDTO);
        return "redirect:/community/detail?userId=" + savedId;
    }

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                       @RequestParam(name = "type", required = false) String type,
                       @RequestParam(name = "keyword", required = false) String keyword) {

        Page<CommunityDTO> page = communityService.getList(pageNo);
        PageHandler<CommunityDTO> ph = new PageHandler<>(page, pageNo, type, keyword);
        model.addAttribute("ph", ph);

        return "community/list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("userId") Long userId,
                         Model model) {

        CommunityDTO communityDTO = communityService.getDetail(userId);
        model.addAttribute("communityDTO", communityDTO);

        return "community/detail";
    }

    @PostMapping("/modify")
    public String modify(CommunityDTO communityDTO,
                         RedirectAttributes redirectAttributes,
                         Authentication authentication) {

        if (!isLogin(authentication)) {
            return "redirect:/login";
        }

        // ✅ 서버에서 email/userId 재세팅 (위변조 방지 + NOT NULL 방지)
        String email = authentication.getName();
        Long loginUserId = userService.findUserIdByUsername(email);

        communityDTO.setUserId(loginUserId);
        communityDTO.setEmail(email);

        if (communityDTO.getRole() == null || communityDTO.getRole().isBlank()) {
            communityDTO.setRole("USER");
        }

        Long savedId = communityService.modify(communityDTO);
        redirectAttributes.addAttribute("userId", savedId);
        return "redirect:/community/detail";
    }

    @GetMapping("/remove")
    public String remove(@RequestParam("userId") Long userId,
                         Authentication authentication) {

        if (!isLogin(authentication)) {
            return "redirect:/login";
        }

        // ✅ 본인만 삭제 가능 (최소 방어)
        String email = authentication.getName();
        Long loginUserId = userService.findUserIdByUsername(email);

        if (!userId.equals(loginUserId)) {
            return "redirect:/error/403";
        }

        communityService.remove(userId);
        return "redirect:/community/list";
    }
}
