package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Favorite;
import com.StudyLink.www.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 멘토 즐겨찾기 추가
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @RequestParam Long studentId,
            @RequestParam Long mentorId) {
        try {
            Favorite favorite = favoriteService.addFavorite(studentId, mentorId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "멘토가 즐겨찾기에 추가되었습니다");
            response.put("data", Map.of(
                    "favorite_id", favorite.getFavoriteId(),
                    "student_id", favorite.getStudentId(),      // ✅ 편의 메서드 사용
                    "mentor_id", favorite.getMentorId(),        // ✅ 편의 메서드 사용
                    "created_at", favorite.getCreatedAt()
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
     * 멘토 즐겨찾기 제거
     */
    @DeleteMapping("/remove/{favoriteId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(@PathVariable Long favoriteId) {
        try {
            favoriteService.removeFavorite(favoriteId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기가 제거되었습니다");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 학생의 즐겨찾기 목록 조회
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getFavoritesByStudent(@PathVariable Long studentId) {
        try {
            List<Favorite> favorites = favoriteService.getFavoritesByStudent(studentId);

            // ✅ 수정: ArrayList로 명시적으로 선언
            List<Map<String, Object>> favoriteList = new ArrayList<>();
            for (Favorite favorite : favorites) {
                Map<String, Object> favoriteMap = new HashMap<>();
                favoriteMap.put("favorite_id", favorite.getFavoriteId());
                favoriteMap.put("student_id", favorite.getStudentId());
                favoriteMap.put("mentor_id", favorite.getMentorId());
                favoriteMap.put("mentor_name", favorite.getMentor().getName());
                favoriteMap.put("mentor_nickname", favorite.getMentor().getNickname());
                favoriteMap.put("created_at", favorite.getCreatedAt());
                favoriteList.add(favoriteMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", favoriteList);
            response.put("count", favoriteList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 멘토의 팬 수 조회
     */
    @GetMapping("/mentor/{mentorId}/count")
    public ResponseEntity<Map<String, Object>> getFavoriteCountByMentor(@PathVariable Long mentorId) {
        try {
            long count = favoriteService.getFavoriteCountByMentor(mentorId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mentor_id", mentorId);
            response.put("favorite_count", count);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 특정 학생이 특정 멘토를 즐겨찾기했는지 확인
     */
    @GetMapping("/check/{studentId}/{mentorId}")
    public ResponseEntity<Map<String, Object>> isFavored(
            @PathVariable Long studentId,
            @PathVariable Long mentorId) {
        try {
            boolean isFavored = favoriteService.isFavored(studentId, mentorId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("student_id", studentId);
            response.put("mentor_id", mentorId);
            response.put("is_favored", isFavored);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
