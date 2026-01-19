package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새로운 비밀번호
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
     * @param userId 사용자 ID
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

        // 5. 이메일 변경
        user.setEmail(newEmail);
        user.setEmailVerified(false);  // 새 이메일은 미인증 상태
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 이메일 변경 완료: userId={}, newEmail={}", userId, newEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "이메일이 변경되었습니다. 새 이메일로 인증해주세요");
        response.put("newEmail", newEmail);
        return response;
    }

    /**
     * 휴대폰 번호 변경
     *
     * @param userId 사용자 ID
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
     * @param userId 사용자 ID
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
     * @param userId 사용자 ID
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
     * @param userId 사용자 ID
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
}
