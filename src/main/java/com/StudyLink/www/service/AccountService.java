/* AccountService */

package com.StudyLink.www.service;

import com.StudyLink.www.dto.VerifyEmailChangeResult;
import com.StudyLink.www.entity.EmailVerificationToken;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.EmailVerificationTokenRepository;
import com.StudyLink.www.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.StudyLink.www.repository.EmailVerificationTokenRepository;
import org.springframework.mail.javamail.JavaMailSender;


/**
 * AccountService (계정 관리 서비스)
 * 사용자 계정 관련 기능 관리
 *
 * 담당 기능:
 * - 비밀번호 변경
 * - 이메일 변경
 * - 휴대폰 번호 변경
 * - 계정 활성화/비활성화
 * - 계정 삭제 (탈퇴)
 * - 계정 상태 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final JavaMailSender mailSender;

    // 현재 비밀번호 검증 (blur용)
    @Transactional(readOnly = true)
    public boolean verifyCurrentPassword(Long userId, String currentPassword) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));


        log.info("🔎 verifyCurrentPassword userId={}, storedPwPrefix={}",
                userId,
                user.getPassword() == null ? "null" : user.getPassword().substring(0, Math.min(7, user.getPassword().length()))
        );

        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    /**
     * 계정 정보 조회
     *
     * @param userId 사용자 ID
     * @return 계정 정보 맵
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAccountInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Map<String, Object> accountInfo = new HashMap<>();
        accountInfo.put("userId", user.getUserId());
        accountInfo.put("email", user.getEmail());
        accountInfo.put("username", user.getUsername());
        accountInfo.put("name", user.getName());
        accountInfo.put("phone", user.getPhone());
        accountInfo.put("emailVerified", user.getEmailVerified());
        accountInfo.put("isActive", user.getIsActive());
        accountInfo.put("createdAt", user.getCreatedAt());
        accountInfo.put("updatedAt", user.getUpdatedAt());

        log.info("✅ 계정 정보 조회: userId={}", userId);
        return accountInfo;

    }


    /**
     * 비밀번호 변경
     * 현재 비밀번호를 확인 후 새로운 비밀번호로 변경
     *
     * @param userId          사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새로운 비밀번호
     * @param confirmPassword 비밀번호 확인
     * @return 변경 성공 여부 맵
     */
    @Transactional
    public Map<String, Object> changePassword(
            Long userId,
            String currentPassword,
            String newPassword,
            String confirmPassword) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 1. 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("❌ 비밀번호 변경 실패: 현재 비밀번호 불일치 - userId={}", userId);
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        // 2. 새 비밀번호와 확인 비밀번호 일치 확인
        if (!newPassword.equals(confirmPassword)) {
            log.warn("❌ 비밀번호 변경 실패: 새 비밀번호 불일치 - userId={}", userId);
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다");
        }

        // 3. 새 비밀번호 검증
        validatePassword(newPassword);

        // 4. 현재 비밀번호와 새 비밀번호가 같은지 확인
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다");
        }

        // 5. 비밀번호 변경
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 비밀번호 변경 완료: userId={}", userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "비밀번호가 성공적으로 변경되었습니다");
        return response;
    }

    /**
     * 이메일 변경
     * 현재 이메일을 새로운 이메일로 변경
     * (실제로는 이메일 인증 필요)
     *
     * @param userId   사용자 ID
     * @param newEmail 새로운 이메일
     * @param password 비밀번호 확인
     * @return 변경 성공 여부 맵
     */
    @Transactional
    public Map<String, Object> changeEmail(
            Long userId,
            String newEmail,
            String password) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 1. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("❌ 이메일 변경 실패: 비밀번호 불일치 - userId={}", userId);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 2. 이메일 형식 검증
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("올바른 이메일 형식을 입력하세요");
        }

        // 3. 현재 이메일과 새 이메일이 같은지 확인
        if (user.getEmail().equals(newEmail)) {
            throw new IllegalArgumentException("새 이메일은 현재 이메일과 달라야 합니다");
        }

        // 4. 이메일 중복 확인
        if (userRepository.findByEmail(newEmail).isPresent()) {
            log.warn("❌ 이메일 변경 실패: 이미 사용 중인 이메일 - email={}", newEmail);
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

// 5. 이메일 변경 "요청" (즉시 변경 금지)
        String requestedUsername = user.getUsername();

// (선택) 기존 토큰 삭제: 같은 계정에서 재요청 시 갱신
        emailVerificationTokenRepository.deleteByRequestedUsername(requestedUsername);

// 링크 토큰(길게)
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();

// 토큰 저장
        EmailVerificationToken t = EmailVerificationToken.builder()
                .email(newEmail)
                .requestedUsername(requestedUsername)
                .verificationCode(token)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        emailVerificationTokenRepository.save(t);

// ✅ 여기서 메일 발송 (confirm 링크 포함)
        sendEmailChangeConfirmMail(newEmail, token, requestedUsername);

        log.info("📧 이메일 변경 확인 메일 발송: userId={}, newEmail={}", userId, newEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "새 이메일로 확인 메일을 보냈습니다. 메일에서 승인하면 변경이 완료됩니다.");
        response.put("newEmail", newEmail);
        return response;

    }

    private void sendEmailChangeConfirmMail(String newEmail, String token, String requestedUsername) {
        try {
            String link = "http://localhost:8088/api/account/change-email/confirm"
                    + "?token=" + token
                    + "&username=" + requestedUsername;

            boolean isUniv = isUniversityEmail(newEmail);

            // ✅ 대학 이메일일 때만 안내 박스(조건부)
            String universityNoticeHtml = "";
            if (isUniv) {
                universityNoticeHtml = """
                <table width="100%" style="background: #eaf4ff; border-radius: 8px; border-collapse: collapse; margin: 18px 0 30px 0; border-left: 4px solid #2c5aa0;">
                    <tr>
                        <td style="padding: 16px 18px;">
                            <div style="font-weight: 700; color: #1e3c72; font-size: 14px; margin-bottom: 4px;">🎓 대학 이메일로 확인되었습니다</div>
                            <div style="font-size: 13px; color: #555555; line-height: 1.7;">
                                이메일 변경을 완료하면, 이어서 <strong>대학생 인증(멘토 인증)</strong>도 진행할 수 있습니다.<br>
                                일부 학교 이메일은 인증 방식에 따라 추가 절차가 있을 수 있어요.
                            </div>
                        </td>
                    </tr>
                </table>
            """;
            }

            // ✅ 제목도 (선택) 대학 이메일이면 살짝 다르게
            String subject = isUniv
                    ? "StudyLink - 이메일 변경 확인 (대학 이메일 감지됨)"
                    : "StudyLink - 이메일 변경 확인";

            String html = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif; background: #f5f7fa;">
            <table width="100%%" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; border-collapse: collapse;">
                <!-- Header -->
                <tr>
                    <td style="background: linear-gradient(135deg, #2c5aa0 0%%, #1e3c72 100%%); padding: 40px 30px; text-align: center;">
                        <div style="font-size: 40px; margin-bottom: 10px;">📧</div>
                        <h1 style="font-size: 32px; color: #ffffff; margin: 0 0 5px 0; font-weight: 700; letter-spacing: -0.5px;">StudyLink</h1>
                        <p style="color: rgba(255, 255, 255, 0.9); font-size: 16px; font-weight: 300; margin: 0;">이메일 변경 확인</p>
                    </td>
                </tr>

                <!-- Main Content -->
                <tr>
                    <td style="padding: 40px 30px; color: #333333;">
                        <!-- Greeting -->
                        <div style="font-size: 18px; color: #1e3c72; font-weight: 600; margin-bottom: 20px;">안녕하세요! 👋</div>

                        <p style="font-size: 15px; line-height: 1.8; color: #555555; margin: 0 0 15px 0;">
                            StudyLink 계정의 이메일 변경 요청이 접수되었습니다.<br>
                            아래 버튼을 클릭하면 <strong>이메일 변경이 최종 완료</strong>됩니다.
                        </p>

                        %s

                        <!-- Steps -->
                        <table width="100%%" style="background: #f8f9fa; border-radius: 8px; border-collapse: collapse; margin: 0 0 30px 0; border-left: 4px solid #2c5aa0;">
                            <tr>
                                <td style="padding: 25px;">
                                    <!-- Step 1 -->
                                    <table width="100%%" style="margin-bottom: 15px; border-collapse: collapse;">
                                        <tr>
                                            <td style="width: 30px; text-align: center; vertical-align: middle;">
                                                <div style="width: 30px; height: 30px; background: #2c5aa0; color: white; border-radius: 50%%; font-weight: 700; font-size: 14px; line-height: 30px; text-align: center;">1</div>
                                            </td>
                                            <td style="padding-left: 15px; vertical-align: middle;">
                                                <div style="font-weight: 600; color: #1e3c72; font-size: 14px;">아래 버튼 클릭</div>
                                                <div style="font-size: 13px; color: #666666; margin-top: 2px;">이메일 변경 승인 버튼을 클릭해주세요</div>
                                            </td>
                                        </tr>
                                    </table>

                                    <!-- Step 2 -->
                                    <table width="100%%" style="margin-bottom: 15px; border-collapse: collapse;">
                                        <tr>
                                            <td style="width: 30px; text-align: center; vertical-align: middle;">
                                                <div style="width: 30px; height: 30px; background: #2c5aa0; color: white; border-radius: 50%%; font-weight: 700; font-size: 14px; line-height: 30px; text-align: center;">2</div>
                                            </td>
                                            <td style="padding-left: 15px; vertical-align: middle;">
                                                <div style="font-weight: 600; color: #1e3c72; font-size: 14px;">이메일 변경 완료</div>
                                                <div style="font-size: 13px; color: #666666; margin-top: 2px;">새 이메일로 계정 이메일이 업데이트됩니다</div>
                                            </td>
                                        </tr>
                                    </table>

                                    <!-- Step 3 -->
                                    <table width="100%%" style="border-collapse: collapse;">
                                        <tr>
                                            <td style="width: 30px; text-align: center; vertical-align: middle;">
                                                <div style="width: 30px; height: 30px; background: #2c5aa0; color: white; border-radius: 50%%; font-weight: 700; font-size: 14px; line-height: 30px; text-align: center;">3</div>
                                            </td>
                                            <td style="padding-left: 15px; vertical-align: middle;">
                                                <div style="font-weight: 600; color: #1e3c72; font-size: 14px;">(선택) 대학생/멘토 인증 진행</div>
                                                <div style="font-size: 13px; color: #666666; margin-top: 2px;">대학 이메일이라면 인증 단계로 이어질 수 있어요</div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>

                        <!-- CTA Button -->
                        <div style="text-align: center; margin: 35px 0;">
                            <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #2c5aa0 0%%, #1e3c72 100%%); color: white; padding: 16px 40px; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 16px; letter-spacing: 0.5px;">✅ 이메일 변경 승인</a>
                        </div>

                        <!-- Info Boxes -->
                        <table width="100%%" style="margin: 30px 0; border-collapse: collapse;">
                            <tr>
                                <td style="width: 50%%; padding-right: 8px;">
                                    <table width="100%%" style="background: linear-gradient(135deg, #f0f4f8 0%%, #d9e2ec 100%%); padding: 20px; border-radius: 8px; border-collapse: collapse; border-left: 4px solid #2c5aa0;">
                                        <tr>
                                            <td style="padding: 0;">
                                                <div style="font-size: 12px; color: #2c5aa0; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px;">⏰ 유효시간</div>
                                                <div style="font-size: 14px; color: #333333; font-weight: 600;">30분</div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                                <td style="width: 50%%; padding-left: 8px;">
                                    <table width="100%%" style="background: linear-gradient(135deg, #f0f4f8 0%%, #d9e2ec 100%%); padding: 20px; border-radius: 8px; border-collapse: collapse; border-left: 4px solid #2c5aa0;">
                                        <tr>
                                            <td style="padding: 0;">
                                                <div style="font-size: 12px; color: #2c5aa0; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px;">📧 변경 이메일</div>
                                                <div style="font-size: 14px; color: #333333; font-weight: 600; word-break: break-all;">%s</div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>

                        <!-- Link Copy Section -->
                        <table width="100%%" style="background: #f5f7fa; padding: 20px; border-radius: 8px; margin: 25px 0; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 0;">
                                    <p style="font-size: 13px; color: #333333; margin: 0 0 10px 0; font-weight: 600;"><strong>버튼이 작동하지 않으면?</strong></p>
                                    <p style="font-size: 13px; color: #666666; margin: 0 0 10px 0; line-height: 1.6;">아래 링크를 복사하여 브라우저에 붙여넣으세요:</p>
                                    <div style="background: white; padding: 12px; border-radius: 4px; border: 1px solid #e0e0e0; word-break: break-all; font-size: 12px; color: #2c5aa0; font-weight: 500; font-family: 'Monaco', 'Courier New', monospace; overflow-x: auto;">%s</div>
                                </td>
                            </tr>
                        </table>

                        <!-- Warning -->
                        <table width="100%%" style="background: #fff9e6; border-left: 4px solid #ffc107; padding: 15px; border-radius: 4px; margin: 25px 0; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 0;">
                                    <p style="font-size: 13px; color: #856404; margin: 0; line-height: 1.6;">
                                        <strong>⚠️ 주의:</strong> 이 요청을 본인이 하지 않았다면 이 이메일을 무시하셔도 됩니다. 또한 다른 사람과 이 링크를 공유하지 마세요.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <!-- Footer -->
                <tr>
                    <td style="background: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                        <p style="font-size: 14px; color: #333333; margin: 0 0 5px 0; font-weight: 600;"><strong>StudyLink 팀</strong></p>
                        <p style="font-size: 13px; color: #999999; margin: 0 0 15px 0;">대학생들을 위한 멘토링 플랫폼</p>

                        <div style="width: 50px; height: 2px; background: #2c5aa0; margin: 15px auto;"></div>

                        <p style="font-size: 12px; color: #2c5aa0; margin: 0;">
                            <a href="#" style="color: #2c5aa0; text-decoration: none; margin: 0 10px; font-weight: 500;">문의하기</a>
                            •
                            <a href="#" style="color: #2c5aa0; text-decoration: none; margin: 0 10px; font-weight: 500;">개인정보 보호정책</a>
                        </p>

                        <p style="font-size: 11px; color: #cccccc; margin: 15px 0 0 0;">
                            © 2026 StudyLink. All rights reserved.
                        </p>
                    </td>
                </tr>
            </table>
            </body>
            </html>
            """.formatted(universityNoticeHtml, link, newEmail, link);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(newEmail);
            helper.setFrom("2021166051@kcu.ac.kr"); // StudentVerificationService와 동일하게
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("❌ 이메일 변경 확인 메일 전송 실패", e);
            throw new RuntimeException("이메일 전송에 실패했습니다");
        }
    }

    /**
     * ✅ 대학 이메일 판별 (도메인 리스트 방식)
     * - 운영에서는 학교 도메인 목록을 DB/설정파일로 관리하는 걸 추천
     */
    private boolean isUniversityEmail(String email) {
        if (email == null) return false;
        String lower = email.trim().toLowerCase();

        // 흔한 한국 대학 이메일 도메인 예시 (너 프로젝트에 맞게 추가/수정)
        String[] universityDomains = {
                "ac.kr",
                "edu",          // 해외 일부
                "edu.",
                "siswa.um.edu.my",
                "siswa-old.um.edu.my",
                "gmail.com",
                "naver.com"
        };

        int at = lower.lastIndexOf("@");
        if (at < 0) return false;
        String domain = lower.substring(at + 1);

        // 1) 가장 강력: ".ac.kr" 포함
        if (domain.endsWith(".ac.kr")) return true;

        // 2) 필요 시 추가 규칙
        for (String d : universityDomains) {
            if (domain.equals(d) || domain.endsWith("." + d) || domain.contains(d)) {
                return true;
            }
        }
        return false;
    }



    /**
     * 휴대폰 번호 변경
     *
     * @param userId   사용자 ID
     * @param newPhone 새로운 휴대폰 번호
     * @param password 비밀번호 확인
     * @return 변경 성공 여부 맵
     */
    @Transactional
    public Map<String, Object> changePhone(
            Long userId,
            String newPhone,
            String password) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 1. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("❌ 휴대폰 번호 변경 실패: 비밀번호 불일치 - userId={}", userId);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 2. 휴대폰 번호 형식 검증
        if (!newPhone.matches("^01[0-9]-?\\d{3,4}-?\\d{4}$")) {
            throw new IllegalArgumentException("올바른 휴대폰 번호 형식을 입력하세요 (예: 010-1234-5678)");
        }

        // 3. 현재 번호와 새 번호가 같은지 확인
        if (user.getPhone() != null && user.getPhone().equals(newPhone)) {
            throw new IllegalArgumentException("새 휴대폰 번호는 현재 번호와 달라야 합니다");
        }

        // 4. 휴대폰 번호 변경
        user.setPhone(newPhone);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 휴대폰 번호 변경 완료: userId={}", userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "휴대폰 번호가 변경되었습니다");
        response.put("newPhone", newPhone);
        return response;
    }

    /**
     * 계정 활성화
     *
     * @param userId 사용자 ID
     * @return 활성화 성공 여부 맵
     */
    @Transactional
    public Map<String, Object> activateAccount(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (user.getIsActive()) {
            throw new IllegalArgumentException("이미 활성화된 계정입니다");
        }

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 계정 활성화: userId={}", userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "계정이 활성화되었습니다");
        return response;
    }

    /**
     * 계정 비활성화 (일시 중지)
     *
     * @param userId   사용자 ID
     * @param password 비밀번호 확인
     * @return 비활성화 성공 여부 맵
     */
    @Transactional
    public Map<String, Object> deactivateAccount(Long userId, String password) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("❌ 계정 비활성화 실패: 비밀번호 불일치 - userId={}", userId);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("이미 비활성화된 계정입니다");
        }

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 계정 비활성화: userId={}", userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "계정이 비활성화되었습니다");
        return response;
    }

    /**
     * 계정 삭제 (회원 탈퇴)
     * 영구 삭제 전에 비밀번호 확인 필수
     *
     * @param userId   사용자 ID
     * @param password 비밀번호
     * @return 삭제 성공 여부 맵
     */
    @Transactional
    public Map<String, Object> deleteAccount(Long userId, String password) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 1. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("❌ 계정 삭제 실패: 비밀번호 불일치 - userId={}", userId);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 2. 계정 삭제 (cascade로 관련 데이터도 삭제됨)
        userRepository.delete(user);

        log.info("✅ 계정 삭제 완료 (탈퇴): userId={}, email={}", userId, user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "계정이 삭제되었습니다");
        return response;
    }

    /**
     * 계정 상태 확인
     *
     * @param userId 사용자 ID
     * @return 계정 상태 맵
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAccountStatus(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Map<String, Object> status = new HashMap<>();
        status.put("userId", user.getUserId());
        status.put("isActive", user.getIsActive());
        status.put("emailVerified", user.getEmailVerified());
        status.put("isStudentVerified", user.getIsStudentVerified());
        status.put("lastUpdated", user.getUpdatedAt());

        return status;
    }

    /**
     * 이메일 중복 확인
     *
     * @param email 이메일
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    /**
     * 비밀번호 검증 (형식)
     *
     * @param password 비밀번호
     * @throws IllegalArgumentException 형식이 맞지 않으면
     */
    private void validatePassword(String password) {
        // 비밀번호 길이 확인
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다");
        }

        if (password.length() > 100) {
            throw new IllegalArgumentException("비밀번호는 100자 이하여야 합니다");
        }

        // 비밀번호 복잡도 검증 (선택사항)
        // 최소한 하나의 숫자와 하나의 문자 포함
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasLetter = password.matches(".*[a-zA-Z].*");

        if (!hasNumber || !hasLetter) {
            throw new IllegalArgumentException("비밀번호는 숫자와 문자를 포함해야 합니다");
        }
    }

    /**
     * 이메일 인증 상태 업데이트
     *
     * @param userId   사용자 ID
     * @param verified 인증 여부
     * @return 업데이트 성공 여부 맵
     */
    @Transactional
    public Map<String, Object> updateEmailVerificationStatus(Long userId, boolean verified) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.setEmailVerified(verified);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 이메일 인증 상태 업데이트: userId={}, verified={}", userId, verified);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("verified", verified);
        return response;
    }

    /**
     * 비밀번호 재설정 가능 여부 확인
     *
     * @param email 사용자 이메일
     * @return 사용자 존재 여부 및 활성화 여부
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> checkPasswordResetEligibility(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    Map<String, Object> eligibility = new HashMap<>();
                    eligibility.put("userId", user.getUserId());
                    eligibility.put("email", user.getEmail());
                    eligibility.put("isActive", user.getIsActive());
                    eligibility.put("eligible", user.getIsActive());  // 활성 계정만 가능
                    return eligibility;
                });
    }

    @Transactional
    public String confirmEmailChange(String token, String requestedUsername) {

        EmailVerificationToken t = emailVerificationTokenRepository
                .findByVerificationCodeAndRequestedUsername(token, requestedUsername)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다"));

        if (t.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationTokenRepository.delete(t);
            throw new IllegalArgumentException("토큰이 만료되었습니다");
        }

        Users user = userRepository.findByUsername(requestedUsername)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        String newEmail = t.getEmail();

        // 이미 다른 계정이 사용 중인지 체크
        Optional<Users> existing = userRepository.findByEmail(newEmail);
        if (existing.isPresent() && !existing.get().getUserId().equals(user.getUserId())) {
            emailVerificationTokenRepository.delete(t);
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        // ✅ 실제 이메일 변경
        user.setEmail(newEmail);
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 토큰 삭제
        emailVerificationTokenRepository.delete(t);

        // ⭐ redirect 판단용으로 email 반환
        return newEmail;
    }

    @Transactional
    public VerifyEmailChangeResult confirmEmailChangeResult(String token, String requestedUsername) {
        // ✅ 실제 변경/검증/토큰삭제 로직은 confirmEmailChange() 한 군데만 사용
        String newEmail = confirmEmailChange(token, requestedUsername);

        boolean isUniv = isUniversityEmail(newEmail);

        return new VerifyEmailChangeResult(
                true,
                null,
                maskEmail(newEmail),
                isUniv
        );
    }


    /** 이메일 마스킹 (페이지 표시용) */
    private String maskEmail(String email) {
        if (email == null) return "";
        int at = email.indexOf("@");
        if (at <= 1) return "***" + email.substring(Math.max(at, 0));
        String local = email.substring(0, at);
        String domain = email.substring(at);
        return local.substring(0, 2) + "***" + domain;
    }


}
