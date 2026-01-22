package com.StudyLink.www.service;

import com.StudyLink.www.entity.Role;
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

    @Value("${app.email.template-path:templates/email-templates/verification-email.html}")  // ← 추가
    private String emailTemplatePath;

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
            "@google.com"
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
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);  // ← plusHours 대신 plusMinutes

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
     * ⭐ 템플릿 파일 로드 실패 시 사용하는 Fallback
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
                "            <table width=\"100%\" style=\"background: #f8f9fa; border-radius: 8px; border-collapse: collapse; margin: 30px 0; border-left: 4px solid #2c5aa0;\">\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 25px;\">\n" +
                "                        <table width=\"100%\" style=\"margin-bottom: 15px; border-collapse: collapse;\">\n" +
                "                            <tr>\n" +
                "                                <td style=\"width: 30px; text-align: center; vertical-align: middle;\">\n" +
                "                                    <div style=\"width: 30px; height: 30px; background: #2c5aa0; color: white; border-radius: 50%; font-weight: 700; font-size: 14px; line-height: 30px; text-align: center;\">1</div>\n" +
                "                                </td>\n" +
                "                                <td style=\"padding-left: 15px; vertical-align: middle;\">\n" +
                "                                    <div style=\"font-weight: 600; color: #1e3c72; font-size: 14px;\">아래 버튼 클릭</div>\n" +
                "                                    <div style=\"font-size: 13px; color: #666666; margin-top: 2px;\">인증 완료 버튼을 클릭해주세요</div>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <table width=\"100%\" style=\"margin-bottom: 15px; border-collapse: collapse;\">\n" +
                "                            <tr>\n" +
                "                                <td style=\"width: 30px; text-align: center; vertical-align: middle;\">\n" +
                "                                    <div style=\"width: 30px; height: 30px; background: #2c5aa0; color: white; border-radius: 50%; font-weight: 700; font-size: 14px; line-height: 30px; text-align: center;\">2</div>\n" +
                "                                </td>\n" +
                "                                <td style=\"padding-left: 15px; vertical-align: middle;\">\n" +
                "                                    <div style=\"font-weight: 600; color: #1e3c72; font-size: 14px;\">인증 완료</div>\n" +
                "                                    <div style=\"font-size: 13px; color: #666666; margin-top: 2px;\">자동으로 인증이 완료됩니다</div>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <table width=\"100%\" style=\"border-collapse: collapse;\">\n" +
                "                            <tr>\n" +
                "                                <td style=\"width: 30px; text-align: center; vertical-align: middle;\">\n" +
                "                                    <div style=\"width: 30px; height: 30px; background: #2c5aa0; color: white; border-radius: 50%; font-weight: 700; font-size: 14px; line-height: 30px; text-align: center;\">3</div>\n" +
                "                                </td>\n" +
                "                                <td style=\"padding-left: 15px; vertical-align: middle;\">\n" +
                "                                    <div style=\"font-weight: 600; color: #1e3c72; font-size: 14px;\">모든 기능 사용 가능</div>\n" +
                "                                    <div style=\"font-size: 13px; color: #666666; margin-top: 2px;\">StudyLink의 모든 서비스를 이용하세요</div>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "            <div style=\"text-align: center; margin: 35px 0;\">\n" +
                "                <a href=\"" + verificationLink + "\" style=\"display: inline-block; background: linear-gradient(135deg, #2c5aa0 0%, #1e3c72 100%); color: white; padding: 16px 40px; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 16px; letter-spacing: 0.5px;\">✅ 인증 완료</a>\n" +
                "            </div>\n" +
                "            <table width=\"100%\" style=\"margin: 30px 0; border-collapse: collapse;\">\n" +
                "                <tr>\n" +
                "                    <td style=\"width: 50%; padding-right: 8px;\">\n" +
                "                        <table width=\"100%\" style=\"background: linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%); padding: 20px; border-radius: 8px; border-collapse: collapse; border-left: 4px solid #2c5aa0;\">\n" +
                "                            <tr>\n" +
                "                                <td style=\"padding: 0;\">\n" +
                "                                    <div style=\"font-size: 12px; color: #2c5aa0; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px;\">⏰ 유효시간</div>\n" +
                "                                    <div style=\"font-size: 14px; color: #333333; font-weight: 600;\">24시간</div>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                    <td style=\"width: 50%; padding-left: 8px;\">\n" +
                "                        <table width=\"100%\" style=\"background: linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%); padding: 20px; border-radius: 8px; border-collapse: collapse; border-left: 4px solid #2c5aa0;\">\n" +
                "                            <tr>\n" +
                "                                <td style=\"padding: 0;\">\n" +
                "                                    <div style=\"font-size: 12px; color: #2c5aa0; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px;\">📧 수신자</div>\n" +
                "                                    <div style=\"font-size: 14px; color: #333333; font-weight: 600; word-break: break-all;\">" + email + "</div>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "            <table width=\"100%\" style=\"background: #f5f7fa; padding: 20px; border-radius: 8px; margin: 25px 0; border-collapse: collapse;\">\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 0;\">\n" +
                "                        <p style=\"font-size: 13px; color: #333333; margin: 0 0 10px 0; font-weight: 600;\"><strong>버튼이 작동하지 않으면?</strong></p>\n" +
                "                        <p style=\"font-size: 13px; color: #666666; margin: 0 0 10px 0; line-height: 1.6;\">아래 링크를 복사하여 브라우저에 붙여넣으세요:</p>\n" +
                "                        <div style=\"background: white; padding: 12px; border-radius: 4px; border: 1px solid #e0e0e0; word-break: break-all; font-size: 12px; color: #2c5aa0; font-weight: 500; font-family: 'Monaco', 'Courier New', monospace; overflow-x: auto;\">" + verificationLink + "</div>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "            <table width=\"100%\" style=\"background: #fff9e6; border-left: 4px solid #ffc107; padding: 15px; border-radius: 4px; margin: 25px 0; border-collapse: collapse;\">\n" +
                "                <tr>\n" +
                "                    <td style=\"padding: 0;\">\n" +
                "                        <p style=\"font-size: 13px; color: #856404; margin: 0; line-height: 1.6;\">\n" +
                "                            <strong>⚠️ 주의:</strong> 이 링크를 요청하지 않았다면 이 이메일을 무시하셔도 됩니다. 또한 다른 사람과 이 링크를 공유하지 마세요.\n" +
                "                        </p>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td style=\"background: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0;\">\n" +
                "            <p style=\"font-size: 14px; color: #333333; margin: 0 0 5px 0; font-weight: 600;\"><strong>StudyLink 팀</strong></p>\n" +
                "            <p style=\"font-size: 13px; color: #999999; margin: 0 0 15px 0;\">대학생들을 위한 멘토링 플랫폼</p>\n" +
                "            <div style=\"width: 50px; height: 2px; background: #2c5aa0; margin: 15px auto;\"></div>\n" +
                "            <p style=\"font-size: 12px; color: #2c5aa0; margin: 0;\">\n" +
                "                <a href=\"#\" style=\"color: #2c5aa0; text-decoration: none; margin: 0 10px; font-weight: 500;\">문의하기</a>\n" +
                "                •\n" +
                "                <a href=\"#\" style=\"color: #2c5aa0; text-decoration: none; margin: 0 10px; font-weight: 500;\">개인정보 보호정책</a>\n" +
                "            </p>\n" +
                "            <p style=\"font-size: 11px; color: #cccccc; margin: 15px 0 0 0;\">\n" +
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
