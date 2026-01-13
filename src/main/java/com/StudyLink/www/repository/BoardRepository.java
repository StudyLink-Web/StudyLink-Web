package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Board;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/* JPA 기능을 처리하는 인터페이스 */
public interface BoardRepository extends JpaRepository<Board, Long>, BoardCustomeRepository {

    // ✅ 조회수 +1 (원자적 증가)
    @Modifying
    @Query("update Board b set b.viewCount = b.viewCount + 1 where b.postId = :postId")
    int increaseViewCount(@Param("postId") Long postId);
}
