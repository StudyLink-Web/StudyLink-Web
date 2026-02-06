package com.StudyLink.www.service;

import com.StudyLink.www.dto.MentorProfileDTO;
import com.StudyLink.www.dto.UsersDTO;
import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.MentorProfileRepository;
import com.StudyLink.www.repository.RoomRepository;
import com.StudyLink.www.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;

    /**
     * 멘토 프로필 생성
     */
    @Transactional
    public MentorProfile createMentorProfile(Long userId, Long univId, Long deptId, String introduction) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!"MENTOR".equals(user.getRole())) {
            throw new IllegalArgumentException("멘토 역할만 프로필을 생성할 수 있습니다.");
        }

        if (mentorProfileRepository.existsByUser_UserId(userId)) {
            throw new IllegalArgumentException("이미 멘토 프로필이 존재합니다.");
        }

        MentorProfile profile = MentorProfile.builder()
                .user(user)
                .univId(univId)
                .deptId(deptId)
                .introduction(introduction)
                .averageRating(0.0)
                .point(0L)
                .isVerified(false)
                .build();

        return mentorProfileRepository.save(profile);
    }

    /**
     * 멘토 프로필 조회 (Optional 타입 반환)
     * ✅ Optional<MentorProfile> 타입 명시
     */
    @Transactional(readOnly = true)
    public Optional<MentorProfile> getMentorProfile(Long userId) {
        return mentorProfileRepository.findByUser_UserId(userId);
    }

    /**
     * 멘토 프로필 업데이트
     */
    @Transactional
    public MentorProfile updateMentorProfile(Long userId, Long univId, Long deptId, String introduction) {
        MentorProfile profile = mentorProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("멘토 프로필을 찾을 수 없습니다."));

        if (univId != null)
            profile.setUnivId(univId);
        if (deptId != null)
            profile.setDeptId(deptId);
        if (introduction != null)
            profile.setIntroduction(introduction);

        return mentorProfileRepository.save(profile);
    }

    /**
     * 멘토 프로필 삭제
     */
    @Transactional
    public void deleteMentorProfile(Long userId) {
        MentorProfile profile = mentorProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("멘토 프로필을 찾을 수 없습니다."));

        mentorProfileRepository.delete(profile);
    }

    /**
     * 모든 인증된 멘토 조회 (DTO 리스트 반환)
     */
    @Transactional(readOnly = true)
    public List<MentorProfileDTO> getVerifiedMentorDTOs() {
        log.info("📋 모든 인증된 멘토 목록 조회 (DTO)");
        List<MentorProfile> mentors = mentorProfileRepository.findByIsVerifiedTrue();

        return mentors.stream()
                .map(profile -> {
                    UsersDTO usersDTO = new UsersDTO(profile.getUser());
                    MentorProfileDTO dto = new MentorProfileDTO(profile, usersDTO);

                    // ⭐ 평점 소수점 첫째 자리까지만 제한 (반올림)
                    if (dto.getAverageRating() != null) {
                        double rounded = Math.round(dto.getAverageRating() * 10.0) / 10.0;
                        dto.setAverageRating(rounded);
                    }

                    // 이미지 경로 보정
                    if (dto.getProfileImageUrl() == null || dto.getProfileImageUrl().isEmpty()) {
                        dto.setProfileImageUrl("/img/default-profile.png");
                    }
                    return dto;
                })
                .toList();
    }

    /**
     * 메인 화면용 상위 멘토 목록 조회 (평점순)
     */
    @Transactional(readOnly = true)
    public List<MentorProfileDTO> getTopMentorDTOs(int limit) {
        log.info("🏠 메인 화면용 상위 멘토 조회: limit={}", limit);
        List<MentorProfile> mentors = mentorProfileRepository
                .findAllVerifiedMentorsOrderByRatingDesc(org.springframework.data.domain.PageRequest.of(0, limit));

        return mentors.stream()
                .map(profile -> {
                    UsersDTO usersDTO = new UsersDTO(profile.getUser());
                    MentorProfileDTO dto = new MentorProfileDTO(profile, usersDTO);

                    // ⭐ 평점 소수점 첫째 자리까지만 제한 (반올림)
                    if (dto.getAverageRating() != null) {
                        double rounded = Math.round(dto.getAverageRating() * 10.0) / 10.0;
                        dto.setAverageRating(rounded);
                    }

                    // 이미지 경로 보정 (프로필 이미지가 없으면 기본 이미지)
                    if (dto.getProfileImageUrl() == null || dto.getProfileImageUrl().isEmpty()) {
                        dto.setProfileImageUrl("/img/default-profile.png");
                    }
                    return dto;
                })
                .toList();
    }

    /**
     * 모든 미인증 멘토 조회 (선택사항)
     */
    @Transactional(readOnly = true)
    public List<MentorProfile> getUnverifiedMentors() {
        return mentorProfileRepository.findByIsVerifiedFalse();
    }

    /**
     * 멘토 인증
     */
    @Transactional
    public MentorProfile verifyMentor(Long userId) {
        MentorProfile profile = mentorProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("멘토 프로필을 찾을 수 없습니다."));

        profile.setIsVerified(true);
        return mentorProfileRepository.save(profile);
    }

    /**
     * 멘토 경험치 추가
     */
    @Transactional
    public MentorProfile addExp(Long userId, Long amount) {
        MentorProfile profile = mentorProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("멘토 프로필을 찾을 수 없습니다."));

        profile.setExp(profile.getExp() + amount);
        return mentorProfileRepository.save(profile);
    }

    /**
     * 멘토 포인트 추가
     */
    @Transactional
    public MentorProfile addPoint(Long userId, Long amount) {
        MentorProfile profile = mentorProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("멘토 프로필을 찾을 수 없습니다."));

        profile.setPoint(profile.getPoint() + amount);
        return mentorProfileRepository.save(profile);
    }

    /**
     * 멘토 프로필 저장/업데이트 (Controller에서 사용)
     */
    @Transactional
    public MentorProfile updateProfile(MentorProfile mentorProfile) {
        return mentorProfileRepository.save(mentorProfile);
    }

    /**
     * ⭐ 멘토 프로필 저장 (새로 생성할 때 사용)
     */
    @Transactional
    public MentorProfile saveMentorProfile(MentorProfile mentorProfile) {
        return mentorProfileRepository.save(mentorProfile);
    }

    /**
     * username으로 프로필 조회
     */
    @Transactional(readOnly = true)
    public Optional<MentorProfile> getMentorProfileByUsername(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return mentorProfileRepository.findByUser_UserId(user.getUserId());
    }

    /**
     * 비밀번호로 프로필 업데이트
     */
    @Transactional
    public void updateMentorProfileWithPassword(String username, MentorProfileDTO dto, MultipartFile profileImage) {
        log.info("📝 멘토 프로필 업데이트 시작: {}", username);

        try {
            log.debug("🔍 사용자 조회 중: {}", username);
            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
            log.debug("✅ 사용자 조회 완료: userId={}", user.getUserId());

            log.debug("🔍 멘토 프로필 조회 중: {}", user.getUserId());
            MentorProfile mentorProfile = mentorProfileRepository.findByUser_UserId(user.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("멘토 프로필을 찾을 수 없습니다"));
            log.debug("✅ 멘토 프로필 조회 완료");

            if (dto.getFirstName() != null && !dto.getFirstName().isEmpty()) {
                user.setName(dto.getFirstName());
            }
            if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
                user.setNickname(dto.getNickname());
            }
            // ✅ 전화번호 저장 정책
            // 1) 이미 DB에 phone이 있으면: 이 API(/mentor/update)에서는 변경 금지 (환경설정에서만 변경)
            // 2) DB에 phone이 없으면: 이번 요청에서 phoneVerified=true 일 때만 저장
            String currentPhone = user.getPhone();
            String newPhone = dto.getPhone();
            Boolean phoneVerified = dto.getPhoneVerified();

            if (currentPhone != null && !currentPhone.trim().isEmpty()) {
                // 이미 저장된 번호가 있으면 무시 (변경은 환경설정 API에서)
                log.info("📵 전화번호는 이미 저장되어 있어 /mentor/update 에서 변경 불가: userId={}", user.getUserId());
            } else {
                if (Boolean.TRUE.equals(phoneVerified) && newPhone != null && !newPhone.trim().isEmpty()) {
                    // (선택) 형식 검증까지 하고 싶으면 여기서 정규식 검사 가능
                    user.setPhone(newPhone);
                    log.info("✅ 전화번호 최초 저장 완료: userId={}, phone={}", user.getUserId(), newPhone);
                } else {
                    log.info("📵 전화번호 인증 미완료로 저장 무시: userId={}", user.getUserId());
                }
            }

            if (profileImage != null && !profileImage.isEmpty()) {
                log.info("📸 프로필 이미지 처리 시작: size={} bytes", profileImage.getSize());
                try {
                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        log.debug("🗑️  기존 이미지 삭제: {}", user.getProfileImageUrl());
                        fileStorageService.deleteProfileImage(user.getUserId());
                    }
                    log.debug("💾 새 이미지 저장 중...");
                    String imageUrl = fileStorageService.saveProfileImage(profileImage, user.getUserId());
                    user.setProfileImageUrl(imageUrl);
                    log.info("✅ 프로필 이미지 저장 완료: {}", imageUrl);
                } catch (IOException e) {
                    log.error("❌ 이미지 저장 실패: {}", e.getMessage(), e);
                    throw new RuntimeException("이미지 저장 중 오류가 발생했습니다: " + e.getMessage());
                }
            }

            if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
                log.debug("🔒 비밀번호 변경 중...");
                validatePasswordChange(user, dto);
                user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                log.info("✅ 비밀번호 변경 완료");
            }

            if (dto.getBio() != null && !dto.getBio().isEmpty()) {
                mentorProfile.setIntroduction(dto.getBio());
            }

            if (dto.getUniversity() != null && !dto.getUniversity().isEmpty()) {
                mentorProfile.setUniversity(dto.getUniversity());
            }
            if (dto.getMajor() != null && !dto.getMajor().isEmpty()) {
                mentorProfile.setMajor(dto.getMajor());
            }
            if (dto.getEntranceYear() != null) {
                mentorProfile.setEntranceYear(dto.getEntranceYear());
            }
            if (dto.getGraduationYear() != null) {
                mentorProfile.setGraduationYear(dto.getGraduationYear());
            }
            if (dto.getCredentials() != null && !dto.getCredentials().isEmpty()) {
                mentorProfile.setCredentials(dto.getCredentials());
            }

            if (dto.getSubjects() != null && !dto.getSubjects().isEmpty()) {
                mentorProfile.setSubjects(dto.getSubjects());
            }
            if (dto.getGrades() != null && !dto.getGrades().isEmpty()) {
                mentorProfile.setGrades(dto.getGrades());
            }
            if (dto.getPricePerHour() != null) {
                mentorProfile.setPricePerHour(dto.getPricePerHour());
            }
            if (dto.getMinLessonHours() != null) {
                mentorProfile.setMinLessonHours(dto.getMinLessonHours());
            }
            if (dto.getLessonType() != null && !dto.getLessonType().isEmpty()) {
                mentorProfile.setLessonType(dto.getLessonType());
            }
            if (dto.getLessonLocation() != null && !dto.getLessonLocation().isEmpty()) {
                mentorProfile.setLessonLocation(dto.getLessonLocation());
            }
            if (dto.getAvailableTime() != null && !dto.getAvailableTime().isEmpty()) {
                mentorProfile.setAvailableTime(dto.getAvailableTime());
            }

            if (dto.getNotificationLesson() != null) {
                mentorProfile.setNotificationLesson(dto.getNotificationLesson());
            }
            if (dto.getNotificationMessage() != null) {
                mentorProfile.setNotificationMessage(dto.getNotificationMessage());
            }
            if (dto.getNotificationReview() != null) {
                mentorProfile.setNotificationReview(dto.getNotificationReview());
            }

            mentorProfile.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            log.debug("💾 사용자 저장 중...");
            userRepository.save(user);
            log.debug("💾 멘토 프로필 저장 중...");
            mentorProfileRepository.save(mentorProfile);

            log.info("✅ 멘토 프로필 업데이트 완료: userId={}", user.getUserId());

        } catch (IllegalArgumentException e) {
            log.error("❌ 유효성 검사 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ 프로필 업데이트 실패: {}", e.getMessage(), e);
            throw new RuntimeException("프로필 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 프로필 정보만 업데이트 (이미지 없음)
     */
    @Transactional
    public void updateMentorProfileWithoutImage(String username, MentorProfileDTO dto) {
        updateMentorProfileWithPassword(username, dto, null);
    }

    /**
     * 비밀번호 변경 유효성 검사
     */
    private void validatePasswordChange(Users user, MentorProfileDTO dto) {
        if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isEmpty()) {
            throw new IllegalArgumentException("현재 비밀번호를 입력해주세요");
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().isEmpty()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요");
        }

        if (dto.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 최소 8자 이상이어야 합니다");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
    }

    /**
     * 멘토 프로필 조회 (통계 포함)
     * 수업 횟수와 리뷰 개수를 동적으로 계산
     * ⭐ Optional<MentorProfile> 타입으로 변경 (MentorProfileController에서 사용)
     */
    @Transactional(readOnly = true)
    public Optional<MentorProfile> getMentorProfileWithStats(Long userId) {
        log.info("📊 멘토 프로필 조회 (통계 포함): userId={}", userId);

        Optional<MentorProfile> mentorOpt = mentorProfileRepository.findByUser_UserId(userId);

        if (mentorOpt.isEmpty()) {
            log.warn("⚠️ 멘토 프로필을 찾을 수 없습니다: userId={}", userId);
            return Optional.empty();
        }

        MentorProfile mentor = mentorOpt.get();

        // 수업 횟수 계산 (완료된 수업만)
        long lessonCount = mentorProfileRepository.countLessonsByMentorId(userId);
        mentor.setLessonCount(lessonCount);

        // 리뷰 개수 계산
        long reviewCount = mentorProfileRepository.countReviewsByMentorId(userId);
        mentor.setReviewCount(reviewCount);

        log.debug("✅ 통계: lessonCount={}, reviewCount={}", lessonCount, reviewCount);

        return Optional.of(mentor);
    }

    /**
     * username으로 프로필 조회 (통계 포함)
     */
    @Transactional(readOnly = true)
    public Optional<MentorProfile> getMentorProfileWithStatsByUsername(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return getMentorProfileWithStats(user.getUserId());
    }

    @Transactional
    public void plusQuizCount(Long mentorId) {
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 멘토가 없습니다."));
        profile.setQuizCount(profile.getQuizCount() + 1);
    }

    @Transactional
    public void updateAverageRating(Long mentorId) {

        // DB에서 현재 평균 평점 조회
        Double avg = roomRepository.findAverageRatingByMentor(mentorId);

        // ⭐ 평점 소수점 첫째 자리까지만 제한 (반올림)
        double average = (avg != null) ? Math.round(avg * 10.0) / 10.0 : 0.0;

        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 멘토가 없습니다."));

        profile.setAverageRating(average);
    }

    public long getPoint(Long userId) {
        MentorProfile profile = mentorProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 멘토가 없습니다."));
        return profile.getPoint();
    }
}