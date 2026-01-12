package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 대학생 이메일 인증 서비스 인터페이스
 * 학교 이메일로 인증 링크를 발송하고 검증합니다.
 */
public interface EmailVerificationService {

    /**
     * 대학생 인증 이메일 발송
     * @param studentEmail 학교 이메일 (예: 22101@ewhain.ewha.ac.kr)
     * @param user 사용자 정보
     * @return 생성된 인증 토큰
     * @throws Exception 이메일 발송 실패 시
     */
    String sendVerificationEmail(String studentEmail, Users user) throws Exception;

    /**
     * 인증 링크 생성
     * @param studentEmail 학교 이메일
     * @param token 인증 토큰
     * @return 인증 링크 (전체 URL)
     */
    String generateVerificationLink(String studentEmail, String token);

    /**
     * 인증 토큰 생성
     * @return 일회용 토큰 (UUID)
     */
    String generateVerificationToken();

    /**
     * 토큰 만료 여부 확인
     * @param expiryTime 토큰 만료 시간
     * @return true면 만료됨, false면 유효함
     */
    boolean isTokenExpired(LocalDateTime expiryTime);

    /**
     * 인증 토큰과 이메일로 사용자 조회
     * @param studentEmail 학교 이메일
     * @param token 인증 토큰
     * @return 사용자 Optional
     */
    Optional<Users> findByEmailAndToken(String studentEmail, String token);
}