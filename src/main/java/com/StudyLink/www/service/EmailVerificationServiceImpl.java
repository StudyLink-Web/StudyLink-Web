package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 대학생 이메일 인증 서비스 구현
 * 학교 이메일로 인증 링크를 발송하고 검증합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${mail.from.name:StudyLink}")
    private String fromName;

    @Value("${verification.token.expiry.hours:24}")
    private long tokenExpiryHours;

    @Value("${server.url:http://localhost:8088}")
    private String serverUrl;

    /**
     * 대학생 인증 이메일 발송
     */
    @Override
    public String sendVerificationEmail(String studentEmail, Users user) throws Exception {
        // 1️⃣ 이메일 중복 확인
        if (userRepository.existsByStudentEmail(studentEmail)) {
            throw new IllegalArgumentException("이미 인증된 이메일입니다.");
        }

        // 2️⃣ 인증 토큰 생성
        String token = generateVerificationToken();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(tokenExpiryHours);

        // 3️⃣ Users 테이블에 토큰 저장
        user.setStudentEmail(studentEmail);
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(expiryTime);
        userRepository.save(user);

        // 4️⃣ 인증 이메일 발송
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(studentEmail);
            message.setSubject("[StudyLink] 대학생 인증 확인 이메일");

            // 인증 링크 생성
            String verificationLink = generateVerificationLink(studentEmail, token);

            // 이메일 본문
            String emailBody = """
                안녕하세요! StudyLink입니다.

                대학생 인증을 완료하려면 아래 링크를 클릭하세요.
                (링크는 24시간 동안 유효합니다)

                🔗 인증 링크: %s

                감사합니다,
                StudyLink 팀
                """.formatted(verificationLink);

            message.setText(emailBody);
            mailSender.send(message);

            log.info("✅ 인증 이메일 발송 완료: {}", studentEmail);

        } catch (Exception e) {
            log.error("❌ 이메일 발송 실패: {}", studentEmail, e);
            // 실패 시 토큰 삭제
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            user.setStudentEmail(null);
            userRepository.save(user);
            throw e;
        }

        return token;
    }

    /**
     * 인증 링크 생성
     */
    @Override
    public String generateVerificationLink(String studentEmail, String token) {
        return String.format(
                "%s/mypage/verify?email=%s&token=%s",
                serverUrl,
                studentEmail,
                token
        );
    }

    /**
     * 인증 토큰 생성 (UUID 기반)
     */
    @Override
    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * 토큰 만료 여부 확인
     */
    @Override
    public boolean isTokenExpired(LocalDateTime expiryTime) {
        if (expiryTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * 인증 토큰과 이메일로 사용자 조회
     */
    @Override
    public Optional<Users> findByEmailAndToken(String studentEmail, String token) {
        return userRepository.findByStudentEmailAndVerificationToken(studentEmail, token);
    }
}