package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 사용자 상세 정보 서비스
 * 로그인 시 사용자 검증 (데이터베이스 연동)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 데이터베이스에서 사용자 조회
        // username 또는 email로 조회
        Users user = userRepository.findByNickname(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "사용자를 찾을 수 없습니다: " + username)));

        // 권한 설정
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String role = user.getRole() != null ? user.getRole() : "STUDENT";
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        System.out.println("✅ 사용자 로드: " + user.getUsername() + " (" + user.getEmail() + ") - Role: " + role);

        // Spring Security UserDetails 반환
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