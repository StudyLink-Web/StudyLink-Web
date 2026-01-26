package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    @Modifying
    @Query("update Community c set c.readCount = c.readCount + 1 where c.bno = :bno")
    void increaseReadCount(@Param("bno") Long bno);
}
