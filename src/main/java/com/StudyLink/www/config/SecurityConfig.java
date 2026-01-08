package com.StudyLink.www.config;

import com.StudyLink.www.service.CustomUserDetailsService;

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

/**
 * Spring Security 설정
 * - 일반 로그인 (Username/Password)
 * - OAuth2 소셜 로그인 (카카오, 네이버, 구글)
 * - 사용자 검증
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 매니저 설정
     * UserDetailsService를 사용해 사용자 검증
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    /**
     * 보안 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ⭐ CSRF 설정: 로그인/회원가입은 허용, 나머지는 보호
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/login", "/signup",
                                "/api/auth/**",
                                "/loginProc", "/logout"
                        )
                )

                // 권한 설정
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/signup",
                                "/loginProc",
                                "/logout",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/api/auth/**",
                                "/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // ⭐ Form Login 설정 (매우 중요!)
                .formLogin(form -> form
                        .loginPage("/login")                    // 로그인 페이지 경로
                        .loginProcessingUrl("/loginProc")       // 폼 제출 처리 경로
                        .usernameParameter("email")             // 이메일 필드명
                        .passwordParameter("password")          // 비밀번호 필드명
                        .defaultSuccessUrl("/", false)          // 성공 시 홈으로
                        .failureUrl("/login?error=true")        // 실패 시 에러 메시지
                        .permitAll()
                )

                // OAuth2 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
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
}