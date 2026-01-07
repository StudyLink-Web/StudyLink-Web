package com.StudyLink.www.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // ✅ 홈페이지는 누구나 접근 가능
                        .requestMatchers("/").permitAll()

                        // ✅ 로그인 관련
                        .requestMatchers("/login", "/signup", "/error").permitAll()

                        // ✅ 정적 리소스 (CSS, JS, 이미지)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/img/**").permitAll()
                        .requestMatchers("/static/**", "/static.dist/**").permitAll()

                        // ✅ API는 모두 공개 (테스트용)
                        .requestMatchers("/api/**").permitAll()

                        // ✅ quiz관련 모두 허용. 나중에 분리 - 김광주
                        .requestMatchers("/room/**").permitAll()

                        // ✅ 나머지 페이지는 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}
