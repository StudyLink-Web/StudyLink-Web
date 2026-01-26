package com.StudyLink.www.repository;

import com.StudyLink.www.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 이메일 인증 토큰 Repository
 *
 * 주요 메서드:
 * - findByEmailAndRequestedUsername: 이메일 + 계정명으로 토큰 조회
 * - deleteByRequestedUsername: 특정 계정의 모든 토큰 삭제
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /**
     * ⭐ 이메일 + 요청한 계정명으로 토큰 조회
     *
     * 예시:
     * - findByEmailAndRequestedUsername("user@ewha.ac.kr", "accountA")
     *
     * @param email 이메일
     * @param requestedUsername 요청한 계정명
     * @return Optional 토큰
     */
    Optional<EmailVerificationToken> findByEmailAndRequestedUsername(
            String email,
            String requestedUsername
    );

    /**
     * 특정 계정의 모든 토큰 삭제
     * 새로운 인증 요청 시 이전 토큰을 삭제하기 위해 사용
     *
     * 예시:
     * - deleteByRequestedUsername("accountA")
     *
     * @param requestedUsername 요청한 계정명
     */
    void deleteByRequestedUsername(String requestedUsername);

    /**
     * 이메일로 토큰 조회 (모든 계정)
     * 이메일이 이미 인증되었는지 확인할 때 사용
     *
     * @param email 이메일
     * @return Optional 토큰
     */
    Optional<EmailVerificationToken> findByEmail(String email);
}
