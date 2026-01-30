package com.StudyLink.www.controller;

import com.StudyLink.www.dto.MentorProfileDTO;
import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.MentorProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Firebase 설정값 추가
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mentor")
public class MentorProfileController {

    private final MentorProfileService mentorProfileService;
    private final UserRepository userRepository;

    /**
     * Authentication에서 Users 엔티티 추출
     */
    private Users extractUser(Authentication authentication) {
        String username = authentication.getName();
        log.info("로그인 사용자: {}", username);

        return userRepository.findByUsername(username)
                .orElseGet(() ->
                        userRepository.findByEmail(username)
                                .orElseThrow(() ->
                                        new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username)
                                )
                );
    }

    /**
     * 멘토 프로필 수정 페이지 (GET)
     */
    @GetMapping("/edit-profile")
    public String editProfile(Authentication authentication, Model model) {
        log.info("✅ 멘토 프로필 수정 페이지 접근");
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                Users currentUser = extractUser(authentication);
                log.info("사용자 조회 성공 - userId: {}, email: {}", currentUser.getUserId(), currentUser.getEmail());

                // Optional 처리 + 없으면 자동 생성
                MentorProfile mentor = mentorProfileService.getMentorProfileWithStats(currentUser.getUserId())
                        .orElseGet(() -> {
                            log.info("⚠️ 멘토 프로필이 없어서 새로 생성합니다. userId: {}", currentUser.getUserId());
                            MentorProfile newMentor = new MentorProfile();
                            newMentor.setUser(currentUser);
                            newMentor.setLessonCount(0L);
                            newMentor.setReviewCount(0L);
                            newMentor.setAverageRating(0.0);
                            return mentorProfileService.saveMentorProfile(newMentor);
                        });

                model.addAttribute("mentor", mentor);
                model.addAttribute("user", currentUser);
                log.info("✅ 멘토 프로필 조회/생성 완료 - lessonCount: {}, reviewCount: {}",
                        mentor.getLessonCount(), mentor.getReviewCount());
            } catch (Exception e) {
                log.error("❌ 프로필 조회 실패: {}", e.getMessage(), e);
                return "redirect:/";
            }
        }
        return "mentor/mentor-profile";
    }


    /**
     * 다른 멘토 프로필 조회 (옵션)
     */
    @GetMapping("/{mentorId}")
    public String viewProfile(@PathVariable Long mentorId, Model model) {
        log.info("✅ 멘토 프로필 조회: {}", mentorId);
        // 특정 멘토의 프로필 조회
        // model.addAttribute("mentor", mentorProfileService.getMentorById(mentorId));
        return "mentor/view-profile";
    }

    /**
     * 프로필 저장 (POST) - FormData + 파일 지원
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "university", required = false) String university,
            @RequestParam(value = "major", required = false) String major,
            @RequestParam(value = "entranceYear", required = false) String entranceYear,
            @RequestParam(value = "graduationYear", required = false) String graduationYear,
            @RequestParam(value = "credentials", required = false) String credentials,
            @RequestParam(value = "subjects", required = false) List<String> subjects,
            @RequestParam(value = "grades", required = false) List<String> grades,
            @RequestParam(value = "pricePerHour", required = false) String pricePerHour,
            @RequestParam(value = "minLessonHours", required = false) String minLessonHours,
            @RequestParam(value = "lessonType", required = false) String lessonType,
            @RequestParam(value = "lessonLocation", required = false) String lessonLocation,
            @RequestParam(value = "availableTime", required = false) String availableTime,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            @RequestParam(value = "notificationLesson", required = false) Boolean notificationLesson,
            @RequestParam(value = "notificationMessage", required = false) Boolean notificationMessage,
            @RequestParam(value = "notificationReview", required = false) Boolean notificationReview,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            Authentication authentication) {

        log.info("📝 멘토 프로필 업데이트 API 요청");

        Map<String, Object> response = new HashMap<>();

        try {
            // 1️⃣ 인증 확인
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("❌ 인증되지 않은 사용자");
                response.put("error", "인증이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String username = authentication.getName();
            log.info("✅ 사용자 인증 완료: {}", username);

            // 2️⃣ 안전한 숫자 파싱 (NumberFormatException 방지)
            Integer entranceYearInt = null;
            if (entranceYear != null && !entranceYear.isEmpty() && !entranceYear.trim().isEmpty()) {
                try {
                    entranceYearInt = Integer.parseInt(entranceYear);
                } catch (NumberFormatException e) {
                    log.warn("⚠️ 입학년도 파싱 실패: {}", entranceYear);
                }
            }

            Integer graduationYearInt = null;
            if (graduationYear != null && !graduationYear.isEmpty() && !graduationYear.trim().isEmpty()) {
                try {
                    graduationYearInt = Integer.parseInt(graduationYear);
                } catch (NumberFormatException e) {
                    log.warn("⚠️ 졸업년도 파싱 실패: {}", graduationYear);
                }
            }

            Integer pricePerHourInt = null;
            if (pricePerHour != null && !pricePerHour.isEmpty() && !pricePerHour.trim().isEmpty()) {
                try {
                    pricePerHourInt = Integer.parseInt(pricePerHour);
                } catch (NumberFormatException e) {
                    log.warn("⚠️ 시급 파싱 실패: {}", pricePerHour);
                }
            }

            Double minLessonHoursDouble = null;
            if (minLessonHours != null && !minLessonHours.isEmpty() && !minLessonHours.trim().isEmpty()) {
                try {
                    minLessonHoursDouble = Double.parseDouble(minLessonHours);
                } catch (NumberFormatException e) {
                    log.warn("⚠️ 최소수업시간 파싱 실패: {}", minLessonHours);
                }
            }

            // 3️⃣ DTO 생성
            MentorProfileDTO mentorDTO = MentorProfileDTO.builder()
                    .firstName(firstName)
                    .nickname(nickname)
                    .phone(phone)
                    .bio(bio)
                    .university(university)
                    .major(major)
                    .entranceYear(entranceYearInt)
                    .graduationYear(graduationYearInt)
                    .credentials(credentials)
                    .subjects(subjects)
                    .grades(grades)
                    .pricePerHour(pricePerHourInt)
                    .minLessonHours(minLessonHoursDouble)
                    .lessonType(lessonType)
                    .lessonLocation(lessonLocation)
                    .availableTime(availableTime)
                    .currentPassword(currentPassword)
                    .newPassword(newPassword)
                    .confirmPassword(confirmPassword)
                    .notificationLesson(notificationLesson != null ? notificationLesson : true)
                    .notificationMessage(notificationMessage != null ? notificationMessage : true)
                    .notificationReview(notificationReview != null ? notificationReview : true)
                    .build();

            log.info("✅ DTO 생성 완료");

            // 4️⃣ 서비스 호출
            mentorProfileService.updateMentorProfileWithPassword(username, mentorDTO, profileImage);

            log.info("✅ 프로필 업데이트 완료");

            response.put("message", "프로필이 성공적으로 저장되었습니다!");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️  유효성 검사 실패: {}", e.getMessage());
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("❌ 프로필 업데이트 실패: {}", e.getMessage(), e);
            e.printStackTrace();
            response.put("error", e.getMessage() != null ? e.getMessage() : "프로필 저장 중 오류가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * 계정 삭제 (DELETE)
     */
    @DeleteMapping("/delete-account")
    @ResponseBody
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        log.info("🗑️  계정 삭제 요청");

        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("error", "인증이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            response.put("message", "계정이 삭제되었습니다");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 계정 삭제 실패: {}", e.getMessage());
            response.put("error", "계정 삭제 중 오류가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
