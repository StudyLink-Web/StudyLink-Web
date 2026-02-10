package com.StudyLink.www.init;

import com.StudyLink.www.entity.File;
import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.entity.Role;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.service.FileStorageService;
import com.StudyLink.www.service.MentorProfileService;
import com.StudyLink.www.service.UserService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateMentor {
    private final UserService userService;
    private final MentorProfileService mentorProfileService;
    private final Random random = new Random();
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    // 더미 데이터용 리스트
    private final List<String> names = List.of("홍길동", "김철수", "이영희", "박민수", "최지우");
    private final List<String> nicknames = List.of("Hero", "Star", "Ace", "Moon", "Sky");
    private final List<String> emails = List.of("@naver.com", "@daum.com", "@google.com", "@gmail.com");
    private final List<String> usernames = List.of("hong", "kim", "lee", "park", "choi");
    private final List<String> passwords = List.of("1234");
    private final List<String> universities  = List.of("서울대학교", "연세대학교", "고려대학교", "성균관대학교", "한양대학교");
    private final List<String> majorList  = List.of("기계공학과", "경영학과", "정치외교학과", "물리학과", "수학과");
    private final List<String> fileNames = List.of(
            "profile1.png",
            "profile2.png",
            "profile3.png",
            "profile4.png",
            "profile5.png",
            "profile6.png",
            "profile7.png"
    );

    /*
    users 테이블 필수 입력 데이터
    email
    password
    name
    nickname
    username


    mentor_profile 테이블 필수 입력 데이터
    user
     */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createRandomMentor(Optional<String> id,
                                   Optional<String> pwd,
                                   Optional<Long> point) {
        boolean saved = false;
        int cnt = 0;
        while (!saved) {
            try {
                // 1️⃣ Users 생성
                String rawPassword  = pwd.orElse(randomFromList(passwords));
                long mentorPoint = point.orElse(0L);
                String email = id.orElse(generateRandomEmail());
                String username = randomFromList(usernames) + random.nextInt(10000);
                String nickname = randomFromList(nicknames) + random.nextInt(10000);
                String encodedPassword = passwordEncoder.encode(rawPassword);

                LocalDateTime createdAt = randomCreatedAt();

                Users user = Users.builder()
                        .email(email)
                        .password(encodedPassword)
                        .name(randomFromList(names))
                        .nickname(nickname)
                        .username(username)
                        .role(Role.MENTOR)
                        .createdAt(createdAt)
                        .updatedAt(createdAt)
                        .isStudentVerified(true)
                        .isVerifiedStudent(true)
                        .build();

                user = userService.saveUser(user); // DB에 저장 후 userId 생성

                // 2️⃣ MentorProfile 생성 (필수: user)
                MentorProfile mentorProfile = MentorProfile.builder()
                        .user(user) // 필수
                        .isVerified(false) // 기본값
                        .point(mentorPoint)       // 기본값
                        .exp(0L)         // 기본값
                        .notificationLesson(true)
                        .notificationMessage(true)
                        .notificationReview(true)
                        .lessonCount(0L)
                        .reviewCount(0L)
                        .university(randomFromList(universities))
                        .major(randomFromList(majorList))
                        .isVerified(true)
                        .averageRating(randomAverageRating())
                        .quizCount(randomQuizCount())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build();

                mentorProfileService.saveMentorProfile(mentorProfile);

                String selectedFileName = randomFromList(fileNames);

                // DB에 저장할 접근 경로
                String accessPath = "/uploads/profile/user-" + user.getUserId() + "/" + selectedFileName;
                user.setProfileImageUrl(accessPath);

                // 저장할 실제 디렉토리 생성
                Path saveDir = Path.of("uploads/profile/user-" + user.getUserId());
                Files.createDirectories(saveDir);

                // 더미 이미지 가져오기
                try (InputStream is = getClass().getClassLoader()
                        .getResourceAsStream("static/image/dummy-images/" + selectedFileName)) {

                    if (is == null) {
                        throw new RuntimeException("더미 이미지가 존재하지 않습니다: " + selectedFileName);
                    }

                    // 직접 구현한 MultipartFile 사용
                    // 파일에서 contentType 추출
                    String contentType = Files.probeContentType(Path.of("src/main/resources/static/image/dummy-images/" + selectedFileName));

                    MultipartFile multipartFile = new FileMultipart(selectedFileName, is, contentType);

                    // 실제 저장은 fileStorageService가 담당
                    String savePath = fileStorageService.saveProfileImage(multipartFile, user.getUserId());
                    System.out.println(">>> 이미지 저장 완료: " + accessPath);
                    user.setProfileImageUrl(savePath);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 처리 실패", e);
                }

                saved = true; // 성공하면 반복 종료
                System.out.println(">>> 더미 멘토 생성 완료: " + user.getUsername());

            } catch (Exception e) {
                // 중복키 등 DB 제약조건 오류 발생 시 재시도
                log.info(">>> 자동생성 오류 {}", e);
                cnt++;
                if (cnt > 10) {
                    return;
                }
            }
        }
    }

    // 랜덤 이메일 생성: 랜덤 문자열 + 리스트에서 도메인 선택
    private String generateRandomEmail() {
        String localPart = randomString(6); // 6자리 랜덤 문자열
        String domain = randomFromList(emails);
        return localPart + domain;
    }

    // 랜덤 문자열 생성 (영문 소문자 + 숫자)
    private String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 리스트에서 랜덤 값 뽑기
    private <T> T randomFromList(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    // 랜덤 생성일 생성 (오늘 기준 최근 30일 내)
    private LocalDateTime randomCreatedAt() {
        int daysBack = random.nextInt(30); // 0~29일 전
        int hours = random.nextInt(24);
        int minutes = random.nextInt(60);
        int seconds = random.nextInt(60);
        return LocalDateTime.now()
                .minusDays(daysBack)
                .minusHours(hours)
                .minusMinutes(minutes)
                .minusSeconds(seconds);
    }

    // 1. 랜덤 평점 생성 (1.0 ~ 5.0)
    private double randomAverageRating() {
        return 1.0 + random.nextDouble() * 4.0; // 1.0 ~ 5.0
    }

    // 2. 랜덤 푼 문제 수 생성 (0 ~ 100)
    private int randomQuizCount() {
        return random.nextInt(101); // 0~100
    }
}
