package com.StudyLink.www.controller;

import com.StudyLink.www.dto.InquiryDTO;
import com.StudyLink.www.handler.InquiryPageHandler;
import com.StudyLink.www.service.InquiryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    private boolean isLogin(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean isAdmin(Authentication authentication) {
        if (!isLogin(authentication)) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /* ===================== 목록 ===================== */
    @GetMapping("/list")
    public String list(@RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        var result = inquiryService.getList(pageNo);

        InquiryPageHandler<?> ph = new InquiryPageHandler<>(pageNo, result);
        ph.setType(type);
        ph.setKeyword(keyword);

        model.addAttribute("ph", ph);
        return "inquiry/list";
    }

    /* ===================== 등록 페이지 ===================== */
    @GetMapping("/register")
    public String register(Authentication authentication) {
        if (!isLogin(authentication)) return "redirect:/login";
        return "inquiry/register";
    }

    /* ===================== 등록 처리 ===================== */
    @PostMapping("/register")
    public String registerPost(@ModelAttribute InquiryDTO inquiryDTO,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (!isLogin(authentication)) return "redirect:/login";

        inquiryDTO.setStatus("PENDING"); // ✅ 등록 시 기본 상태
        inquiryService.register(inquiryDTO, authentication.getName());

        redirectAttributes.addFlashAttribute("msg", "문의가 등록되었습니다.");
        return "redirect:/inquiry/list";
    }

    /* ===================== 상세 ===================== */
    @GetMapping("/detail/{qno}")
    public String detail(@PathVariable Long qno,
                         Authentication authentication,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        InquiryDTO dto = inquiryService.getDetail(qno);
        if (dto == null) {
            redirectAttributes.addFlashAttribute("error", "존재하지 않는 문의입니다.");
            return "redirect:/inquiry/list";
        }

        if (isAdmin(authentication)) {
            model.addAttribute("inquiry", dto);
            return "inquiry/detail";
        }

        if ("N".equals(dto.getIsPublic())) {
            String key = "INQ_OK_" + qno;
            Boolean ok = (Boolean) session.getAttribute(key);
            if (ok == null || !ok) {
                return "redirect:/inquiry/password/" + qno;
            }
        }

        model.addAttribute("inquiry", dto);
        return "inquiry/detail";
    }

    /* ===================== 비밀번호 입력 페이지 ===================== */
    @GetMapping("/password/{qno}")
    public String passwordPage(@PathVariable Long qno, Model model) {
        model.addAttribute("qno", qno);
        return "inquiry/password";
    }

    /* ===================== 비밀번호 검증 (폼) ===================== */
    @PostMapping("/password/verify")
    public String verifyPassword(@RequestParam Long qno,
                                 @RequestParam String password,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        boolean ok = inquiryService.verifyPassword(qno, password);
        if (!ok) {
            redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "redirect:/inquiry/password/" + qno;
        }

        session.setAttribute("INQ_OK_" + qno, true);
        return "redirect:/inquiry/detail/" + qno;
    }

    /* ===================== 비밀번호 검증 (AJAX) ===================== */
    @PostMapping("/password/verify-ajax")
    @ResponseBody
    public Map<String, Object> verifyPasswordAjax(@RequestParam Long qno,
                                                  @RequestParam String password,
                                                  HttpSession session) {

        boolean ok = inquiryService.verifyPassword(qno, password);

        if (!ok) {
            return Map.of(
                    "ok", false,
                    "message", "비밀번호가 일치하지 않습니다."
            );
        }

        session.setAttribute("INQ_OK_" + qno, true);
        return Map.of("ok", true);
    }

    /* ===================== 답변 처리 (관리자) ===================== */
    @PostMapping("/answer")
    public String answer(@RequestParam Long qno,
                         @RequestParam String adminContent,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {

        if (!isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("error", "권한이 없습니다.");
            return "redirect:/inquiry/detail/" + qno;
        }

        inquiryService.answer(qno, adminContent);
        redirectAttributes.addFlashAttribute("msg", "답변이 등록되었습니다.");
        return "redirect:/inquiry/detail/" + qno;
    }
}
