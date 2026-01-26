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
import org.springframework.web.multipart.MultipartFile;
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

    private String loginEmail(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    private Long loginUserId(Authentication authentication) {
        String email = loginEmail(authentication);
        if (email == null || email.isBlank()) return null;
        return userService.findUserIdByUsername(email);
    }

    @GetMapping("/register")
    public String register(Authentication authentication, Model model) {
        if (!isLogin(authentication)) return "redirect:/login";
        model.addAttribute("loginEmail", authentication.getName());
        return "community/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute CommunityDTO communityDTO,
                           @RequestParam(value = "files", required = false) MultipartFile[] files,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

        if (!isLogin(authentication)) return "redirect:/login";

        String email = loginEmail(authentication);
        Long userId = loginUserId(authentication);
        if (userId == null) return "redirect:/login";

        communityDTO.setUserId(userId);
        communityDTO.setEmail(email);
        communityDTO.setWriter(email);
        communityDTO.setRole("USER");
        communityDTO.setReadCount(0);
        communityDTO.setCmtQty(0);

        int fileQty = 0;
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) fileQty++;
            }
        }
        communityDTO.setFileQty(fileQty);

        Long savedBno = communityService.insert(communityDTO, files);

        redirectAttributes.addAttribute("bno", savedBno);
        return "redirect:/community/list";
    }

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(defaultValue = "1") int pageNo,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String keyword) {

        int safePageNo = Math.max(pageNo, 1);
        Page<CommunityDTO> page = communityService.getList(safePageNo);
        PageHandler<CommunityDTO> ph = new PageHandler<>(page, safePageNo, type, keyword);

        model.addAttribute("ph", ph);
        return "community/list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam Long bno,
                         @RequestParam(required = false) String mode,
                         Model model,
                         Authentication authentication) {

        CommunityDTO communityDTO = communityService.getDetail(bno);
        if (communityDTO == null) return "error/404";

        boolean modifyMode = "modify".equals(mode);
        if (modifyMode) {
            if (!isLogin(authentication)) return "redirect:/login";
            if (!loginEmail(authentication).equals(communityDTO.getWriter())) {
                return "redirect:/community/detail?bno=" + bno;
            }
        }

        model.addAttribute("communityDTO", communityDTO);
        model.addAttribute("mode", modifyMode ? "modify" : "read");
        return "community/detail";
    }

    @PostMapping("/modify")
    public String modify(@ModelAttribute CommunityDTO communityDTO,
                         @RequestParam(value = "files", required = false) MultipartFile[] files,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {

        if (!isLogin(authentication)) return "redirect:/login";

        CommunityDTO origin = communityService.getDetail(communityDTO.getBno());
        if (origin == null) return "error/404";

        if (!loginEmail(authentication).equals(origin.getWriter())) {
            return "redirect:/community/detail?bno=" + communityDTO.getBno();
        }

        communityDTO.setUserId(origin.getUserId());
        communityDTO.setEmail(origin.getEmail());
        communityDTO.setWriter(origin.getWriter());
        communityDTO.setRole(origin.getRole());
        communityDTO.setReadCount(origin.getReadCount());
        communityDTO.setCmtQty(origin.getCmtQty());

        int fileQty = 0;
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) fileQty++;
            }
        }
        communityDTO.setFileQty(fileQty > 0 ? fileQty : origin.getFileQty());

        Long savedBno = communityService.modify(communityDTO);
        redirectAttributes.addAttribute("bno", savedBno);
        return "redirect:/community/detail";
    }

    @GetMapping("/remove")
    public String remove(@RequestParam Long bno,
                         Authentication authentication) {

        if (!isLogin(authentication)) return "redirect:/login";

        CommunityDTO origin = communityService.getDetail(bno);
        if (origin == null) return "error/404";

        if (!loginEmail(authentication).equals(origin.getWriter())) {
            return "redirect:/community/detail?bno=" + bno;
        }

        communityService.remove(bno);
        return "redirect:/community/list";
    }
}
