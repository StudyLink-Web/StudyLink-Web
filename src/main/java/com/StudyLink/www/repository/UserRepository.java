package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    // 이메일로 사용자 조회
    Optional<Users> findByEmail(String email);
    // 닉네임으로 사용자 조회
    Optional<Users> findByNickname(String nickname);
    // 아이디(username)로 사용자 조회
    Optional<Users> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    // 추가: 학교 이메일 인증 관련 메서드
    Optional<Users> findBySchoolEmail(String schoolEmail);

    Optional<Users> findBySchoolEmailVerificationToken(String token);

    boolean existsBySchoolEmail(String schoolEmail);

    // ⭐ 추가: OAuth 로그인 관련 메서드
    Optional<Users> findByOauthIdAndOauthProvider(String oauthId, String oauthProvider);

    // 관리자페이지 통계용, 일일 신규 가입자 수
    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 관리자페이지 통계용, 누적 회원 수
    @Query("""
        select count(u)
        from Users u
        where u.createdAt < :start
    """)
    int countBefore(@Param("start") LocalDateTime start);

    // 관리자페이지 통계용, 일일 회원 수
    @Query("""
        select count(u)
        from Users u
        where u.createdAt >= :start
        and u.createdAt < :end
        """)
    int countByCreatedDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}