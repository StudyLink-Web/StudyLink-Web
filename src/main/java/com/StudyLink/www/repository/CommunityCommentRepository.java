package com.StudyLink.www.repository;

import com.StudyLink.www.entity.CommunityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    Page<CommunityComment> findAllByBno(Long bno, Pageable pageable);
    long countByBno(Long bno);
    void deleteAllByBno(Long bno);
}
