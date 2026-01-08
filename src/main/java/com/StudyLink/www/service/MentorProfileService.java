package com.StudyLink.www.service;

import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.MentorProfileRepository;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;

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

        if (univId != null) profile.setUnivId(univId);
        if (deptId != null) profile.setDeptId(deptId);
        if (introduction != null) profile.setIntroduction(introduction);

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
     * 모든 인증된 멘토 조회
     * ✅ List<MentorProfile> 타입 명시
     */
    @Transactional(readOnly = true)
    public List<MentorProfile> getVerifiedMentors() {
        return mentorProfileRepository.findByIsVerifiedTrue();  // ✅ 이제 메서드 존재
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
}
