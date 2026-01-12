package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import java.util.Optional;

public interface MyPageService {

    /**
     * 마이페이지 정보 조회
     */
    Optional<Users> getUserInfo(Long userId);

    /**
     * 프로필 정보 수정
     */
    Users updateProfile(Long userId, String university, String department, String studentYear);

    /**
     * 대학생 인증 시작 (이메일 발송)
     */
    void startVerification(Long userId, String studentEmail) throws Exception;

    /**
     * 대학생 인증 완료 (토큰 검증)
     */
    boolean completeVerification(String studentEmail, String token);

    /**
     * 인증 상태 조회
     */
    boolean isVerified(Long userId);

    /**
     * 인증 정보 조회
     */
    Users getVerificationInfo(Long userId);

    /**
     * 계정 설정 업데이트
     */
    Users updateSettings(Long userId, String password, String nickname);
}