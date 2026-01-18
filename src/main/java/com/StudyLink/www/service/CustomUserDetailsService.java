package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // ⭐ 수정: username을 먼저 조회 (OAuth2 친화적)
        Users user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByNickname(username)
                        .orElseGet(() -> userRepository.findByEmail(username)
                                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username))));

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String role = (user.getRole() != null && !user.getRole().trim().isEmpty()) ? user.getRole() : "ROLE_USER";
        authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));

        log.info("✅ 사용자 로드: {} (name: {}) - Role: {}", user.getUsername(), user.getName(), role);

        // ✅ 여기서 Security 인증ID를 "username"이 아니라 "nickname"으로 맞추고 싶으면 아래 한 줄만 바꾸면 됨
        // .username(user.getNickname())
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
