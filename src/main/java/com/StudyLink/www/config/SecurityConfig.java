/* SecurityConfig */

package com.StudyLink.www.config;

import com.StudyLink.www.handler.RoleBasedLoginSuccessHandler;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.CustomOAuth2UserService;
import com.StudyLink.www.service.CustomOidcUserService;
import com.StudyLink.www.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final UserRepository userRepository;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    /*
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    */

    @Bean
    public PasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder encoder =
                (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();

        // ✅ DB에 "$2a$..."처럼 prefix 없는 bcrypt가 들어있어도 매칭되게
        encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        return encoder;
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
                                /* "/api/auth/**", */
                                "/loginProc",
                                "/logout",
                                "/oauth2/**",
                                "/ws/**",
                                "/chatbot/**",
                                "/api/chatbot/archive/**",
                                "/room/**",
                                "/payment/**",
                                "/map/**",
                                "/api/cover-letter/**",
                                "/api/fcm/**",
                                "/api/account/**",
                                "/api/settings/**",
                                "/api/profile/**",
                                "/api/auth/**"
                        )
                )

                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.GET, "/api/account/ping").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers("/mentor/edit-profile").authenticated()
                        .requestMatchers("/mentor/**").authenticated()
                        .requestMatchers("/api/mentor-profiles/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/comment/list/**").permitAll()
                        .requestMatchers("/comment/post", "/comment/modify", "/comment/remove/**").authenticated()

                        .requestMatchers("/error", "/error/**").permitAll()

                        .requestMatchers("/board/register", "/board/register/**")
                        .hasAnyRole("MENTOR", "ADMIN")
                        .requestMatchers("/community/register", "/community/register/**")
                        .hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers("/room/myQuiz").authenticated()
                        .requestMatchers("/mentor/firebase-config").authenticated()

                        /* ===================== ✅ Inquiry 규칙 추가 ===================== */
                        .requestMatchers(HttpMethod.GET, "/inquiry/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/inquiry/register").authenticated()
                        .requestMatchers(HttpMethod.POST, "/inquiry/password/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/inquiry/answer").hasRole("ADMIN")
                        /* ============================================================ */

                        .requestMatchers(
                                "/",
                                "/index",
                                "/cover-letter",
                                "/cover_letter",

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
                                "/uploads/**",

                                /* "/api/auth/**", */
                                "/api/fcm/**",

                                "/room/list",
                                "/room/enterRoom",
                                "/ws/**",

                                "/payment/**",

                                "/board/**",
                                "/community/**",
                                "/.well-known/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/chatbot/**",
                                "/map/**",

                                "/auth/student-verification/verify",
                                "/firebase-messaging-sw.js",
                                "/manifest.webmanifest",
                                "/pwa-192x192.png",
                                "/pwa-512x512.png"
                        ).permitAll()

                        .requestMatchers(
                                "/auth/student-verification",
                                "/auth/student-verification/check-email",
                                "/auth/student-verification/request-verification",
                                "/auth/student-verification/status",
                                "/auth/student-verification/reset-token"
                        ).authenticated()

                        .requestMatchers("/my-page", "/my-page/**").authenticated()
                        .requestMatchers("/api/profile/**", "/api/account/**", "/api/settings/**").authenticated()

                        .anyRequest().access((auth, context) -> {
                            String ip = context.getRequest().getRemoteAddr();
                            boolean allowed = ip.startsWith("192.168.11.")
                                    || ip.equals("127.0.0.1")
                                    || ip.equals("0:0:0:0:0:0:0:1")
                                    || ip.equals("::1");
                            return new AuthorizationDecision(allowed);
                        })
                )

                .exceptionHandling(e -> e
                        .accessDeniedPage("/error")
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(new RoleBasedLoginSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(new RoleBasedLoginSuccessHandler())
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
