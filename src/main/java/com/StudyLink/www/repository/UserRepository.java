package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Role;
import com.StudyLink.www.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    Optional<Users> findByNickname(String nickname);

    Optional<Users> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<Users> findBySchoolEmail(String schoolEmail);

    Optional<Users> findBySchoolEmailVerificationToken(String token);

    boolean existsBySchoolEmail(String schoolEmail);

    Optional<Users> findByOauthIdAndOauthProvider(String oauthId, String oauthProvider);

    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
        select count(u)
        from Users u
        where u.createdAt < :start
    """)
    int countBefore(@Param("start") LocalDateTime start);

    @Query("""
        select count(u)
        from Users u
        where u.createdAt >= :start
          and u.createdAt < :end
    """)
    int countByCreatedDate(@Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    @Query("""
        select u
        from Users u
        where (:email is null or u.email like concat('%', :email, '%'))
          and (:role is null or u.role = :role)
          and (:isActive is null or u.isActive = :isActive)
          and (:start is null or u.createdAt >= :start)
          and (:end is null or u.createdAt < :end)
    """)
    Page<Users> searchByCreatedAt(
            @Param("email") String email,
            @Param("role") Role role,
            @Param("isActive") Boolean isActive,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}
