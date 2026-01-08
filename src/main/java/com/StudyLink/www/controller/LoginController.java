package com.StudyLink.www.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.StudyLink.www.service.AuthService;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 및 회원가입 페이지 컨트롤러
 * 사용자 인증 관련 뷰(HTML) 페이지 처리
 *
 * API는 AuthController에서 처리
 * 이 컨트롤러는 페이지 렌더링만 담당
 */
@Controller
@Slf4j
public class LoginController {

    @Autowired
    private AuthService authService;

    /**
     * 로그인 페이지 표시
     * GET /login
     *
     * 이미 로그인된 사용자가 접근 시 홈으로 리다이렉트
     * 비로그인 사용자가 접근 시 로그인 페이지 표시
     * 에러 파라미터 감지 시 모델에 추가
     *
     * @param authentication 현재 사용자 인증 정보
     * @param error 로그인 실패 여부 파라미터
     * @param expired 세션 만료 여부 파라미터
     * @param model 뷰로 전달할 데이터
     * @return 로그인 페이지 또는 홈으로 리다이렉트
     */
    @GetMapping("/login")
    public String login(Authentication authentication,
                        @RequestParam(required = false) String error,
                        @RequestParam(required = false) String expired,
                        Model model) {

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (expired != null) {
            model.addAttribute("expired", "세션이 만료되었습니다. 다시 로그인해주세요.");
        }

        return "login/login";
    }


    /**
     * 회원가입 페이지 표시
     * GET /signup
     *
     * 이미 로그인된 사용자가 접근 시 홈으로 리다이렉트
     * 비로그인 사용자가 접근 시 회원가입 페이지 표시
     *
     * @param authentication 현재 사용자 인증 정보
     * @return 회원가입 페이지 또는 홈으로 리다이렉트
     */
    @GetMapping("/signup")
    public String signup(Authentication authentication) {

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        return "signup/signup";
    }


    // ========== 참고: API 엔드포인트 ==========
    //
    // 로그인/회원가입 API는 AuthController에서 처리합니다:
    //
    // POST /api/auth/login - 로그인 API
    // POST /api/auth/signup - 회원가입 API
    // POST /api/auth/check-email - 이메일 중복 확인 API
    // POST /api/auth/check-nickname - 닉네임 중복 확인 API
    //
    // ⭐ 해당 API들은 JSON 요청/응답을 처리합니다
    // ⭐ JavaScript에서 fetch() 또는 AJAX로 호출하면 됩니다
}
