package com.StudyLink.www.config;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

/**
 * 모든 Controller에서 사용할 수 있는 공통 Model 속성 설정
 */
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class WebControllerAdvice {

    private final UserRepository userRepository;

    /**
     * 모든 뷰에 사용자 Role 추가
     */
    @ModelAttribute("userRole")
    public String getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("🔐 [WebControllerAdvice] Authentication: {}", authentication);

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            log.warn("⚠️ [WebControllerAdvice] 기본값 GUEST 반환");
            return "GUEST";
        }

        String username = extractUsername(authentication);

        if (username == null) {
            log.warn("⚠️ [WebControllerAdvice] username 추출 실패 → GUEST");
            return "GUEST";
        }

        Optional<Users> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            String role = userOpt.get().getRole().name();
            log.info("✅ [WebControllerAdvice] 사용자 Role: {}", role);
            return role;
        }

        log.warn("⚠️ [WebControllerAdvice] 사용자 없음 → GUEST");
        return "GUEST";
    }

    /**
     * 모든 뷰에 사용자 이름 추가
     */
    @ModelAttribute("userName")
    public String getUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return "Guest";
        }

        String username = extractUsername(authentication);

        if (username == null) {
            return "Guest";
        }

        return userRepository.findByUsername(username)
                .map(user -> user.getName() != null ? user.getName() : user.getNickname())
                .orElse("Guest");
    }

    /**
     * Authentication 객체에서 username 통합 추출
     */
    private String extractUsername(Authentication authentication) {

        Object principal = authentication.getPrincipal();

        // ✅ OAuth2 / OIDC 로그인
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getAttribute("username");
        }

        if (principal instanceof OAuth2User oauth2User) {
            Object username = oauth2User.getAttributes().get("username");
            return username != null ? username.toString() : null;
        }

        // ✅ 로컬 로그인
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return null;
    }
}
