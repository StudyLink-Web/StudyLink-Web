package com.StudyLink.www.config;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.CustomOAuth2UserService;
import com.StudyLink.www.service.CustomUserDetailsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableWebSecurity
// @RequiredArgsConstructor

// 클래스 정의에 추가
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;  // ✅ 새 서비스 주입

    @Autowired
    private UserRepository userRepository;  // ⭐ 추가!

    @Autowired
    private ObjectProvider<PasswordEncoder> passwordEncoderProvider;  // ⭐ 추가!

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ CSRF 설정: REST API와 폼 로그인 모두 지원
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/auth/**",          // REST API는 CSRF 토큰 필요 없음
                                "/loginProc",             // 폼 기반 로그인
                                "/logout",
                                "/oauth2/**",             // OAuth2 요청도 CSRF 제외
                                "/logout",
                                "/ws/**",
                                "/chatbot/**",            // 챗봇 관련 요청 허용
                                "/api/chatbot/archive/**", // 추가: 챗봇 아카이브 API CSRF 제외
                                "/room/**",               // 방 관련 요청 허용
                                "/map/**"                 // 추가: 지도 관련 요청 CSRF 제외
                        )
                )

                // 권한 설정
                .authorizeHttpRequests(authz -> authz
                        // ✅ 댓글 목록: 비로그인 허용
                        .requestMatchers(HttpMethod.GET, "/comment/list/**").permitAll()

                        // ✅ 댓글 작성/수정/삭제: 로그인 필요
                        .requestMatchers("/comment/post", "/comment/modify", "/comment/remove/**").authenticated()

                        // ✅ 에러 페이지는 누구나 접근 가능 (CustomErrorController가 /error 에서 분기함)
                        .requestMatchers("/error", "/error/**").permitAll()

                        // ✅ 등록(폼/처리): MENTOR만 허용 ( /board/** permitAll 보다 위에 있어야 함 )
                        .requestMatchers("/board/register", "/board/register/**").hasRole("MENTOR")

                        // 나의 질문, 답변 내역 비로그인시 접근 불가
                        .requestMatchers("/room/myQuiz").authenticated()

                        .requestMatchers(
                                // ✅ 홈페이지는 누구나 접근 가능
                                "/",
                                "/index",

                                // ✅ 로그인 관련
                                "/login",
                                "/signup",
                                "/error",

                                "/loginProc",
                                "/logout",

                                // ✅ 정적 리소스 (CSS, JS, 이미지)
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/images/**",
                                "/static/**",
                                "/static.dist/**",

                                // ✅ API는 모두 공개 (테스트용)
                                "/api/**",
                                "/api/auth/**",

                                "/room/list",
                                "/room/enterRoom",
                                "/ws/**",

                                // ✅ board 전체 공개(단, register는 위에서 예외로 막음)
                                "/board/**",

                                "/api/auth/**",
                                "/.well-known/**",      // Chrome DevTools 에러 무시
                                "/oauth2/**",           // OAuth2 요청
                                "/login/oauth2/**",      // OAuth2 리다이렉트 URI
                                "/.well-known/**",      // ✅ Chrome DevTools 에러 무시
                                "/chatbot/**",
                                "/map/**",              // 추가: 지도 관련 요청 허용
                                "/auth/student-verification/verify"  // ⭐ 추가: 이메일 인증 링크는 로그인 불필요 (토큰으로 인증)
                        ).permitAll()

                        // ⭐ 학교 이메일 인증 페이지는 로그인 필수
                        .requestMatchers("/auth/student-verification", "/auth/student-verification/check-email", "/auth/student-verification/request-verification", "/auth/student-verification/status", "/auth/student-verification/reset-token").authenticated()

                                // 마이페이지는 인증된 사용자만 접근
                                .requestMatchers("/my-page", "/my-page/**").authenticated()

                                // 마이페이지 API는 인증된 사용자만 접근
                                .requestMatchers("/api/profile/**", "/api/account/**", "/api/settings/**").authenticated()

                                .anyRequest().authenticated()

                )

                // ✅ 권한(403) 처리: /error 로 보내서 CustomErrorController가 403.html로 분기
                .exceptionHandling(e -> e
                        .accessDeniedPage("/error")
                )

                // ✅ Form Login (폼 기반 로그인)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)  // ← true로 변경 (or 커스텀 핸들러 사용)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // OAuth2 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            try {
                                log.info("════════════════════════════════════════════════════════════");
                                log.info("✅ OAuth2 로그인 성공!");
                                log.info("🔍 authentication.getName(): {}", authentication.getName());

                                // ⭐ Google OIDC 사용자 정보 추출
                                var principal = authentication.getPrincipal();
                                Map<String, Object> attributes = null;

                                if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser) {
                                    org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser =
                                            (org.springframework.security.oauth2.core.oidc.user.OidcUser) principal;
                                    attributes = new HashMap<>(oidcUser.getAttributes());
                                    log.info("🔐 OIDC 사용자 감지 - Google 처리");

                                    String sub = oidcUser.getSubject();
                                    String email = (String) attributes.getOrDefault("email", "");
                                    String name = (String) attributes.getOrDefault("name", "구글사용자");
                                    String picture = (String) attributes.getOrDefault("picture", "");

                                    if (email == null || email.isEmpty()) {
                                        email = "google_" + sub + "@google.com";
                                    }

                                    String fixedUsername = "google_" + sub;
                                    String fixedNickname = "Google_" + sub;

                                    log.info("✅ Google OIDC 사용자: name={}, email={}", name, email);
                                    saveGoogleUser(fixedUsername, email, picture, name, fixedNickname);
                                }

                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                response.sendRedirect("/");
                            } catch (Exception e) {
                                log.error("❌ OAuth2 successHandler 오류: {}", e.getMessage(), e);
                                response.sendRedirect("/login?error=true");
                            }
                        })
                        .failureUrl("/login?error=true")
                )
                // Logout 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // 세션 관리
                .sessionManagement(session -> session
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)
                                .expiredUrl("/login?expired=true")
                        )
                );

        return http.build();
    }

    private void saveGoogleUser(String username, String email, String picture, String name, String nickname) {
        try {
            log.info("🔐 Google 사용자 저장 시작: {}", username);

            // email이 null이면 생성
            if (email == null || email.isEmpty()) {
                email = username + "@oauth.com";
                log.warn("⚠️ email이 null - 임시 email 생성: {}", email);
            }

            Optional<Users> existingUser = userRepository.findByUsername(username);

            Users user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.setName(name);
                user.setNickname(nickname);
                user.setProfileImageUrl(picture);
                user.setOauthProvider("google");
                user.setOauthId(username);
                user.setEmail(email);
                log.info("🔄 기존 Google 사용자 업데이트");
            } else {
                PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();
                String encodedPassword = (encoder != null)
                        ? encoder.encode("oauth_google_" + System.currentTimeMillis())
                        : "oauth_google_" + System.currentTimeMillis();

                user = Users.builder()
                        .username(username)
                        .nickname(nickname)
                        .email(email)
                        .name(name)
                        .profileImageUrl(picture)
                        .oauthProvider("google")
                        .oauthId(username)
                        .password(encodedPassword)
                        .role("STUDENT")
                        .isActive(true)
                        .build();

                log.info("✅ 신규 Google 사용자 생성");
            }

            Users savedUser = userRepository.save(user);
            log.info("💾 Google 사용자 저장 완료: user_id={}, email={}", savedUser.getUserId(), email);

        } catch (Exception e) {
            log.error("❌ Google 사용자 저장 실패: {}", e.getMessage());
        }
    }

}
