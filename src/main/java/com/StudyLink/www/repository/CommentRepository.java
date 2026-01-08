package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<Entity, Id class>
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // bno -> postId 로 통일
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    long countByPostId(Long postId);
}
