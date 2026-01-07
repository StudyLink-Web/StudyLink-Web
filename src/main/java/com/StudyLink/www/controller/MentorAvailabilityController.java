package com.StudyLink.www.controller;

import com.StudyLink.www.entity.MentorAvailability;
import com.StudyLink.www.service.MentorAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mentor-availability")
@RequiredArgsConstructor
@Slf4j
public class MentorAvailabilityController {

    private final MentorAvailabilityService mentorAvailabilityService;  // ✅ mentor_availability_service → mentorAvailabilityService

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addAvailability(@RequestBody AddAvailabilityRequest request) {
        try {
            MentorAvailability availability = mentorAvailabilityService.addAvailability(
                    request.getMentorId(),      // ✅ getUser_id() → getMentorId()
                    request.getDayOfWeek(),     // ✅ getDay_of_week() → getDayOfWeek() (Integer)
                    request.getBlock()          // ✅ getStart_time/getEnd_time() → getBlock() (Integer)
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "availabilityId", availability.getAvailabilityId(),  // ✅ getAvailability_id() → getAvailabilityId()
                    "mentorId", availability.getMentorId(),              // ✅ getUser_id() → getMentorId()
                    "dayOfWeek", availability.getDayOfWeek(),            // ✅ getDay_of_week() → getDayOfWeek()
                    "block", availability.getBlock(),                    // ✅ getStart_time/getEnd_time() → getBlock()
                    "message", "멘토 가능 시간이 등록되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "AVAILABILITY_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{mentorId}")  // ✅ /{user_id} → /{mentorId}
    public ResponseEntity<Map<String, Object>> getMentorAvailabilities(@PathVariable Long mentorId) {  // ✅ user_id → mentorId
        List<MentorAvailability> availabilities = mentorAvailabilityService.getMentorAvailabilities(mentorId);

        var response = availabilities.stream()
                .map(a -> Map.of(
                        "availabilityId", a.getAvailabilityId(),  // ✅ getAvailability_id() → getAvailabilityId()
                        "dayOfWeek", a.getDayOfWeek(),            // ✅ getDay_of_week() → getDayOfWeek()
                        "block", a.getBlock()                     // ✅ getStart_time/getEnd_time() → getBlock()
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "mentorId", mentorId,           // ✅ user_id → mentorId
                "availabilities", response
        ));
    }

    @DeleteMapping("/{availabilityId}")  // ✅ /{availability_id} → /{availabilityId}
    public ResponseEntity<Map<String, Object>> deleteAvailability(@PathVariable Long availabilityId) {  // ✅ availability_id → availabilityId
        mentorAvailabilityService.deleteAvailability(availabilityId);

        return ResponseEntity.ok(Map.of(
                "message", "멘토 가능 시간이 삭제되었습니다."
        ));
    }

    @PutMapping("/{availabilityId}")  // ✅ /{availability_id} → /{availabilityId}
    public ResponseEntity<Map<String, Object>> updateAvailability(
            @PathVariable Long availabilityId,  // ✅ availability_id → availabilityId
            @RequestBody UpdateAvailabilityRequest request) {
        try {
            MentorAvailability availability = mentorAvailabilityService.updateAvailability(
                    availabilityId,             // ✅ availability_id → availabilityId
                    request.getDayOfWeek(),     // ✅ getDay_of_week() → getDayOfWeek() (Integer)
                    request.getBlock()          // ✅ getStart_time/getEnd_time() → getBlock() (Integer)
            );

            return ResponseEntity.ok(Map.of(
                    "availabilityId", availability.getAvailabilityId(),  // ✅ getAvailability_id() → getAvailabilityId()
                    "mentorId", availability.getMentorId(),              // ✅ getUser_id() → getMentorId()
                    "dayOfWeek", availability.getDayOfWeek(),            // ✅ getDay_of_week() → getDayOfWeek()
                    "block", availability.getBlock(),                    // ✅ getStart_time/getEnd_time() → getBlock()
                    "message", "멘토 가능 시간이 수정되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "AVAILABILITY_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    // ========== Request 클래스 ==========

    public static class AddAvailabilityRequest {
        private Long mentorId;          // ✅ user_id → mentorId
        private Integer dayOfWeek;      // ✅ String day_of_week → Integer dayOfWeek
        private Integer block;          // ✅ start_time, end_time → block

        public Long getMentorId() { return mentorId; }
        public void setMentorId(Long mentorId) { this.mentorId = mentorId; }

        public Integer getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public Integer getBlock() { return block; }
        public void setBlock(Integer block) { this.block = block; }
    }

    public static class UpdateAvailabilityRequest {
        private Integer dayOfWeek;      // ✅ String day_of_week → Integer dayOfWeek
        private Integer block;          // ✅ start_time, end_time → block

        public Integer getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public Integer getBlock() { return block; }
        public void setBlock(Integer block) { this.block = block; }
    }
}
