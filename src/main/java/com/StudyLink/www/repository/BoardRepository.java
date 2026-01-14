package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Board b set b.viewCount = b.viewCount + 1 where b.postId = :postId")
    int increaseViewCount(@Param("postId") Long postId);

    @Query("""
        select b
        from Board b
        where
          (:keyword is null or :keyword = ''
           or lower(b.title) like lower(concat('%', :keyword, '%'))
           or lower(b.content) like lower(concat('%', :keyword, '%'))
           or lower(b.writer) like lower(concat('%', :keyword, '%'))
          )
        """)
    Page<Board> search(@Param("keyword") String keyword,
                       Pageable pageable);
}
