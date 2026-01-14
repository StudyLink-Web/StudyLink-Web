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
                        // ✅ 댓글 "목록 조회"는 비로그인도 허용
                        .requestMatchers(HttpMethod.GET, "/comment/list/**").permitAll()

                        // ✅ 댓글 작성/수정/삭제는 로그인 필요
                        .requestMatchers("/comment/post", "/comment/modify", "/comment/remove/**").authenticated()

                        // ✅ ✅ ✅ 등록(폼/처리) 접근은 ROLE_ADMIN만 허용 (원하면 ROLE_TEACHER 등 추가 가능)
                        // ⚠️ 중요: /board/** permitAll 보다 "위"에 있어야 우선 적용됨
                        .requestMatchers("/board/register", "/board/register/**").hasRole("MENTOR")

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

                                "/room/**",
                                "/ws/**",

                                // ✅ board 전체는 공개 유지 (단, 위에서 register는 예외로 막음)
                                "/board/**",

                                "/.well-known/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/chatbot/**",
                                "/map/**"
                        ).permitAll()
                        .anyRequest().authenticated()
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
