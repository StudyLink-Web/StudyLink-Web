package com.StudyLink.www.controller;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.handler.PageHandler;
import com.StudyLink.www.service.CommunityService;
import com.StudyLink.www.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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
        model.addAttribute("loginEmail", authentication.getName());
        return "community/register";
    }

    @PostMapping("/register")
    public String register(CommunityDTO communityDTO, Authentication authentication) {
        if (!isLogin(authentication)) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Long userId = userService.findUserIdByUsername(email);
        if (userId == null) {
            log.error("register: userId 조회 실패. email={}", email);
            return "redirect:/error/500";
        }

        communityDTO.setUserId(userId);
        communityDTO.setEmail(email);

        if (communityDTO.getRole() == null || communityDTO.getRole().isBlank()) {
            communityDTO.setRole("USER");
        }

        Long savedBno = communityService.insert(communityDTO);
        return "redirect:/community/detail?bno=" + savedBno;
    }

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                       @RequestParam(name = "type", required = false) String type,
                       @RequestParam(name = "keyword", required = false) String keyword) {

        if (pageNo < 1) pageNo = 1;

        type = (type == null || type.isBlank()) ? "" : type.trim();
        keyword = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();

        try {
            Page<CommunityDTO> page = communityService.getList(pageNo);

            PageHandler<CommunityDTO> ph = new PageHandler<>(page, pageNo, type, keyword);

            model.addAttribute("ph", ph);
            return "community/list";

        } catch (Exception e) {
            log.error("community/list 500. pageNo={}, type='{}', keyword='{}'", pageNo, type, keyword, e);
            return "error/500";
        }
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("bno") Long bno, Model model) {
        try {
            CommunityDTO communityDTO = communityService.getDetail(bno);
            if (communityDTO == null) return "error/404";

            model.addAttribute("communityDTO", communityDTO);
            return "community/detail";
        } catch (EntityNotFoundException e) {
            return "error/404";
        }
    }

    @PostMapping("/modify")
    public String modify(CommunityDTO communityDTO,
                         RedirectAttributes redirectAttributes,
                         Authentication authentication) {

        if (!isLogin(authentication)) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Long loginUserId = userService.findUserIdByUsername(email);
        if (loginUserId == null) {
            log.error("modify: userId 조회 실패. email={}", email);
            return "redirect:/error/500";
        }

        communityDTO.setUserId(loginUserId);
        communityDTO.setEmail(email);

        if (communityDTO.getRole() == null || communityDTO.getRole().isBlank()) {
            communityDTO.setRole("USER");
        }

        Long savedBno = communityService.modify(communityDTO);
        redirectAttributes.addAttribute("bno", savedBno);
        return "redirect:/community/detail";
    }

    @GetMapping("/remove")
    public String remove(@RequestParam("bno") Long bno,
                         Authentication authentication) {

        if (!isLogin(authentication)) {
            return "redirect:/login";
        }

        communityService.remove(bno);
        return "redirect:/community/list";
    }
}
