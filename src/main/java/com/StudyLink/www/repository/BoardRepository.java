package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/* JPA 기능을 처리하는 인터페이스 */
public interface BoardRepository
        extends JpaRepository<Board, Long>, BoardCustomeRepository {

    /* =========================
     *  조회수 증가
     * ========================= */

    // ✅ 조회수 +1 (원자적 증가)
    @Modifying
    @Query("update Board b set b.viewCount = b.viewCount + 1 where b.postId = :postId")
    int increaseViewCount(@Param("postId") Long postId);

    /* =========================
     *  검색 + 페이징
     * ========================= */

    /**
     * 검색 (제목 / 내용 / 작성자)
     * keyword가 포함된 게시글 조회
     */
    @Query("""
        select b
        from Board b
        where
              (:keyword is null or :keyword = ''
               or lower(b.title)   like lower(concat('%', :keyword, '%'))
               or lower(b.content) like lower(concat('%', :keyword, '%'))
               or lower(b.writer)  like lower(concat('%', :keyword, '%'))
              )
        """)
    Page<Board> search(@Param("keyword") String keyword, Pageable pageable);
}
