package com.StudyLink.www.repository;

import com.StudyLink.www.entity.MentorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, Long> {

    List<MentorAvailability> findByMentorId(Long mentorId);

    List<MentorAvailability> findByMentorIdAndDayOfWeek(Long mentorId, Integer dayOfWeek);

    List<MentorAvailability> findByMentorIdAndDayOfWeekAndBlock(Long mentorId, Integer dayOfWeek, Integer block);
}
