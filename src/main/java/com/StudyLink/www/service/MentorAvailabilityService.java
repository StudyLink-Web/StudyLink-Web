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

    @Transactional
    public MentorAvailability addAvailability(
            Long mentorId,
            Integer dayOfWeek,
            Integer block
    ) {
        Users mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!"MENTOR".equals(mentor.getRole())) {
            throw new IllegalArgumentException("멘토만 활동 시간을 설정할 수 있습니다.");
        }

        if (dayOfWeek < 0 || dayOfWeek > 6) {
            throw new IllegalArgumentException("요일은 0~6 사이의 값이어야 합니다.");
        }

        if (block < 0 || block > 11) {
            throw new IllegalArgumentException("블록은 0~11 사이의 값이어야 합니다.");
        }

        MentorAvailability availability = MentorAvailability.builder()
                .mentorId(mentorId)
                .dayOfWeek(dayOfWeek)
                .block(block)
                .build();

        return mentorAvailabilityRepository.save(availability);
    }

    @Transactional
    public MentorAvailability updateAvailability(
            Long availabilityId,
            Integer dayOfWeek,
            Integer block
    ) {
        MentorAvailability availability = mentorAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("활동 시간을 찾을 수 없습니다."));

        if (dayOfWeek < 0 || dayOfWeek > 6) {
            throw new IllegalArgumentException("요일은 0~6 사이의 값이어야 합니다.");
        }

        if (block < 0 || block > 11) {
            throw new IllegalArgumentException("블록은 0~11 사이의 값이어야 합니다.");
        }

        availability.setDayOfWeek(dayOfWeek);
        availability.setBlock(block);

        return mentorAvailabilityRepository.save(availability);
    }

    @Transactional(readOnly = true)
    public List<MentorAvailability> getMentorAvailabilities(Long mentorId) {
        return mentorAvailabilityRepository.findByMentorId(mentorId);
    }

    @Transactional(readOnly = true)
    public List<MentorAvailability> getMentorAvailabilitiesByDay(Long mentorId, Integer dayOfWeek) {
        return mentorAvailabilityRepository.findByMentorIdAndDayOfWeek(mentorId, dayOfWeek);
    }

    @Transactional
    public void deleteAvailability(Long availabilityId) {
        mentorAvailabilityRepository.deleteById(availabilityId);
    }
}
