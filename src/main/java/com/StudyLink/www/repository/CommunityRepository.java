package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    Page<Community> findByBno(Long bno, Pageable pageable);

    long countByBno(Long bno);
}
