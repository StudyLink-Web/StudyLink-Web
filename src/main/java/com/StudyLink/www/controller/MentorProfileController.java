package com.StudyLink.www.controller;

import com.StudyLink.www.entity.MentorProfile;
import com.StudyLink.www.service.MentorProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mentor-profiles")
@RequiredArgsConstructor
@Slf4j
public class MentorProfileController {

    private final MentorProfileService mentorProfileService;

    /**
     * 멘토 프로필 생성
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createMentorProfile(
            @RequestParam Long userId,
            @RequestParam(required = false) Long univId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String introduction) {
        try {
            MentorProfile profile = mentorProfileService.createMentorProfile(
                    userId, univId, deptId, introduction);

            // ✅ ResponseEntity<Map<String, Object>> 타입 명시
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "멘토 프로필이 생성되었습니다");
            response.put("data", Map.of(
                    "userId", profile.getUser().getUserId(),
                    "univId", profile.getUnivId(),
                    "deptId", profile.getDeptId(),
                    "introduction", profile.getIntroduction(),
                    "isVerified", profile.getIsVerified()
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
     * 멘토 프로필 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getMentorProfile(@PathVariable Long userId) {
        try {
            Optional<MentorProfile> profileOpt = mentorProfileService.getMentorProfile(userId);

            if (profileOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "멘토 프로필을 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(error);
            }

            MentorProfile profile = profileOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                    "userId", profile.getUser().getUserId(),
                    "univId", profile.getUnivId(),
                    "deptId", profile.getDeptId(),
                    "introduction", profile.getIntroduction(),
                    "isVerified", profile.getIsVerified(),
                    "averageRating", profile.getAverageRating(),
                    "point", profile.getPoint(),
                    "exp", profile.getExp()
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
     * 모든 인증된 멘토 목록 조회
     */
    @GetMapping("/verified/list")
    public ResponseEntity<Map<String, Object>> getVerifiedMentors() {
        try {
            List<MentorProfile> mentors = mentorProfileService.getVerifiedMentors();

            List<Map<String, Object>> mentorList = new ArrayList<>();
            for (MentorProfile mentor : mentors) {
                Map<String, Object> mentorMap = new HashMap<>();
                mentorMap.put("userId", mentor.getUser().getUserId());
                mentorMap.put("univId", mentor.getUnivId());
                mentorMap.put("deptId", mentor.getDeptId());
                mentorMap.put("introduction", mentor.getIntroduction());
                mentorMap.put("averageRating", mentor.getAverageRating());
                mentorMap.put("point", mentor.getPoint());
                mentorMap.put("exp", mentor.getExp());
                mentorList.add(mentorMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", mentorList);
            response.put("count", mentorList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 멘토 인증
     */
    @PutMapping("/{userId}/verify")
    public ResponseEntity<Map<String, Object>> verifyMentor(@PathVariable Long userId) {
        try {
            MentorProfile profile = mentorProfileService.verifyMentor(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "멘토가 인증되었습니다");
            response.put("data", Map.of(
                    "userId", profile.getUser().getUserId(),
                    "isVerified", profile.getIsVerified()
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
     * 멘토 경험치 추가
     */
    @PutMapping("/{userId}/exp/{amount}")
    public ResponseEntity<Map<String, Object>> addExp(
            @PathVariable Long userId,
            @PathVariable Long amount) {
        try {
            MentorProfile profile = mentorProfileService.addExp(userId, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "경험치가 추가되었습니다");
            response.put("data", Map.of(
                    "userId", profile.getUser().getUserId(),
                    "exp", profile.getExp()
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
     * 멘토 포인트 추가
     */
    @PutMapping("/{userId}/point/{amount}")
    public ResponseEntity<Map<String, Object>> addPoint(
            @PathVariable Long userId,
            @PathVariable Long amount) {
        try {
            MentorProfile profile = mentorProfileService.addPoint(userId, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "포인트가 추가되었습니다");
            response.put("data", Map.of(
                    "userId", profile.getUser().getUserId(),
                    "point", profile.getPoint()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
