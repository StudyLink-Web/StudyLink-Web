package com.StudyLink.www.controller;

import com.StudyLink.www.entity.StudentProfile;
import com.StudyLink.www.service.StudentProfileService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student-profile")
@RequiredArgsConstructor
@Slf4j
public class StudentProfileController {

    private final StudentProfileService studentProfileService;  // ✅ student_profile_service → studentProfileService

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createStudentProfile(@RequestBody CreateStudentProfileRequest request) {
        try {
            StudentProfile profile = studentProfileService.createStudentProfile(
                    request.getUserId(),  // ✅ getUser_id() → getUserId()
                    request.getTargetUniversity(),  // ✅ getTarget_university() → getTargetUniversity()
                    request.getTargetMajor(),  // ✅ getTarget_major() → getTargetMajor()
                    request.getRegionPreference()  // ✅ getRegion_preference() → getRegionPreference()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "profileId", profile.getProfileId(),  // ✅ getProfile_id() → getProfileId()
                    "userId", profile.getUser().getUserId(),  // ✅ getUserId() + user 필드 이용
                    "targetUniversity", profile.getTargetUniversity(),  // ✅ getTargetUniversity()
                    "targetMajor", profile.getTargetMajor(),  // ✅ getTargetMajor()
                    "regionPreference", profile.getRegionPreference(),  // ✅ getRegionPreference()
                    "message", "학생 프로필이 생성되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "PROFILE_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getStudentProfile(@PathVariable Long userId) {  // ✅ ResponseEntity<?> 사용
        return studentProfileService.getStudentProfile(userId)
                .map(profile -> ResponseEntity.ok(Map.of(
                        "profileId", profile.getProfileId(),
                        "userId", profile.getUser().getUserId(),
                        "targetUniversity", profile.getTargetUniversity(),
                        "targetMajor", profile.getTargetMajor(),
                        "regionPreference", profile.getRegionPreference()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "error", "NOT_FOUND",
                        "message", "학생 프로필을 찾을 수 없습니다."
                )));
    }


    @PutMapping("/{userId}")  // ✅ /{user_id} → /{userId}
    public ResponseEntity<Map<String, Object>> updateStudentProfile(
            @PathVariable Long userId,  // ✅ user_id → userId
            @RequestBody UpdateStudentProfileRequest request) {
        try {
            StudentProfile profile = studentProfileService.updateStudentProfile(
                    userId,
                    request.getTargetUniversity(),
                    request.getTargetMajor(),
                    request.getRegionPreference()
            );

            return ResponseEntity.ok(Map.of(
                    "profileId", profile.getProfileId(),  // ✅ getProfileId()
                    "userId", profile.getUser().getUserId(),  // ✅ getUserId() + user 필드
                    "message", "학생 프로필이 업데이트되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "PROFILE_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{userId}")  // ✅ /{user_id} → /{userId}
    public ResponseEntity<Map<String, Object>> deleteStudentProfile(@PathVariable Long userId) {  // ✅ user_id → userId
        try {
            studentProfileService.deleteStudentProfile(userId);

            return ResponseEntity.ok(Map.of(
                    "message", "학생 프로필이 삭제되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "PROFILE_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    // ========== Request 클래스 ==========

    public static class CreateStudentProfileRequest {

        @JsonProperty("user_id")  // ✅ JSON ↔ Java 매핑
        private Long userId;

        @JsonProperty("target_university")
        private String targetUniversity;

        @JsonProperty("target_major")
        private String targetMajor;

        @JsonProperty("region_preference")
        private String regionPreference;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getTargetUniversity() { return targetUniversity; }
        public void setTargetUniversity(String targetUniversity) { this.targetUniversity = targetUniversity; }

        public String getTargetMajor() { return targetMajor; }
        public void setTargetMajor(String targetMajor) { this.targetMajor = targetMajor; }

        public String getRegionPreference() { return regionPreference; }
        public void setRegionPreference(String regionPreference) { this.regionPreference = regionPreference; }
    }

    public static class UpdateStudentProfileRequest {

        @JsonProperty("target_university")
        private String targetUniversity;

        @JsonProperty("target_major")
        private String targetMajor;

        @JsonProperty("region_preference")
        private String regionPreference;

        public String getTargetUniversity() { return targetUniversity; }
        public void setTargetUniversity(String targetUniversity) { this.targetUniversity = targetUniversity; }

        public String getTargetMajor() { return targetMajor; }
        public void setTargetMajor(String targetMajor) { this.targetMajor = targetMajor; }

        public String getRegionPreference() { return regionPreference; }
        public void setRegionPreference(String regionPreference) { this.regionPreference = regionPreference; }
    }
}
