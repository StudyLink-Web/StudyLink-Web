package com.StudyLink.www.controller;

import com.StudyLink.www.entity.MentorAvailability;
import com.StudyLink.www.service.MentorAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mentor-availability")
@RequiredArgsConstructor
@Slf4j
public class MentorAvailabilityController {

    private final MentorAvailabilityService mentorAvailabilityService;

    /**
     * 멘토 활동 가능 시간 추가
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addAvailability(
            @RequestParam Long mentorId,
            @RequestParam Integer dayOfWeek,
            @RequestParam Integer block) {
        try {
            MentorAvailability availability = mentorAvailabilityService.addAvailability(mentorId, dayOfWeek, block);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "활동 가능 시간이 추가되었습니다");
            response.put("data", Map.of(
                    "availId", availability.getAvailId(),        // ✅ getAvailId() 사용
                    "mentor_id", availability.getMentor().getUserId(),
                    "day_of_week", availability.getDayOfWeek(),
                    "block", availability.getBlock()
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
     * 멘토 활동 가능 시간 제거
     */
    @DeleteMapping("/remove/{availId}")
    public ResponseEntity<Map<String, Object>> removeAvailability(@PathVariable Long availId) {
        try {
            mentorAvailabilityService.removeAvailability(availId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "활동 가능 시간이 제거되었습니다");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 멘토의 활동 가능 시간 목록 조회
     */
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<Map<String, Object>> getAvailabilityByMentor(@PathVariable Long mentorId) {
        try {
            List<MentorAvailability> availabilities = mentorAvailabilityService.getAvailabilityByMentor(mentorId);

            // ✅ ArrayList + HashMap으로 타입 안전성 확보
            List<Map<String, Object>> availabilityList = new ArrayList<>();
            for (MentorAvailability availability : availabilities) {
                Map<String, Object> availMap = new HashMap<>();
                availMap.put("availId", availability.getAvailId());
                availMap.put("mentor_id", availability.getMentor().getUserId());
                availMap.put("day_of_week", availability.getDayOfWeek());
                availMap.put("block", availability.getBlock());
                availabilityList.add(availMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", availabilityList);
            response.put("count", availabilityList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 특정 요일의 멘토 활동 가능 시간 조회
     */
    @GetMapping("/mentor/{mentorId}/day/{dayOfWeek}")
    public ResponseEntity<Map<String, Object>> getAvailabilityByMentorAndDay(
            @PathVariable Long mentorId,
            @PathVariable Integer dayOfWeek) {
        try {
            List<MentorAvailability> availabilities = mentorAvailabilityService.getAvailabilityByMentorAndDay(mentorId, dayOfWeek);

            List<Map<String, Object>> availabilityList = new ArrayList<>();
            for (MentorAvailability availability : availabilities) {
                Map<String, Object> availMap = new HashMap<>();
                availMap.put("availId", availability.getAvailId());
                availMap.put("mentor_id", availability.getMentor().getUserId());
                availMap.put("day_of_week", availability.getDayOfWeek());
                availMap.put("block", availability.getBlock());
                availabilityList.add(availMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", availabilityList);
            response.put("count", availabilityList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 특정 요일과 시간 블록의 멘토 활동 가능 여부 확인
     */
    @GetMapping("/check/{mentorId}/{dayOfWeek}/{block}")
    public ResponseEntity<Map<String, Object>> isAvailable(
            @PathVariable Long mentorId,
            @PathVariable Integer dayOfWeek,
            @PathVariable Integer block) {
        try {
            boolean isAvailable = mentorAvailabilityService.isAvailable(mentorId, dayOfWeek, block);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mentor_id", mentorId);
            response.put("day_of_week", dayOfWeek);
            response.put("block", block);
            response.put("is_available", isAvailable);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
