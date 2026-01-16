package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentVerificationService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    // application.properties에서 읽어오기
    @Value("${app.token.expiration-minutes:1}")  // 기본값: 24시간 (1440분) -> 테스트 1분 변경
    private int tokenExpirationMinutes;

    // 허용된 학교 도메인 목록 (계속 추가 가능)
    private static final String[] ALLOWED_DOMAINS = {
            "@snu.ac.kr",       // 서울대
            "@ewha.ac.kr",      // 이화여자대 (사용자 학교)
            "@yonsei.ac.kr",    // 연세대
            "@korea.ac.kr",     // 고려대
            "@cau.ac.kr",       // 중앙대
            "@khu.ac.kr",       // 경희대
            "@hongik.ac.kr",    // 홍익대
            "@kaist.ac.kr",     // KAIST
            "@postech.ac.kr",   // 포스텍
            "@sogang.ac.kr",    // 소강대
            "@hanyang.ac.kr",   // 한양대
            "@sejong.ac.kr",    // 세종대
            "@dankook.ac.kr",   // 단국대
            "@konkuk.ac.kr",    // 건국대
            "@chung-ang.ac.kr", // 중앙대 (alt)
            "@sookmyung.ac.kr", // 숙명여자대
            "@iseoul.ac.kr",    // 서울시립대
            "@kcu.ac.kr",       // 가톨릭대학교
            "@siswa.um.edu.my"  // 말라야대학교
    };

    /**
     * 현재 로그인한 사용자 정보 조회 (안전한 방식)
     * ⭐ 수정됨: 엔티티 캐스팅 대신 Repository로 조회
     */
    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("인증되지 않은 사용자입니다");
        }

        String username = auth.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
    }

    /**
     * 학교 이메일 중복 확인
     */
    public Map<String, Object> checkSchoolEmailAvailability(String email) {
        Map<String, Object> response = new HashMap<>();

        // 1. 이메일 형식 검증
        if (!email.contains("@")) {
            response.put("available", false);
            response.put("message", "올바른 이메일 형식이 아닙니다");
            response.put("code", "INVALID_FORMAT");
            return response;
        }

        // 2. 학교 도메인 검증
        if (!isValidSchoolEmail(email)) {
            response.put("available", false);
            response.put("message", "인정된 학교 이메일이 아닙니다");
            response.put("code", "INVALID_DOMAIN");
            return response;
        }

        // 3. DB 중복 확인
        Optional<Users> existingUser = userRepository.findBySchoolEmail(email);
        if (existingUser.isPresent()) {
            Users user = existingUser.get();

            // ⭐ 인증된 경우
            if (user.getIsVerifiedStudent()) {
                response.put("available", false);
                response.put("message", "이미 인증된 이메일입니다");
                response.put("code", "ALREADY_VERIFIED");
                log.warn("⚠️ 이미 인증된 이메일 사용 시도: {} (소유자: {})", email, user.getUsername());
                return response;
            }

            // ⭐ 인증 대기 중인 경우
            if (user.getSchoolEmailVerificationToken() != null) {
                response.put("available", false);
                response.put("message", "이미 인증 요청된 이메일입니다. 이메일을 확인하세요.");
                response.put("code", "VERIFICATION_PENDING");
                log.warn("⚠️ 인증 대기 중인 이메일 재요청: {} (사용자: {})", email, user.getUsername());
                return response;
            }
        }

        // ⭐ 사용 가능한 경우만 여기에 도달
        response.put("available", true);
        response.put("message", "사용 가능한 이메일입니다");
        response.put("code", "AVAILABLE");
        return response;
    }

    /**
     * 학교 이메일 인증 요청
     */
    public Map<String, Object> requestEmailVerification(String email) {
        Map<String, Object> response = new HashMap<>();

        // 1. 가용성 확인
        Map<String, Object> availabilityCheck = checkSchoolEmailAvailability(email);
        if (!(boolean) availabilityCheck.get("available")) {
            response.put("success", false);
            response.put("message", availabilityCheck.get("message"));
            response.put("code", availabilityCheck.get("code"));
            return response;
        }

        try {
            // 2. 현재 로그인 사용자 조회
            // ⭐ 수정됨: 안전한 getCurrentUser() 메서드 사용
            Users currentUser = getCurrentUser();

            // 3. 토큰 생성
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

            // 4. 현재 사용자 정보 수정
            currentUser.setSchoolEmail(email);
            currentUser.setSchoolEmailVerificationToken(token);
            currentUser.setSchoolEmailTokenExpires(expiresAt);
            userRepository.save(currentUser);

            // 5. 이메일 전송
            sendVerificationEmail(email, token);

            response.put("success", true);
            response.put("message", "인증 이메일이 전송되었습니다! 이메일을 확인하세요.");
            response.put("code", "EMAIL_SENT");
            log.info("✅ 인증 이메일 전송: {} (사용자: {})", email, currentUser.getUsername());
            return response;

        } catch (Exception e) {
            log.error("❌ 이메일 인증 요청 실패", e);
            response.put("success", false);
            response.put("message", "인증 요청 중 오류가 발생했습니다");
            response.put("code", "SERVER_ERROR");
            return response;
        }
    }

    /**
     * 학교 이메일 도메인 검증
     */
    private boolean isValidSchoolEmail(String email) {
        String lowerEmail = email.toLowerCase();
        for (String domain : ALLOWED_DOMAINS) {
            if (lowerEmail.endsWith(domain)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 인증 이메일 전송
     */
    private void sendVerificationEmail(String email, String token) {
        try {
            String verificationLink = "http://localhost:8088/auth/student-verification/verify?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom("2021166051@kcu.ac.kr");
            message.setSubject("🎓 StudyLink - 대학생 인증");
            message.setText(
                    "안녕하세요!\n\n" +
                            "StudyLink 대학생 인증을 완료하려면 아래 링크를 클릭하세요.\n\n" +
                            "인증 링크: " + verificationLink + "\n\n" +
                            "⏰ 유효시간: 24시간\n\n" +
                            "이 링크를 요청하지 않았다면 이 이메일을 무시하세요.\n\n" +
                            "감사합니다,\nStudyLink 팀"
            );

            mailSender.send(message);
            log.info("✅ 인증 이메일 전송 성공: {}", email);
        } catch (Exception e) {
            log.error("❌ 이메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다");
        }
    }

    /**
     * 이메일 HTML 템플릿 로드 및 변수 치환
     */
    private String loadEmailTemplate(String verificationLink, String email) {
        try {
            String template = new String(
                    java.nio.file.Files.readAllBytes(
                            java.nio.file.Paths.get("src/main/resources/email-templates/verification-email.html")
                    )
            );
            return template
                    .replace("${verificationLink}", verificationLink)
                    .replace("${email}", email);
        } catch (Exception e) {
            log.warn("⚠️ 이메일 템플릿 로드 실패, 기본 텍스트 사용: {}", e.getMessage());
            // Fallback: 기본 텍스트 이메일
            return """
                    안녕하세요!
                    
                    StudyLink 대학생 인증을 완료하려면 아래 링크를 클릭하세요.
                    
                    인증 링크: """ + verificationLink + """
                    
                    ⏰ 유효시간: 24시간
                    
                    이 링크를 요청하지 않았다면 이 이메일을 무시하세요.
                    
                    감사합니다,
                    StudyLink 팀
                    """;
        }
    }

    /**
     * 토큰으로 이메일 인증 완료
     * ⭐ 수정됨: 사용자 역할(role) 및 이메일 정보 업데이트 추가
     */
    public Map<String, Object> verifyEmail(String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 토큰 검증
            Optional<Users> userOpt = userRepository.findBySchoolEmailVerificationToken(token);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "유효하지 않은 토큰입니다");
                response.put("code", "INVALID_TOKEN");
                return response;
            }

            Users user = userOpt.get();

            // 2. 토큰 만료 확인
            if (user.getSchoolEmailTokenExpires().isBefore(LocalDateTime.now())) {
                response.put("success", false);
                response.put("message", "토큰이 만료되었습니다. 다시 인증을 요청하세요.");
                response.put("code", "TOKEN_EXPIRED");
                return response;
            }

            // 3. 인증 완료 및 계정 정보 업데이트
            user.setIsVerifiedStudent(true);
            user.setSchoolEmailVerificationToken(null);
            user.setSchoolEmailTokenExpires(null);
            user.setSchoolEmailVerifiedAt(LocalDateTime.now());

            // 사용자 역할을 MENTOR로 변경 (대학생 인증 시)
            user.setRole("MENTOR");

            // 원래 이메일을 학교 이메일로 업데이트
            // (선택사항: 원래 이메일을 보존하고 싶으면 주석 처리)
            user.setEmail(user.getSchoolEmail());

            userRepository.save(user);

            log.info("✅ 대학생 인증 완료: {} ({})", user.getUsername(), user.getSchoolEmail());
            log.info("✅ 역할 업데이트: {} → MENTOR", user.getUserId());

            response.put("success", true);
            response.put("message", "대학생 인증이 완료되었습니다! 멘토로 등록되었습니다 🎉");
            response.put("code", "VERIFICATION_SUCCESS");
            response.put("userId", user.getUserId());
            response.put("role", user.getRole());
            response.put("schoolEmail", user.getSchoolEmail());
            return response;

        } catch (Exception e) {
            log.error("❌ 이메일 인증 처리 중 오류", e);
            response.put("success", false);
            response.put("message", "인증 처리 중 오류가 발생했습니다");
            response.put("code", "SERVER_ERROR");
            return response;
        }
    }

    /**
     * 사용자의 학교 이메일 인증 상태 조회
     */
    public Map<String, Object> getVerificationStatus(Long userId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Users> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            response.put("verified", false);
            response.put("message", "사용자를 찾을 수 없습니다");
            return response;
        }

        Users user = userOpt.get();
        response.put("verified", user.getIsVerifiedStudent());
        response.put("schoolEmail", user.getSchoolEmail());
        response.put("verifiedAt", user.getSchoolEmailVerifiedAt());
        response.put("role", user.getRole());
        response.put("username", user.getUsername());
        response.put("name", user.getName());
        return response;
    }

    /**
     * 테스트용: 이메일 토큰 초기화
     * 이미 요청된 이메일로 다시 인증 요청할 수 있게 함
     * 개발 환경에서만 사용!
     */
    public Map<String, Object> resetVerificationToken(String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 이메일로 사용자 조회
            Optional<Users> userOpt = userRepository.findBySchoolEmail(email);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "해당 이메일로 등록된 사용자가 없습니다");
                response.put("code", "USER_NOT_FOUND");
                return response;
            }

            Users user = userOpt.get();

            // 2. 이미 인증된 경우 (해제 불가)
            if (user.getIsVerifiedStudent()) {
                response.put("success", false);
                response.put("message", "이미 인증이 완료된 이메일입니다");
                response.put("code", "ALREADY_VERIFIED");
                log.warn("⚠️ 이미 인증된 이메일 초기화 시도: {}", email);
                return response;
            }

            // 3. 토큰 초기화 (제거)
            user.setSchoolEmailVerificationToken(null);
            user.setSchoolEmailTokenExpires(null);
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "✅ 토큰이 초기화되었습니다. 다시 인증을 요청할 수 있습니다.");
            response.put("code", "TOKEN_RESET");
            log.warn("⚠️ [테스트] 이메일 토큰 초기화 완료: {}", email);
            return response;

        } catch (Exception e) {
            log.error("❌ 토큰 초기화 중 오류", e);
            response.put("success", false);
            response.put("message", "토큰 초기화 중 오류가 발생했습니다");
            response.put("code", "SERVER_ERROR");
            return response;
        }
    }
}
