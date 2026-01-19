package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    @Transactional
    public Users signup(String email, String password, String name, String nickname, String role) {
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null) {
            log.error("❌ PasswordEncoder를 찾을 수 없습니다");
            throw new RuntimeException("PasswordEncoder 설정 오류");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        validateSignupInput(email, password, name, nickname, role);

        String encodedPassword = passwordEncoder.encode(password);

        Users user = Users.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .nickname(nickname)
                .username(nickname)
                .role(role)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Users savedUser = userRepository.save(user);
        log.info("✅ 회원가입 완료: {}", email);

        return savedUser;
    }

    @Transactional(readOnly = true)
    public Users login(String email, String password) {
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null) {
            log.error("❌ PasswordEncoder를 찾을 수 없습니다");
            throw new RuntimeException("PasswordEncoder 설정 오류");
        }

        Optional<Users> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            log.warn("❌ 로그인 실패: 존재하지 않는 사용자 - {}", email);
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        Users user = optionalUser.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("❌ 로그인 실패: 비밀번호 불일치 - {}", email);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("✅ 로그인 성공: {}", email);
        return user;
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    @Transactional(readOnly = true)
    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Users> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Users findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("식별자가 비어있습니다.");
        }

        return userRepository.findByNickname(identifier)
                .orElseGet(() -> userRepository.findByEmail(identifier)
                        .orElseGet(() -> userRepository.findByUsername(identifier)
                                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + identifier))));
    }

    @Transactional(readOnly = true)
    public Long findUserIdByIdentifier(String identifier) {
        return findByIdentifier(identifier).getUserId();
    }

    @Transactional
    public Users updateUser(Long userId, String name, String nickname) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.getNickname().equals(nickname) &&
                userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        user.setName(name);
        user.setNickname(nickname);
        user.setUsername(nickname);
        user.setUpdatedAt(LocalDateTime.now());

        log.info("✅ 사용자 정보 업데이트: {}", userId);
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null) {
            log.error("❌ PasswordEncoder를 찾을 수 없습니다");
            throw new RuntimeException("PasswordEncoder 설정 오류");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 비밀번호 변경 완료: {}", userId);
    }

    @Transactional
    public void deleteUser(Long userId, String password) {
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null) {
            log.error("❌ PasswordEncoder를 찾을 수 없습니다");
            throw new RuntimeException("PasswordEncoder 설정 오류");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
        log.info("✅ 사용자 계정 삭제: {}", userId);
    }

    private void validateSignupInput(String email, String password, String name, String nickname, String role) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일을 입력하세요.");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("올바른 이메일 형식을 입력하세요.");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름을 입력하세요.");
        }

        if (nickname == null || nickname.length() < 2) {
            throw new IllegalArgumentException("닉네임은 2자 이상이어야 합니다.");
        }

        if (role == null || role.isEmpty()) {
            throw new IllegalArgumentException("역할을 선택하세요.");
        }
        if (!role.equals("STUDENT") && !role.equals("MENTOR")) {
            throw new IllegalArgumentException("역할은 STUDENT 또는 MENTOR이어야 합니다.");
        }
    }
}
