package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    /* 답변 등록 */
    @Modifying
    @Query("""
        update Inquiry i
        set i.adminContent = :adminContent,
            i.status = 'COMPLETE',
            i.answerAt = CURRENT_TIMESTAMP
        where i.qno = :qno
    """)
    void answer(@Param("qno") Long qno,
                @Param("adminContent") String adminContent);

    /* BCrypt 검증용: 저장된 비밀번호만 조회 */
    @Query("select i.password from Inquiry i where i.qno = :qno")
    Optional<String> findPasswordByQno(@Param("qno") Long qno);

    /* 평문 저장일 때만 */
    boolean existsByQnoAndPassword(Long qno, String password);

    /* 검색 */
    @Query("""
        select i from Inquiry i
        where (:category is null or :category = '' or i.category = :category)
          and (:status is null or :status = '' or i.status = :status)
          and (:keyword is null or :keyword = ''
               or lower(i.title) like lower(concat('%', :keyword, '%'))
               or lower(i.userContent) like lower(concat('%', :keyword, '%')))
    """)
    Page<Inquiry> search(@Param("category") String category,
                         @Param("status") String status,
                         @Param("keyword") String keyword,
                         Pageable pageable);
}
