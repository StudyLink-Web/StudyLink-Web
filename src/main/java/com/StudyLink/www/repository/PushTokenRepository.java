package com.StudyLink.www.repository;

import com.StudyLink.www.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    Optional<PushToken> findByToken(String token);
    java.util.List<PushToken> findAllByUsername(String username);
    void deleteByToken(String token);
}
