package com.StudyLink.www.service;

import com.StudyLink.www.entity.Role;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Value("${app.email.template-path:templates/email-templates/verification-email.html}")  // ← 추가
    private String emailTemplatePath;

    // 이메일 재전송 쿨다운 시간 (분)
    private static final int EMAIL_RESEND_COOLDOWN_MINUTES = 5;

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
            "@siswa.um.edu.my",  // 말라야대학교
            "@kcu.ac.kr",
            "@gangseo.ac.kr",
            "@naver.com",
            "@google.com",
            "@gmail.com"
    };

    /**
     * 현재 로그인한 사용자 정보 조회 (안전한 방식)
     * 엔티티 캐스팅 대신 Repository로 조회
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

            // 인증된 경우
            if (user.getIsVerifiedStudent()) {
                response.put("available", false);
                response.put("message", "이미 인증된 이메일입니다");
                response.put("code", "ALREADY_VERIFIED");
                log.warn("⚠️ 이미 인증된 이메일 사용 시도: {} (소유자: {})", email, user.getUsername());
                return response;
            }

            // 인증 대기 중이지만 쿨다운이 경과한 경우 재전송 가능하도록 변경
            if (user.getSchoolEmailVerificationToken() != null) {
                LocalDateTime lastSentAt = user.getLastEmailSentAt();

                // 마지막 전송 시간이 없거나, 쿨다운이 경과했는지 확인
                if (lastSentAt != null) {
                    LocalDateTime canResendAt = lastSentAt.plusMinutes(EMAIL_RESEND_COOLDOWN_MINUTES);

                    if (LocalDateTime.now().isBefore(canResendAt)) {
                        // 쿨다운 진행 중 - 재전송 불가
                        response.put("available", false);
                        response.put("message", "이미 인증 요청된 이메일입니다. 이메일을 확인하세요.");
                        response.put("code", "VERIFICATION_PENDING");
                        log.warn("⚠️ 인증 대기 중인 이메일 재요청 (쿨다운 진행 중): {} (사용자: {})", email, user.getUsername());
                        return response;
                    } else {
                        // 쿨다운 경과 - 재전송 가능
                        response.put("available", true);
                        response.put("message", "사용 가능한 이메일입니다");
                        response.put("code", "AVAILABLE");
                        log.info("✅ 쿨다운 경과한 이메일 재전송 가능: {} (사용자: {})", email, user.getUsername());
                        return response;
                    }
                } else {
                    // 마지막 전송 시간이 없음 - 재전송 가능
                    response.put("available", true);
                    response.put("message", "사용 가능한 이메일입니다");
                    response.put("code", "AVAILABLE");
                    return response;
                }
            }
        }

        // 사용 가능한 경우만 여기에 도달
        response.put("available", true);
        response.put("message", "사용 가능한 이메일입니다");
        response.put("code", "AVAILABLE");
        return response;
    }

    /**
     * 이메일 재전송 쿨다운 확인
     */
    public Map<String, Object> getResendCooldown(Users user) {
        Map<String, Object> response = new HashMap<>();

        LocalDateTime lastSentAt = user.getLastEmailSentAt();

        if (lastSentAt == null) {
            response.put("canResend", true);
            response.put("remainingSeconds", 0);
            response.put("message", "이메일을 보낼 수 있습니다");
            return response;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime canResendAt = lastSentAt.plusMinutes(EMAIL_RESEND_COOLDOWN_MINUTES);

        if (now.isAfter(canResendAt)) {
            response.put("canResend", true);
            response.put("remainingSeconds", 0);
            response.put("message", "다시 이메일을 보낼 수 있습니다");
            log.info("✅ 이메일 재전송 쿨다운 종료: {}", user.getEmail());
        } else {
            long remainingSeconds = java.time.temporal.ChronoUnit.SECONDS
                    .between(now, canResendAt);
            int remainingMinutes = (int) Math.ceil(remainingSeconds / 60.0);

            response.put("canResend", false);
            response.put("remainingSeconds", remainingSeconds);
            response.put("remainingMinutes", remainingMinutes);
            response.put("message", remainingMinutes + "분 후에 다시 시도하세요");
            log.info("⏳ 이메일 재전송 쿨다운 진행 중: {} ({} 분 남음)", user.getEmail(), remainingMinutes);
        }

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
            // 안전한 getCurrentUser() 메서드 사용
            Users currentUser = getCurrentUser();

            // 쿨다운 체크
            Map<String, Object> cooldownCheck = getResendCooldown(currentUser);
            if (!(boolean) cooldownCheck.get("canResend")) {
                response.put("success", false);
                response.put("message", cooldownCheck.get("message"));
                response.put("code", "COOLDOWN_ACTIVE");
                response.put("remainingSeconds", cooldownCheck.get("remainingSeconds"));
                response.put("remainingMinutes", cooldownCheck.get("remainingMinutes"));
                log.warn("⏳ 이메일 재전송 쿨다운 진행 중: {} ({} 초 남음)",
                        currentUser.getEmail(), cooldownCheck.get("remainingSeconds"));
                return response;
            }

            // 3. 다른 사용자가 이미 이 school_email을 사용 중인지 확인
            Optional<Users> existingUser = userRepository.findBySchoolEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(currentUser.getUserId())) {
                log.warn("⚠️ 다른 계정에서 이미 사용 중인 이메일: {} (기존 userId: {}, 현재 userId: {})",
                        email, existingUser.get().getUserId(), currentUser.getUserId());

                response.put("success", false);
                response.put("message", "다른 계정에서 이미 인증을 요청한 이메일입니다. 해당 계정의 인증을 완료하거나, 다른 이메일을 사용해주세요.");
                response.put("code", "EMAIL_ALREADY_REQUESTED");
                return response;
            }

            // 4. 토큰 생성
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);  // ← plusHours 대신 plusMinutes

            // 5. 현재 사용자 정보 수정
            currentUser.setSchoolEmail(email);
            currentUser.setSchoolEmailVerificationToken(token);
            currentUser.setSchoolEmailTokenExpires(expiresAt);
            currentUser.setLastEmailSentAt(LocalDateTime.now());  // ⭐ 전송 시간 기록
            userRepository.save(currentUser);

            // 6. 이메일 전송
            sendVerificationEmail(email, token);

            response.put("success", true);
            response.put("message", "인증 이메일이 전송되었습니다! 이메일을 확인하세요.");
            response.put("code", "EMAIL_SENT");
            response.put("canResend", false);
            response.put("remainingSeconds", EMAIL_RESEND_COOLDOWN_MINUTES * 60);
            log.info("✅ 인증 이메일 전송: {} (사용자: {})", email, currentUser.getUsername());
            return response;

        } catch (DataIntegrityViolationException e) {
            // ✅ UNIQUE 제약조건 위반 → 사용자 친화적 메시지
            log.error("❌ UNIQUE 제약 위반: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "다른 계정에서 이미 인증을 요청한 이메일입니다. 해당 계정의 인증을 완료하거나, 다른 이메일을 사용해주세요.");
            response.put("code", "EMAIL_ALREADY_REQUESTED");
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
     * 인증 이메일 전송 (HTML 이메일)
     * 리소스 파일(templates/email-templates/verification-email.html)에서 로드
     */
    private void sendVerificationEmail(String email, String token) {
        try {
            String verificationLink = "http://localhost:8088/auth/student-verification/verify?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setFrom("2021166051@kcu.ac.kr");
            helper.setSubject("🎓 StudyLink - 대학생 인증");

            // HTML 템플릿 파일에서 로드
            String htmlContent = loadEmailTemplate(verificationLink, email);
            helper.setText(htmlContent, true);  // true = HTML 모드

            mailSender.send(message);
            log.info("✅ HTML 인증 이메일 전송 성공: {}", email);

        } catch (MessagingException e) {
            log.error("❌ 이메일 전송 실패 (MessagingException): {}", e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ 이메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다");
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
     * 이메일 HTML 템플릿 로드 및 변수 치환
     */
    private String loadEmailTemplate(String verificationLink, String email) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String template = new String(
                    classLoader.getResourceAsStream(emailTemplatePath)
                            .readAllBytes()
            );
            return template
                    .replace("${verificationLink}", verificationLink)
                    .replace("${email}", email);
        }  catch (Exception e) {
            log.warn("⚠️ 이메일 템플릿 로드 실패 ({}), 기본 HTML 사용합니다", e.getMessage());
            return getDefaultHtmlTemplate(verificationLink, email);  // ← Fallback 추가
        }
    }

    /**
     * 기본 HTML 이메일 템플릿 (inline CSS)
     * 템플릿 파일 로드 실패 시 사용하는 Fallback
     */
    private String getDefaultHtmlTemplate(String verificationLink, String email) {
        String html = "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif; background: #f5f7fa;\">\n" +
                "<table width=\"100%\" style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; border-collapse: collapse;\">\n" +
                "    <tr>\n" +
                "        <td style=\"background: linear-gradient(135deg, #2c5aa0 0%, #1e3c72 100%); padding: 40px 30px; text-align: center;\">\n" +
                "            <div style=\"font-size: 40px; margin-bottom: 10px;\">🎓</div>\n" +
                "            <h1 style=\"font-size: 32px; color: #ffffff; margin: 0 0 5px 0; font-weight: 700; letter-spacing: -0.5px;\">StudyLink</h1>\n" +
                "            <p style=\"color: rgba(255, 255, 255, 0.9); font-size: 16px; font-weight: 300; margin: 0;\">대학생 인증 완료</p>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td style=\"padding: 40px 30px; color: #333333;\">\n" +
                "            <div style=\"font-size: 18px; color: #1e3c72; font-weight: 600; margin-bottom: 20px;\">안녕하세요! 👋</div>\n" +
                "            <p style=\"font-size: 15px; line-height: 1.8; color: #555555; margin: 0 0 15px 0;\">\n" +
                "                StudyLink에 가입해주셔서 감사합니다!<br>\n" +
                "                아래 버튼을 클릭하여 대학생 인증을 완료하시면 모든 멘토 기능을 이용하실 수 있습니다.\n" +
                "            </p>\n" +
                "            <div style=\"text-align: center; margin: 35px 0;\">\n" +
                "                <a href=\"" + verificationLink + "\" style=\"display: inline-block; background: linear-gradient(135deg, #2c5aa0 0%, #1e3c72 100%); color: white; padding: 16px 40px; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 16px; letter-spacing: 0.5px;\">✅ 인증 완료</a>\n" +
                "            </div>\n" +
                "            <p style=\"font-size: 13px; color: #999999; margin: 15px 0 0 0;\">\n" +
                "                © 2026 StudyLink. All rights reserved.\n" +
                "            </p>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";
        return html;
    }



    /**
     * 토큰으로 이메일 인증 완료
     * 사용자 역할(role) 및 이메일 정보 업데이트 추가
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
            user.setRole(Role.MENTOR);

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
