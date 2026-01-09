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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

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
                                "/.well-known/**",      // ✅ Chrome DevTools 에러 무시
                                "/chatbot/**",
                                "/oauth2/**"
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

                // FIXME: 설정 파일(application.properties 등)에 소셜 로그인 클라이언트 정보(ID/Secret)가 없어 임시 주석 처리함. 
                // 소셜 로그인 기능을 구현하려면 설정 추가 후 아래 주석을 해제
                /*
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                )
                */

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
