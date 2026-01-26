package com.StudyLink.www.service;

import com.StudyLink.www.entity.Role;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.entity.EmailVerificationToken;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.repository.EmailVerificationTokenRepository;
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
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    // 필드 주입에서 ObjectProvider로 변경 (순환 참조 방지)
    @Autowired
    private ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    // 생성자 제거 - 필드 주입 사용으로 변경됨
    //public AuthService(PasswordEncoder passwordEncoder) {
    //    this.passwordEncoder = passwordEncoder;


    /**
     * 회원가입
     */
    @Transactional
    public Users signup(String email, String password, String name, String nickname, String role) {
        // ObjectProvider에서 PasswordEncoder 가져오기
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null) {
            log.error("❌ PasswordEncoder를 찾을 수 없습니다");
            throw new RuntimeException("PasswordEncoder 설정 오류");
        }

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

        // 5. 역할 설정 (기본값: STUDENT)
        Role userRole = Role.STUDENT;
        if (role != null && !role.isEmpty()) {
            userRole = Role.fromString(role);  // String을 Enum으로 변환
            log.info("✅ 역할 설정: {}", userRole);
        } else {
            log.info("✅ 역할 자동 설정: STUDENT");
        }

        // 6. 사용자 생성
        Users user = Users.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .nickname(nickname)
                .username(nickname)  // ✅ username을 nickname과 동일하게 설정
                .role(userRole)  // Enum으로 설정
                .emailVerified(false)  // ✅ 이메일 미인증 상태로 시작
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 7. 데이터베이스에 저장
        Users savedUser = userRepository.save(user);
        log.info("✅ 회원가입 완료: {}", email);

        return savedUser;
    }

    /**
     * 로그인
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return 로그인된 사용자 정보
     * @throws IllegalArgumentException 로그인 실패 시
     */
    @Transactional(readOnly = true)
    public Users login(String email, String password) {
        // ObjectProvider에서 PasswordEncoder 가져오기
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null) {
            log.error("❌ PasswordEncoder를 찾을 수 없습니다");
            throw new RuntimeException("PasswordEncoder 설정 오류");
        }

        // 1. 이메일로 사용자 조회
        Optional<Users> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            log.warn("❌ 로그인 실패: 존재하지 않는 사용자 - {}", email);
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        Users user = optionalUser.get();

        // OAuth 사용자는 비밀번호 검증 스킵
        if (user.getOauthProvider() != null && !user.getOauthProvider().isEmpty()) {
            log.info("✅ OAuth 사용자 로그인: {} ({})", email, user.getOauthProvider());
            return user;
        }

        // 2. 비밀번호 검증 (BCrypt로 암호화된 비밀번호와 비교)
        // OAuth가 아닌 일반 사용자만 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("❌ 로그인 실패: 비밀번호 불일치 - {}", email);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("✅ 로그인 성공: {}", email);
        return user;
    }


    /**
     * 이메일 중복 확인
     * @param email 확인할 이메일
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    /**
     * 닉네임 중복 확인
     * @param nickname 확인할 닉네임
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     */
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    /**
     * 이메일로 사용자 조회
     * @param email 사용자 이메일
     * @return Optional 사용자 정보
     */
    @Transactional(readOnly = true)
    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * ID로 사용자 조회
     * @param userId 사용자 ID
     * @return Optional 사용자 정보
     */
    @Transactional(readOnly = true)
    public Optional<Users> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 사용자 정보 업데이트
     * @param userId 사용자 ID
     * @param name 새로운 이름
     * @param nickname 새로운 닉네임
     * @return 업데이트된 사용자 정보
     */
    @Transactional
    public Users updateUser(Long userId, String name, String nickname) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 닉네임 중복 확인 (현재 사용자의 닉네임이 아닌 경우만)
        if (!user.getNickname().equals(nickname) &&
                userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        user.setName(name);
        user.setNickname(nickname);
        user.setUsername(nickname);  // username도 함께 업데이트
        user.setUpdatedAt(LocalDateTime.now());

        log.info("✅ 사용자 정보 업데이트: {}", userId);
        return userRepository.save(user);
    }

    /**
     * 비밀번호 변경
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새로운 비밀번호
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        // ObjectProvider에서 PasswordEncoder 가져오기
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null) {
            log.error("❌ PasswordEncoder를 찾을 수 없습니다");
            throw new RuntimeException("PasswordEncoder 설정 오류");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 검증
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }

        // 새 비밀번호 저장
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 비밀번호 변경 완료: {}", userId);
    }

    /**
     * 사용자 삭제 (계정 탈퇴)
     * @param userId 사용자 ID
     * @param password 비밀번호 확인
     */
    @Transactional
    public void deleteUser(Long userId, String password) {
        // ObjectProvider에서 PasswordEncoder 가져오기
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null) {
            log.error("❌ PasswordEncoder를 찾을 수 없습니다");
            throw new RuntimeException("PasswordEncoder 설정 오류");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
        log.info("✅ 사용자 계정 삭제: {}", userId);
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

        // Enum 검증 추가
        try {
            Role.fromString(role);  // 유효한 Role인지 확인
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("역할은 STUDENT 또는 MENTOR이어야 합니다.");
        }
    }

    /**
     * 이메일 인증 코드 발송
     * @param email 인증받을 이메일
     * @param requestedUsername 요청한 계정명
     */
    @Transactional
    public void sendVerificationEmail(String email, String requestedUsername) {
        log.info("📧 이메일 인증 코드 발송: {} (요청자: {})", email, requestedUsername);

        // 1️⃣ 이미 인증된 이메일 확인
        if (userRepository.findByEmail(email).isPresent()) {
            Users existingUser = userRepository.findByEmail(email).get();
            if (existingUser.getEmailVerified() != null && existingUser.getEmailVerified()) {
                throw new IllegalArgumentException("이미 가입된 이메일입니다");
            }
        }

        // 2️⃣ 기존 토큰 삭제 (같은 계정의 이전 요청만 삭제)
        emailVerificationTokenRepository.deleteByRequestedUsername(requestedUsername);
        log.info("✅ 기존 토큰 삭제 (계정: {})", requestedUsername);

        // 3️⃣ 인증 코드 생성 (6자리 숫자)
        String verificationCode = String.format("%06d", (int)(Math.random() * 1000000));
        log.info("✅ 생성된 인증 코드: {}", verificationCode);

        // 4️⃣ DB에 저장 (이메일 + 요청자 정보)
        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(email)
                .requestedUsername(requestedUsername)
                .verificationCode(verificationCode)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .build();

        emailVerificationTokenRepository.save(token);
        log.info("✅ DB에 저장: email={}, requestedUsername={}", email, requestedUsername);

        // 5️⃣ 이메일 발송 (MailService 필요)
        // mailService.sendVerificationEmail(email, verificationCode, requestedUsername);

        log.info("✅ 인증 이메일 발송 완료: {}", email);
    }

    /**
     * 이메일 인증 코드 확인
     * @param email 받은 이메일
     * @param code 입력한 코드
     * @param requestedUsername 요청한 계정
     * @return 인증 성공 여부
     */
    @Transactional
    public boolean verifyEmail(String email, String code, String requestedUsername) {
        log.info("🔍 이메일 인증 코드 확인: email={}, requestedUsername={}", email, requestedUsername);

        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("인증 코드를 입력해주세요");
        }

        // ❌ 이미 다른 계정이 인증한 이메일인지 확인!
        Optional<Users> existingVerifiedUser = userRepository.findByEmail(email);
        if (existingVerifiedUser.isPresent()) {
            Users verifiedUser = existingVerifiedUser.get();
            if (verifiedUser.getEmailVerified() != null &&
                    verifiedUser.getEmailVerified() &&
                    !verifiedUser.getUsername().equals(requestedUsername)) {
                log.warn("❌ 이미 다른 계정에서 인증된 이메일: email={}", email);
                throw new IllegalArgumentException("이메일이 이미 다른 계정에서 인증되었습니다");
            }
        }

        // DB에서 토큰 조회 (이메일 + 요청자 모두 확인!)
        EmailVerificationToken token = emailVerificationTokenRepository
                .findByEmailAndRequestedUsername(email, requestedUsername)
                .orElseThrow(() -> {
                    log.warn("❌ 저장된 인증 토큰이 없음: email={}, requestedUsername={}",
                            email, requestedUsername);
                    return new IllegalArgumentException(
                            "인증 코드가 만료되었거나 일치하는 요청이 없습니다. 다시 요청해주세요");
                });

        // 만료 확인
        if (token.isExpired()) {
            emailVerificationTokenRepository.delete(token);
            log.warn("❌ 인증 토큰 만료: email={}", email);
            throw new IllegalArgumentException("인증 코드가 만료되었습니다. 다시 요청해주세요");
        }

        // 코드 확인
        if (!token.getVerificationCode().equals(code)) {
            log.warn("❌ 인증 코드 불일치: email={}", email);
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다");
        }

        // ✅ 코드 확인 후 즉시 삭제
        emailVerificationTokenRepository.delete(token);
        log.info("✅ 인증 코드 확인 성공! email={}, requestedUsername={}", email, requestedUsername);

        return true;
    }

    /**
     * 사용자 이메일 인증 상태 업데이트
     * @param email 인증할 이메일
     * @param requestedUsername 요청한 계정
     */
    @Transactional
    public void markEmailAsVerified(String email, String requestedUsername) {
        log.info("📧 이메일 인증 완료 처리: email={}, requestedUsername={}", email, requestedUsername);

        Users user = userRepository.findByUsername(requestedUsername)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // ✅ 해당 이메일이 이미 다른 사람에게 인증되었는지 확인
        Optional<Users> existingVerifiedUser = userRepository.findByEmail(email);
        if (existingVerifiedUser.isPresent()) {
            Users verifiedUser = existingVerifiedUser.get();
            if (verifiedUser.getEmailVerified() != null &&
                    verifiedUser.getEmailVerified() &&
                    !verifiedUser.getUserId().equals(user.getUserId())) {
                log.error("❌ 이메일이 이미 다른 계정에 인증됨: email={}", email);
                throw new IllegalArgumentException("이메일이 이미 다른 계정에서 인증되었습니다");
            }
        }

        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 이메일 인증 완료: email={}, userId={}", email, user.getUserId());
    }
}
