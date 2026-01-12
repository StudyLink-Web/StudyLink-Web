package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 마이페이지 서비스 구현
 * 사용자 프로필, 대학생 인증, 계정 설정 관리
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MyPageServiceImpl implements MyPageService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 마이페이지 정보 조회
     */
    @Override
    public Optional<Users> getUserInfo(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 프로필 정보 수정 (대학, 학과, 학년)
     */
    @Override
    @Transactional
    public Users updateProfile(Long userId, String university, String department, String studentYear) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setUniversity(university);
        user.setDepartment(department);
        user.setStudentYear(studentYear);

        log.info("✅ 프로필 수정 완료: user_id={}, university={}, department={}", userId, university, department);

        return userRepository.save(user);
    }

    /**
     * 대학생 인증 시작 (이메일 발송)
     */
    @Override
    @Transactional
    public void startVerification(Long userId, String studentEmail) throws Exception {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 인증된 경우
        if (user.getIsStudentVerified()) {
            throw new IllegalArgumentException("이미 대학생 인증을 완료했습니다.");
        }

        // 이메일 발송
        emailVerificationService.sendVerificationEmail(studentEmail, user);

        log.info("✅ 인증 이메일 발송 완료: user_id={}, student_email={}", userId, studentEmail);
    }

    /**
     * 대학생 인증 완료 (토큰 검증)
     */
    @Override
    @Transactional
    public boolean completeVerification(String studentEmail, String token) {
        // 이메일과 토큰으로 사용자 찾기
        Optional<Users> userOpt = userRepository.findByStudentEmailAndVerificationToken(studentEmail, token);

        if (userOpt.isEmpty()) {
            log.warn("❌ 인증 실패: 일치하는 사용자 없음");
            return false;
        }

        Users user = userOpt.get();

        // 토큰 만료 여부 확인
        if (emailVerificationService.isTokenExpired(user.getVerificationTokenExpiry())) {
            log.warn("❌ 인증 실패: 토큰 만료됨");
            return false;
        }

        // 인증 완료 처리
        user.setIsStudentVerified(true);
        user.setVerifiedAt(LocalDateTime.now());
        user.setVerificationToken(null);  // 토큰 삭제
        user.setVerificationTokenExpiry(null);

        userRepository.save(user);

        log.info("✅ 대학생 인증 완료: student_email={}", studentEmail);

        return true;
    }

    /**
     * 인증 상태 조회
     */
    @Override
    public boolean isVerified(Long userId) {
        Optional<Users> user = userRepository.findById(userId);
        return user.map(Users::getIsStudentVerified).orElse(false);
    }

    /**
     * 인증 정보 조회
     */
    @Override
    public Users getVerificationInfo(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 계정 설정 업데이트 (비밀번호, 닉네임)
     */
    @Override
    @Transactional
    public Users updateSettings(Long userId, String password, String nickname) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 변경 (null이 아닌 경우만)
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        // 닉네임 변경 (null이 아닌 경우만)
        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }

        log.info("✅ 계정 설정 업데이트 완료: user_id={}", userId);

        return userRepository.save(user);
    }
}