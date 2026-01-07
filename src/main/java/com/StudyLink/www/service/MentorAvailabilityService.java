package com.StudyLink.www.service;

import com.StudyLink.www.entity.MentorAvailability;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.MentorAvailabilityRepository;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorAvailabilityService {

    private final MentorAvailabilityRepository mentorAvailabilityRepository;
    private final UserRepository userRepository;

    /**
     * 멘토 활동 가능 시간 추가
     */
    @Transactional
    public MentorAvailability addAvailability(Long mentorId, Integer dayOfWeek, Integer block) {
        // 1. 멘토 사용자 확인
        Users mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        // 2. 입력값 검증
        validateInput(dayOfWeek, block);

        // 3. 중복 확인 (같은 요일과 시간 블록이 이미 존재하는지)
        List<MentorAvailability> existingAvailabilities =
                mentorAvailabilityRepository.findByMentorAndDayOfWeekAndBlock(mentor, dayOfWeek, block);
        if (!existingAvailabilities.isEmpty()) {
            throw new IllegalArgumentException("이미 설정된 활동 가능 시간입니다.");
        }

        // 4. 활동 가능 시간 생성 및 저장
        MentorAvailability availability = MentorAvailability.builder()
                .mentor(mentor)
                .dayOfWeek(dayOfWeek)
                .block(block)
                .build();

        MentorAvailability savedAvailability = mentorAvailabilityRepository.save(availability);
        log.info("활동 가능 시간 추가: 멘토 ID={}, 요일={}, 블록={}", mentorId, dayOfWeek, block);

        return savedAvailability;
    }

    /**
     * 멘토 활동 가능 시간 제거 (availId로)
     * ✅ 추가됨
     */
    @Transactional
    public void removeAvailability(Long availId) {
        // 1. 활동 가능 시간 확인
        MentorAvailability availability = mentorAvailabilityRepository.findById(availId)
                .orElseThrow(() -> new IllegalArgumentException("활동 가능 시간을 찾을 수 없습니다."));

        // 2. 삭제
        mentorAvailabilityRepository.delete(availability);
        log.info("활동 가능 시간 제거: availId={}", availId);
    }

    /**
     * 멘토의 활동 가능 시간 목록 조회
     */
    @Transactional(readOnly = true)
    public List<MentorAvailability> getAvailabilityByMentor(Long mentorId) {
        Users mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        return mentorAvailabilityRepository.findByMentor(mentor);
    }

    /**
     * 특정 요일의 멘토 활동 가능 시간 조회
     */
    @Transactional(readOnly = true)
    public List<MentorAvailability> getAvailabilityByMentorAndDay(Long mentorId, Integer dayOfWeek) {
        Users mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        validateDayOfWeek(dayOfWeek);

        return mentorAvailabilityRepository.findByMentorAndDayOfWeek(mentor, dayOfWeek);
    }

    /**
     * 특정 요일과 시간 블록의 멘토 활동 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isAvailable(Long mentorId, Integer dayOfWeek, Integer block) {
        Users mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        validateInput(dayOfWeek, block);

        List<MentorAvailability> availabilities =
                mentorAvailabilityRepository.findByMentorAndDayOfWeekAndBlock(mentor, dayOfWeek, block);

        return !availabilities.isEmpty();
    }

    /**
     * 입력값 검증 (요일과 블록)
     */
    private void validateInput(Integer dayOfWeek, Integer block) {
        validateDayOfWeek(dayOfWeek);
        validateBlock(block);
    }

    /**
     * 요일 검증
     */
    private void validateDayOfWeek(Integer dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek < 0 || dayOfWeek > 6) {
            throw new IllegalArgumentException("요일은 0~6 사이의 값이어야 합니다. (0: 일요일, 6: 토요일)");
        }
    }

    /**
     * 시간 블록 검증
     */
    private void validateBlock(Integer block) {
        if (block == null || block < 0 || block > 11) {
            throw new IllegalArgumentException("블록은 0~11 사이의 값이어야 합니다.");
        }
    }
}
