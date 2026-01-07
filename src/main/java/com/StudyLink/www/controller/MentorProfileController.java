package com.StudyLink.www.controller;

import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.service.MentorProfileService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mentor-profile")
@RequiredArgsConstructor
@Slf4j
public class MentorProfileController {

    private final MentorProfileService mentorProfileService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createMentorProfile(@RequestBody CreateMentorProfileRequest request) {
        try {
            MentorProfile profile = mentorProfileService.createMentorProfile(
                    request.getUserId(),
                    request.getUnivId(),
                    request.getDeptId(),
                    request.getIntroduction()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "profileId", profile.getProfileId(),
                    "userId", profile.getUser().getUserId(),
                    "introduction", profile.getIntroduction(),
                    "isVerified", profile.getIsVerified(),
                    "point", profile.getPoint(),
                    "message", "멘토 프로필이 생성되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "PROFILE_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getMentorProfile(@PathVariable Long userId) {
        var optionalProfile = mentorProfileService.getMentorProfile(userId);

        if (optionalProfile.isPresent()) {
            MentorProfile profile = optionalProfile.get();
            return ResponseEntity.ok(Map.of(
                    "profileId", profile.getProfileId(),
                    "userId", profile.getUser().getUserId(),
                    "introduction", profile.getIntroduction(),
                    "isVerified", profile.getIsVerified(),
                    "averageRating", profile.getAverageRating(),
                    "point", profile.getPoint()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateMentorProfile(
            @PathVariable Long userId,
            @RequestBody UpdateMentorProfileRequest request) {
        try {
            MentorProfile profile = mentorProfileService.updateMentorProfile(
                    userId,
                    request.getUnivId(),
                    request.getDeptId(),
                    request.getIntroduction()
            );

            return ResponseEntity.ok(Map.of(
                    "profileId", profile.getProfileId(),
                    "message", "멘토 프로필이 업데이트되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "PROFILE_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteMentorProfile(@PathVariable Long userId) {
        try {
            mentorProfileService.deleteMentorProfile(userId);

            return ResponseEntity.ok(Map.of(
                    "message", "멘토 프로필이 삭제되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "PROFILE_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    // ========== Request 클래스 ==========

    public static class CreateMentorProfileRequest {

        @JsonProperty("user_id")  // ✅ JSON에서 user_id로 받음 → Java에서 userId로 변환
        private Long userId;

        @JsonProperty("univ_id")  // ✅ JSON에서 univ_id로 받음 → Java에서 univId로 변환
        private Long univId;

        @JsonProperty("dept_id")  // ✅ JSON에서 dept_id로 받음 → Java에서 deptId로 변환
        private Long deptId;

        private String introduction;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getUnivId() { return univId; }
        public void setUnivId(Long univId) { this.univId = univId; }

        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }

        public String getIntroduction() { return introduction; }
        public void setIntroduction(String introduction) { this.introduction = introduction; }
    }

    public static class UpdateMentorProfileRequest {

        @com.fasterxml.jackson.annotation.JsonProperty("univ_id")  // ✅ 이 줄 추가!
        private Long univId;

        @com.fasterxml.jackson.annotation.JsonProperty("dept_id")  // ✅ 이 줄 추가!
        private Long deptId;

        private String introduction;

        public Long getUnivId() { return univId; }
        public void setUnivId(Long univId) { this.univId = univId; }

        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }

        public String getIntroduction() { return introduction; }
        public void setIntroduction(String introduction) { this.introduction = introduction; }
    }
}
