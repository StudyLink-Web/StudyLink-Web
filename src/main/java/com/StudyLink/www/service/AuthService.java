package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public Users signup(String email, String password, String name, String nickname, String role) {
        // 1. 이메일 중복 확인
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 닉네임 중복 확인
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 3. 입력값 검증
        validateSignupInput(email, password, name, nickname, role);

        // 4. 비밀번호 해싱
        String encodedPassword = passwordEncoder.encode(password);

        // 5. 사용자 생성
        Users user = Users.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .nickname(nickname)
                .username(nickname)  // ✅ username을 nickname과 동일하게 설정
                .role(role)
                .emailVerified(false)  // ✅ 이메일 미인증 상태로 시작
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 6. 데이터베이스에 저장
        Users savedUser = userRepository.save(user);
        log.info("회원가입 완료: {}", email);

        return savedUser;
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public Users login(String email, String password) {
        // 1. 이메일로 사용자 조회
        Optional<Users> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        Users user = optionalUser.get();

        // 2. 비밀번호 검증 (BCrypt로 암호화된 비밀번호와 비교)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("로그인 성공: {}", email);
        return user;
    }

    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    /**
     * 닉네임 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    /**
     * 이메일로 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * ID로 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<Users> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 회원가입 입력값 검증
     */
    private void validateSignupInput(String email, String password, String name, String nickname, String role) {
        // 이메일 검증
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일을 입력하세요.");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("올바른 이메일 형식을 입력하세요.");
        }

        // 비밀번호 검증
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        // 이름 검증
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름을 입력하세요.");
        }

        // 닉네임 검증
        if (nickname == null || nickname.length() < 2) {
            throw new IllegalArgumentException("닉네임은 2자 이상이어야 합니다.");
        }

        // 역할 검증
        if (role == null || role.isEmpty()) {
            throw new IllegalArgumentException("역할을 선택하세요.");
        }
        if (!role.equals("STUDENT") && !role.equals("MENTOR")) {
            throw new IllegalArgumentException("역할은 STUDENT 또는 MENTOR이어야 합니다.");
        }
    }
}
