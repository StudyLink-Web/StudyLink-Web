package com.StudyLink.www.service;

import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.MentorProfileRepository;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;  // ✅ mentor_profile_repository → mentorProfileRepository
    private final UserRepository userRepository;  // ✅ user_repository → userRepository

    @Transactional
    public MentorProfile createMentorProfile(Long userId, Long univId, Long deptId, String introduction) {  // ✅ snake_case → camelCase
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!"MENTOR".equals(user.getRole())) {
            throw new IllegalArgumentException("멘토 역할만 프로필을 생성할 수 있습니다.");
        }

        if (mentorProfileRepository.existsByUser_UserId(userId)) {  // ✅ existsByUser_user_id() → existsByUser_UserId()
            throw new IllegalArgumentException("이미 멘토 프로필이 존재합니다.");
        }

        MentorProfile profile = MentorProfile.builder()
                .user(user)
                .univId(univId)  // ✅ univ_id → univId
                .deptId(deptId)  // ✅ dept_id → deptId
                .introduction(introduction)
                .averageRating(BigDecimal.ZERO)  // ✅ average_rating → averageRating
                .point(0)
                .isVerified(false)  // ✅ is_verified → isVerified
                .build();

        return mentorProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Optional<MentorProfile> getMentorProfile(Long userId) {  // ✅ user_id → userId
        return mentorProfileRepository.findByUser_UserId(userId);  // ✅ findByUser_user_id() → findByUser_UserId()
    }

    @Transactional
    public MentorProfile updateMentorProfile(Long userId, Long univId, Long deptId, String introduction) {  // ✅ snake_case → camelCase
        MentorProfile profile = mentorProfileRepository.findByUser_UserId(userId)  // ✅ findByUser_user_id() → findByUser_UserId()
                .orElseThrow(() -> new IllegalArgumentException("멘토 프로필을 찾을 수 없습니다."));

        if (univId != null) profile.setUnivId(univId);  // ✅ setUniv_id() → setUnivId()
        if (deptId != null) profile.setDeptId(deptId);  // ✅ setDept_id() → setDeptId()
        if (introduction != null) profile.setIntroduction(introduction);

        return mentorProfileRepository.save(profile);
    }

    @Transactional
    public void deleteMentorProfile(Long userId) {  // ✅ user_id → userId
        MentorProfile profile = mentorProfileRepository.findByUser_UserId(userId)  // ✅ findByUser_user_id() → findByUser_UserId()
                .orElseThrow(() -> new IllegalArgumentException("멘토 프로필을 찾을 수 없습니다."));

        mentorProfileRepository.delete(profile);
    }
}
