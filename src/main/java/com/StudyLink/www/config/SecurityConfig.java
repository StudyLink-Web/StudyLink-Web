package com.StudyLink.www.config;

import com.StudyLink.www.service.CustomUserDetailsService;
import com.StudyLink.www.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// @RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private OAuth2UserService oAuth2UserService;

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
                                "/oauth2/**",             // ⭐ 추가: OAuth2 요청도 CSRF 제외
                                "/logout",
                                "/ws/**",
                                "/chatbot/**",            // 챗봇 관련 요청 허용
                                "/room/**"                // 방 관련 요청 허용
                        )
                )

                // 권한 설정
                .authorizeHttpRequests(authz -> authz
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

                                // ✅ quiz관련 모두 허용. 나중에 분리 - 김광주
                                "/room/**",
                                "/ws/**", // WebSocket 엔드포인트 허용

                                // ✅ board관련 모두 허용. 나중에 분리 - 김광주
                                "/board/**",

                                "/api/auth/**",
                                "/.well-known/**",      // Chrome DevTools 에러 무시
                                "/oauth2/**",           // OAuth2 요청
                                "/login/oauth2/**",      // OAuth2 리다이렉트 URI
                                "/.well-known/**",      // ✅ Chrome DevTools 에러 무시
                                "/chatbot/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // ✅ Form Login (폼 기반 로그인)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/loginProc")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // ⭐ OAuth2 설정
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(oAuth2UserService)
//                        )
//                        .defaultSuccessUrl("/", true)
//                        .failureUrl("/login?error=true")
//                )


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
}
