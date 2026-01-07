package com.StudyLink.www.service;

import com.StudyLink.www.entity.StudentProfile;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.StudentProfileRepository;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudentProfile createStudentProfile(Long userId, String targetUniversity,
                                               String targetMajor, String regionPreference) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!"STUDENT".equals(user.getRole())) {
            throw new IllegalArgumentException("학생 역할만 프로필을 생성할 수 있습니다.");
        }

        if (studentProfileRepository.existsByUser_UserId(userId)) {
            throw new IllegalArgumentException("이미 학생 프로필이 존재합니다.");
        }

        StudentProfile profile = StudentProfile.builder()
                .user(user)
                .targetUniversity(targetUniversity)
                .targetMajor(targetMajor)
                .regionPreference(regionPreference)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return studentProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Optional<StudentProfile> getStudentProfile(Long userId) {  // ✅ Optional<StudentProfile> 추가
        return studentProfileRepository.findByUser_UserId(userId);
    }

    @Transactional
    public StudentProfile updateStudentProfile(Long userId, String targetUniversity,
                                               String targetMajor, String regionPreference) {
        StudentProfile profile = studentProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("학생 프로필을 찾을 수 없습니다."));

        if (targetUniversity != null) profile.setTargetUniversity(targetUniversity);
        if (targetMajor != null) profile.setTargetMajor(targetMajor);
        if (regionPreference != null) profile.setRegionPreference(regionPreference);

        profile.setUpdatedAt(LocalDateTime.now());

        return studentProfileRepository.save(profile);
    }

    @Transactional
    public void deleteStudentProfile(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("학생 프로필을 찾을 수 없습니다."));

        studentProfileRepository.delete(profile);
    }
}
