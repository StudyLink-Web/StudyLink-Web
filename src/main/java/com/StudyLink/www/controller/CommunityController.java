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
        return (authentication == null) ? null : authentication.getName();
    }

    private Long loginUserId(Authentication authentication) {
        String email = loginEmail(authentication);
        if (email == null || email.isBlank()) return null;
        try {
            return userService.findUserIdByUsername(email);
        } catch (Exception e) {
            log.error("findUserIdByUsername error. email={}", email, e);
            return null;
        }
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
        if (communityDTO.getRole() == null || communityDTO.getRole().isBlank()) communityDTO.setRole("USER");
        if (communityDTO.getWriter() == null || communityDTO.getWriter().isBlank()) communityDTO.setWriter(email);

        if (communityDTO.getReadCount() == null) communityDTO.setReadCount(0);
        if (communityDTO.getCmtQty() == null) communityDTO.setCmtQty(0);

        int fileCount = 0;
        if (files != null) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) fileCount++;
            }
        }
        communityDTO.setFileQty(fileCount);

        Long savedBno;
        try {
            savedBno = communityService.insert(communityDTO, files);
        } catch (Exception e) {
            log.error("community register error. dto={}", communityDTO, e);
            return "error/500";
        }

        redirectAttributes.addAttribute("bno", savedBno);
        return "redirect:/community/list";
    }

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                       @RequestParam(name = "type", required = false) String type,
                       @RequestParam(name = "keyword", required = false) String keyword) {

        int safePageNo = Math.max(pageNo, 1);

        String safeType = (type == null) ? "" : type.trim();
        String safeKeyword = (keyword == null) ? "" : keyword.trim();

        Page<CommunityDTO> page = communityService.getList(safePageNo);
        PageHandler<CommunityDTO> ph = new PageHandler<>(page, safePageNo, safeType, safeKeyword);

        model.addAttribute("ph", ph);
        return "community/list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("bno") Long bno,
                         @RequestParam(name = "mode", required = false) String mode,
                         Model model,
                         Authentication authentication) {

        try {
            if (bno == null) return "error/404";

            CommunityDTO communityDTO = communityService.getDetail(bno);
            if (communityDTO == null) return "error/404";

            String safeMode = (mode == null) ? "" : mode.trim();
            boolean modifyMode = "modify".equalsIgnoreCase(safeMode);

            if (modifyMode) {
                if (!isLogin(authentication)) return "redirect:/login";
                String loginEmail = loginEmail(authentication);
                if (loginEmail == null || !loginEmail.equals(communityDTO.getWriter())) {
                    return "redirect:/community/detail?bno=" + bno;
                }
            }

            model.addAttribute("communityDTO", communityDTO);
            model.addAttribute("mode", modifyMode ? "modify" : "read");
            return "community/detail";
        } catch (Exception e) {
            log.error("community detail error. bno={}", bno, e);
            return "error/500";
        }
    }

    @PostMapping("/modify")
    public String modify(@ModelAttribute CommunityDTO communityDTO,
                         RedirectAttributes redirectAttributes,
                         Authentication authentication) {

        if (!isLogin(authentication)) return "redirect:/login";

        String email = loginEmail(authentication);
        Long userId = loginUserId(authentication);
        if (userId == null) return "redirect:/login";

        if (communityDTO.getBno() == null) return "redirect:/community/list";

        CommunityDTO origin = communityService.getDetail(communityDTO.getBno());
        if (origin == null) return "error/404";

        if (!email.equals(origin.getWriter())) {
            return "redirect:/community/detail?bno=" + communityDTO.getBno();
        }

        communityDTO.setUserId(userId);
        communityDTO.setEmail(email);
        if (communityDTO.getRole() == null || communityDTO.getRole().isBlank()) communityDTO.setRole("USER");
        if (communityDTO.getWriter() == null || communityDTO.getWriter().isBlank()) communityDTO.setWriter(origin.getWriter());

        if (communityDTO.getReadCount() == null) communityDTO.setReadCount(origin.getReadCount() == null ? 0 : origin.getReadCount());
        if (communityDTO.getCmtQty() == null) communityDTO.setCmtQty(origin.getCmtQty() == null ? 0 : origin.getCmtQty());
        if (communityDTO.getFileQty() == null) communityDTO.setFileQty(origin.getFileQty() == null ? 0 : origin.getFileQty());

        Long savedBno;
        try {
            savedBno = communityService.modify(communityDTO);
        } catch (Exception e) {
            log.error("community modify error. dto={}", communityDTO, e);
            return "error/500";
        }

        redirectAttributes.addAttribute("bno", savedBno);
        return "redirect:/community/detail";
    }

    @GetMapping("/remove")
    public String remove(@RequestParam("bno") Long bno,
                         Authentication authentication) {

        if (!isLogin(authentication)) return "redirect:/login";
        if (bno == null) return "redirect:/community/list";

        String email = loginEmail(authentication);
        CommunityDTO origin = communityService.getDetail(bno);
        if (origin == null) return "error/404";

        if (email == null || !email.equals(origin.getWriter())) {
            return "redirect:/community/detail?bno=" + bno;
        }

        try {
            communityService.remove(bno);
        } catch (Exception e) {
            log.error("community remove error. bno={}", bno, e);
            return "error/500";
        }

        return "redirect:/community/list";
    }
}
