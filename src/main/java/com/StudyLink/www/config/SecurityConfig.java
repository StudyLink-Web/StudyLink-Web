package com.StudyLink.www.config;

import com.StudyLink.www.service.CustomUserDetailsService;
import com.StudyLink.www.service.OAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableWebSecurity
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
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/auth/**",
                                "/loginProc",
                                "/logout",
                                "/oauth2/**",
                                "/ws/**",
                                "/chatbot/**",
                                "/api/chatbot/archive/**",
                                "/room/**",
                                "/map/**"
                        )
                )

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
                                "/",
                                "/index",
                                "/login",
                                "/signup",
                                "/error",
                                "/loginProc",
                                "/logout",

                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/images/**",
                                "/static/**",
                                "/static.dist/**",

                                "/api/**",
                                "/api/auth/**",

                                "/room/list",
                                "/room/enterRoom",
                                "/ws/**",

                                // ✅ board 전체 공개(단, register는 위에서 예외로 막음)
                                "/board/**",

                                "/.well-known/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/chatbot/**",
                                "/map/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // ✅ 권한(403) 처리: /error 로 보내서 CustomErrorController가 403.html로 분기
                .exceptionHandling(e -> e
                        .accessDeniedPage("/error")
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            response.sendRedirect("/");
                        })
                        .failureUrl("/login?error=true")
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)
                                .expiredUrl("/login?expired=true")
                        )
                );

        return http.build();
    }
}
