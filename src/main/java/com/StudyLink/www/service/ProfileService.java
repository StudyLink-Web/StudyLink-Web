package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ProfileService (프로필 관리 서비스)
 * 사용자 프로필 정보 수정 및 관리
 *
 * 담당 기능:
 * - 기본 정보 수정 (이름, 닉네임, 프로필 사진)
 * - 자기소개 수정
 * - 관심사/전공 수정
 * - 휴대폰 번호 수정
 * - 학년/학과 수정
 * - 프로필 정보 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;

    /**
     * 프로필 기본 정보 조회
     *
     * @param userId 사용자 ID
     * @return 프로필 정보 맵
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProfile(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("email", user.getEmail());
        profile.put("name", user.getName());
        profile.put("nickname", user.getNickname());
        profile.put("role", user.getRole());
        profile.put("profileImageUrl", user.getProfileImageUrl());
        profile.put("gradeYear", user.getGradeYear());
        profile.put("interests", user.getInterests());
        profile.put("phone", user.getPhone());
        profile.put("emailVerified", user.getEmailVerified());
        profile.put("createdAt", user.getCreatedAt());

        log.info("✅ 프로필 조회: userId={}, nickname={}", userId, user.getNickname());
        return profile;
    }

    /**
     * 기본 정보 수정 (이름, 닉네임)
     *
     * @param userId 사용자 ID
     * @param name 새로운 이름
     * @param nickname 새로운 닉네임
     * @return 수정된 프로필 정보 맵
     */
    @Transactional
    public Map<String, Object> updateBasicInfo(Long userId, String name, String nickname) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 이름 수정
        if (name != null && !name.trim().isEmpty()) {
            if (name.length() < 2 || name.length() > 50) {
                throw new IllegalArgumentException("이름은 2자 이상 50자 이하여야 합니다");
            }
            user.setName(name);
            log.info("✅ 이름 수정: userId={}, name={}", userId, name);
        }

        // 닉네임 수정
        if (nickname != null && !nickname.trim().isEmpty()) {
            if (nickname.length() < 2 || nickname.length() > 50) {
                throw new IllegalArgumentException("닉네임은 2자 이상 50자 이하여야 합니다");
            }

            // 닉네임 중복 확인 (현재 사용자의 닉네임이 아닌 경우만)
            if (!user.getNickname().equals(nickname) &&
                    userRepository.findByNickname(nickname).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다");
            }

            user.setNickname(nickname);
            user.setUsername(nickname);  // username도 함께 업데이트
            log.info("✅ 닉네임 수정: userId={}, nickname={}", userId, nickname);
        }

        user.setUpdatedAt(LocalDateTime.now());
        Users updatedUser = userRepository.save(user);

        return getProfile(updatedUser.getUserId());
    }

    /**
     * 프로필 사진 업로드 (URL 저장)
     *
     * @param userId 사용자 ID
     * @param profileImageUrl 프로필 이미지 URL
     * @return 수정된 프로필 정보 맵
     */
    @Transactional
    public Map<String, Object> updateProfileImage(Long userId, String profileImageUrl) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
            user.setProfileImageUrl(profileImageUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("✅ 프로필 사진 수정: userId={}, imageUrl={}", userId, profileImageUrl);
        }

        return getProfile(userId);
    }

    /**
     * 프로필 사진 삭제 (기본 이미지로 변경)
     *
     * @param userId 사용자 ID
     * @return 수정된 프로필 정보 맵
     */
    @Transactional
    public Map<String, Object> deleteProfileImage(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.setProfileImageUrl(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("✅ 프로필 사진 삭제: userId={}", userId);

        return getProfile(userId);
    }

    /**
     * 자기소개/관심사 수정
     *
     * @param userId 사용자 ID
     * @param interests 관심사 (쉼표로 구분)
     * @return 수정된 프로필 정보 맵
     */
    @Transactional
    public Map<String, Object> updateInterests(Long userId, String interests) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (interests != null) {
            if (interests.length() > 500) {
                throw new IllegalArgumentException("관심사는 500자 이하여야 합니다");
            }
            user.setInterests(interests);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("✅ 관심사 수정: userId={}, interests={}", userId, interests);
        }

        return getProfile(userId);
    }

    /**
     * 휴대폰 번호 수정
     *
     * @param userId 사용자 ID
     * @param phone 휴대폰 번호
     * @return 수정된 프로필 정보 맵
     */
    @Transactional
    public Map<String, Object> updatePhone(Long userId, String phone) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (phone != null && !phone.trim().isEmpty()) {
            // 휴대폰 번호 형식 검증 (010-1234-5678 또는 01012345678)
            if (!phone.matches("^01[0-9]-?\\d{3,4}-?\\d{4}$")) {
                throw new IllegalArgumentException("올바른 휴대폰 번호 형식을 입력하세요 (예: 010-1234-5678)");
            }

            user.setPhone(phone);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("✅ 휴대폰 번호 수정: userId={}", userId);
        }

        return getProfile(userId);
    }

    /**
     * 학년/학과 정보 수정
     *
     * @param userId 사용자 ID
     * @param gradeYear 학년 (고1, 고2, 고3, 대1, 대2 등)
     * @return 수정된 프로필 정보 맵
     */
    @Transactional
    public Map<String, Object> updateGradeYear(Long userId, String gradeYear) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (gradeYear != null && !gradeYear.trim().isEmpty()) {
            if (gradeYear.length() > 50) {
                throw new IllegalArgumentException("학년 정보는 50자 이하여야 합니다");
            }

            user.setGradeYear(gradeYear);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("✅ 학년/학과 정보 수정: userId={}, gradeYear={}", userId, gradeYear);
        }

        return getProfile(userId);
    }

    /**
     * 프로필 정보 종합 수정
     * 여러 필드를 한 번에 수정
     *
     * @param userId 사용자 ID
     * @param name 이름
     * @param nickname 닉네임
     * @param phone 휴대폰 번호
     * @param gradeYear 학년/학과
     * @param interests 관심사
     * @param profileImageUrl 프로필 이미지 URL
     * @return 수정된 프로필 정보 맵
     */
    @Transactional
    public Map<String, Object> updateProfileComprehensive(
            Long userId,
            String name,
            String nickname,
            String phone,
            String gradeYear,
            String interests,
            String profileImageUrl) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 이름 수정
        if (name != null && !name.trim().isEmpty()) {
            if (name.length() < 2 || name.length() > 50) {
                throw new IllegalArgumentException("이름은 2자 이상 50자 이하여야 합니다");
            }
            user.setName(name);
        }

        // 닉네임 수정
        if (nickname != null && !nickname.trim().isEmpty()) {
            if (nickname.length() < 2 || nickname.length() > 50) {
                throw new IllegalArgumentException("닉네임은 2자 이상 50자 이하여야 합니다");
            }
            if (!user.getNickname().equals(nickname) &&
                    userRepository.findByNickname(nickname).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다");
            }
            user.setNickname(nickname);
            user.setUsername(nickname);
        }

        // 휴대폰 번호 수정
        if (phone != null && !phone.trim().isEmpty()) {
            if (!phone.matches("^01[0-9]-?\\d{3,4}-?\\d{4}$")) {
                throw new IllegalArgumentException("올바른 휴대폰 번호 형식을 입력하세요");
            }
            user.setPhone(phone);
        }

        // 학년/학과 수정
        if (gradeYear != null && !gradeYear.trim().isEmpty()) {
            if (gradeYear.length() > 50) {
                throw new IllegalArgumentException("학년 정보는 50자 이하여야 합니다");
            }
            user.setGradeYear(gradeYear);
        }

        // 관심사 수정
        if (interests != null && !interests.trim().isEmpty()) {
            if (interests.length() > 500) {
                throw new IllegalArgumentException("관심사는 500자 이하여야 합니다");
            }
            user.setInterests(interests);
        }

        // 프로필 이미지 수정
        if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
            user.setProfileImageUrl(profileImageUrl);
        }

        user.setUpdatedAt(LocalDateTime.now());
        Users updatedUser = userRepository.save(user);

        log.info("✅ 프로필 종합 수정: userId={}", userId);
        return getProfile(updatedUser.getUserId());
    }

    /**
     * 이메일 인증 여부 조회
     *
     * @param userId 사용자 ID
     * @return 이메일 인증 여부
     */
    @Transactional(readOnly = true)
    public Boolean isEmailVerified(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return user.getEmailVerified();
    }

    /**
     * 학생 인증 여부 조회
     *
     * @param userId 사용자 ID
     * @return 학생 인증 여부
     */
    @Transactional(readOnly = true)
    public Boolean isStudentVerified(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return user.getIsStudentVerified();
    }

    /**
     * 사용자가 존재하는지 확인
     *
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * 닉네임 중복 확인
     *
     * @param nickname 닉네임
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     */
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    /**
     * 닉네임으로 사용자 조회
     *
     * @param nickname 닉네임
     * @return 사용자 정보 맵
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> getUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .map(user -> {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("userId", user.getUserId());
                    profile.put("nickname", user.getNickname());
                    profile.put("name", user.getName());
                    profile.put("profileImageUrl", user.getProfileImageUrl());
                    profile.put("role", user.getRole());
                    return profile;
                });
    }
}
