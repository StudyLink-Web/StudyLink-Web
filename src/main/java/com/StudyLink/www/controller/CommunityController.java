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

        Long savedId = communityService.insert(communityDTO);
        return "redirect:/community/detail?userId=" + savedId;
    }

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                       @RequestParam(name = "type", required = false) String type,
                       @RequestParam(name = "keyword", required = false) String keyword) {

        // ✅ pageNo 방어 (0/음수 들어오면 PageRequest에서 터질 수 있음)
        if (pageNo < 1) pageNo = 1;

        // ✅ null/공백 정리 (템플릿/링크 생성 시 "null" 문자열 방지)
        type = (type == null || type.isBlank()) ? "" : type.trim();
        keyword = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();

        try {
            // ✅ 가능하면 서비스도 type/keyword 반영하는 메서드로 연결하는 게 정상
            //    (없으면 우선 기존 getList(pageNo) 유지)
            Page<CommunityDTO> page = communityService.getList(pageNo);

            // ✅ PageHandler 생성
            PageHandler<CommunityDTO> ph = new PageHandler<>(page, pageNo, type, keyword);

            model.addAttribute("ph", ph);
            return "community/list";

        } catch (Exception e) {
            // ✅ 500 원인 로그를 확실히 남기기
            log.error("community/list 500. pageNo={}, type='{}', keyword='{}'", pageNo, type, keyword, e);

            // 화면에서 확인 가능한 에러 페이지로 보내기(원하면 community/list에 빈 ph 주고 보여줘도 됨)
            return "error/500";
        }
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("userId") Long userId, Model model) {
        try {
            CommunityDTO communityDTO = communityService.getDetail(userId);
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

        String email = authentication.getName();
        Long loginUserId = userService.findUserIdByUsername(email);

        if (loginUserId == null) {
            log.error("remove: userId 조회 실패. email={}", email);
            return "redirect:/error/500";
        }

        if (!userId.equals(loginUserId)) {
            return "redirect:/error/403";
        }

        communityService.remove(userId);
        return "redirect:/community/list";
    }
}
