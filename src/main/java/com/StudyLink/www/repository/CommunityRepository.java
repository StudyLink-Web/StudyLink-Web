package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    @Modifying
    @Query("update Community c set c.readCount = c.readCount + 1 where c.bno = :bno")
    void increaseReadCount(@Param("bno") Long bno);

    // ✅ 검색(제목/내용)
    @Query("""
        SELECT c
        FROM Community c
        WHERE c.title LIKE %:keyword%
           OR c.content LIKE %:keyword%
    """)
    Page<Community> search(@Param("keyword") String keyword, Pageable pageable);
}
