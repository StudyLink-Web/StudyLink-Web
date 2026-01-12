package com.StudyLink.www.repository;

import com.StudyLink.www.entity.MentorAvailability;
import com.StudyLink.www.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, Long> {

    /**
     * 특정 멘토의 활동 가능 시간 목록 조회
     */
    List<MentorAvailability> findByMentor(Users mentor);

    /**
     * 특정 멘토의 특정 요일 활동 가능 시간 목록 조회
     */
    List<MentorAvailability> findByMentorAndDayOfWeek(Users mentor, Integer dayOfWeek);

    /**
     * 특정 멘토의 특정 요일 특정 블록 활동 가능 시간 조회
     */
    List<MentorAvailability> findByMentorAndDayOfWeekAndBlock(Users mentor, Integer dayOfWeek, Integer block);
}
