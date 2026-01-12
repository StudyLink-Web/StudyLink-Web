package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    // 기존 메서드들
    // 이메일로 사용자 조회
    Optional<Users> findByEmail(String email);

    // 닉네임으로 사용자 조회
    Optional<Users> findByNickname(String nickname);

    // 아이디(username)로 사용자 조회
    Optional<Users> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    // ========== 대학생 인증 관련 메서드 ==========

    /**
     * 학교 이메일로 사용자 조회
     * @param studentEmail 학교 이메일 (예: 22101@ewhain.ewha.ac.kr)
     * @return 해당 학교 이메일을 가진 사용자
     */
    Optional<Users> findByStudentEmail(String studentEmail);

    /**
     * 학교 이메일과 인증 토큰으로 사용자 조회
     * @param studentEmail 학교 이메일
     * @param verificationToken 인증 토큰
     * @return 일치하는 사용자
     */
    Optional<Users> findByStudentEmailAndVerificationToken(String studentEmail, String verificationToken);

    /**
     * 학교 이메일이 존재하는지 확인
     * @param studentEmail 학교 이메일
     * @return 존재 여부
     */
    boolean existsByStudentEmail(String studentEmail);

    /**
     * 대학생 인증 완료한 사용자 조회
     * @param isStudentVerified true면 인증 완료
     * @return 인증 완료한 사용자 목록
     */
    // List<Users> findByIsStudentVerified(Boolean isStudentVerified); // 필요시 활용
}