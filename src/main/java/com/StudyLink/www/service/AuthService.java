package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     * Users 엔티티: email, password, name, nickname, role 필드만 사용
     */
    @Transactional
    public Users signup(String email, String password, String name,
                        String nickname, String role) {

        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 닉네임 중복 확인
        if (nickname != null && userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 역할 검증
        if (!role.equals("STUDENT") && !role.equals("MENTOR")) {
            throw new IllegalArgumentException("역할은 STUDENT 또는 MENTOR여야 합니다.");
        }

        Users user = Users.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .nickname(nickname)
                .role(role)
                .build();

        return userRepository.save(user);
    }

    /**
     * 로그인
     * email과 password로 사용자 확인
     */
    @Transactional(readOnly = true)
    public Users login(String email, String password) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return true;
        }
        return !userRepository.existsByNickname(nickname);
    }

    /**
     * 이메일로 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * user_id로 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<Users> getUserById(Long user_id) {
        return userRepository.findById(user_id);
    }
}