package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
}
