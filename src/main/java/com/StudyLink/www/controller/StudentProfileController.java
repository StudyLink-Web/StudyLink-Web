package com.StudyLink.www.controller;

import com.StudyLink.www.entity.StudentProfile;
import com.StudyLink.www.service.StudentProfileService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/student-profile")
@RequiredArgsConstructor
@Slf4j
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    /**
     * 학생 프로필 생성
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createStudentProfile(
            @RequestBody CreateStudentProfileRequest request) {
        try {
            StudentProfile profile = studentProfileService.createStudentProfile(
                    request.getUserId(),
                    request.getTargetUniversity(),
                    request.getTargetMajor(),
                    request.getRegionPreference()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "학생 프로필이 생성되었습니다");
            response.put("data", Map.of(
                    "userId", profile.getUserId(),  // ✅ getUserId() 사용 (PK)
                    "targetUniversity", profile.getTargetUniversity(),
                    "targetMajor", profile.getTargetMajor(),
                    "regionPreference", profile.getRegionPreference()
            ));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 학생 프로필 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getStudentProfile(@PathVariable Long userId) {
        try {
            Optional<StudentProfile> profileOpt = studentProfileService.getStudentProfile(userId);

            if (profileOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "학생 프로필을 찾을 수 없습니다");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            StudentProfile profile = profileOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                    "userId", profile.getUserId(),  // ✅ getUserId() 사용
                    "targetUniversity", profile.getTargetUniversity(),
                    "targetMajor", profile.getTargetMajor(),
                    "regionPreference", profile.getRegionPreference()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 학생 프로필 업데이트
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateStudentProfile(
            @PathVariable Long userId,
            @RequestBody UpdateStudentProfileRequest request) {
        try {
            StudentProfile profile = studentProfileService.updateStudentProfile(
                    userId,
                    request.getTargetUniversity(),
                    request.getTargetMajor(),
                    request.getRegionPreference()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "학생 프로필이 업데이트되었습니다");
            response.put("data", Map.of(
                    "userId", profile.getUserId(),  // ✅ getUserId() 사용
                    "targetUniversity", profile.getTargetUniversity(),
                    "targetMajor", profile.getTargetMajor(),
                    "regionPreference", profile.getRegionPreference()
            ));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 학생 프로필 삭제
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteStudentProfile(@PathVariable Long userId) {
        try {
            studentProfileService.deleteStudentProfile(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "학생 프로필이 삭제되었습니다");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== Request 클래스 ==========

    /**
     * 프로필 생성 요청
     */
    public static class CreateStudentProfileRequest {
        @JsonProperty("user_id")
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

    /**
     * 프로필 업데이트 요청
     */
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
