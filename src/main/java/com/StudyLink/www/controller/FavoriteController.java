package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Favorite;
import com.StudyLink.www.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;  // ✅ favorite_service → favoriteService

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addFavorite(@RequestBody AddFavoriteRequest request) {
        try {
            Favorite favorite = favoriteService.addFavorite(
                    request.getStudentId(),   // ✅ getStudent_user_id() → getStudentId()
                    request.getMentorId()     // ✅ getMentor_user_id() → getMentorId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "favorite_id", favorite.getFavoriteId(),  // ✅ getFavorite_id() → getFavoriteId()
                    "student_id", favorite.getStudentId(),     // ✅ getStudent_user_id() → getStudentId()
                    "mentor_id", favorite.getMentorId(),       // ✅ getMentor_user_id() → getMentorId()
                    "message", "멘토가 즐겨찾기되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "FAVORITE_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> removeFavorite(@RequestBody RemoveFavoriteRequest request) {
        try {
            favoriteService.removeFavorite(
                    request.getStudentId(),   // ✅ 수정
                    request.getMentorId()     // ✅ 수정
            );
            return ResponseEntity.ok(Map.of(
                    "message", "멘토가 즐겨찾기에서 제거되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "FAVORITE_ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<Map<String, Object>> getFavorites(@PathVariable Long studentId) {  // ✅ student_user_id → studentId
        List<Favorite> favorites = favoriteService.getFavoritesByStudent(studentId);
        var response = favorites.stream()
                .map(f -> Map.of(
                        "favorite_id", f.getFavoriteId(),    // ✅ getFavorite_id() → getFavoriteId()
                        "mentor_id", f.getMentorId()         // ✅ getMentor_user_id() → getMentorId()
                ))
                .toList();
        return ResponseEntity.ok(Map.of(
                "student_id", studentId,
                "count", response.size(),
                "favorites", response
        ));
    }

    @GetMapping("/check/{studentId}/{mentorId}")
    public ResponseEntity<Map<String, Object>> isFavorite(@PathVariable Long studentId,
                                                          @PathVariable Long mentorId) {  // ✅ 매개변수명 수정
        boolean isFavorite = favoriteService.isFavorite(studentId, mentorId);  // ✅ snake_case → camelCase
        return ResponseEntity.ok(Map.of(
                "student_id", studentId,
                "mentor_id", mentorId,
                "is_favorite", isFavorite
        ));
    }

    // ========== Request 클래스 ==========
    public static class AddFavoriteRequest {
        private Long student_id;      // ✅ student_user_id → student_id
        private Long mentor_id;       // ✅ mentor_user_id → mentor_id

        public Long getStudentId() { return student_id; }      // ✅ getStudent_user_id() → getStudentId()
        public void setStudentId(Long student_id) { this.student_id = student_id; }

        public Long getMentorId() { return mentor_id; }        // ✅ getMentor_user_id() → getMentorId()
        public void setMentorId(Long mentor_id) { this.mentor_id = mentor_id; }
    }

    public static class RemoveFavoriteRequest {
        private Long student_id;      // ✅ 수정
        private Long mentor_id;       // ✅ 수정

        public Long getStudentId() { return student_id; }      // ✅ 수정
        public void setStudentId(Long student_id) { this.student_id = student_id; }

        public Long getMentorId() { return mentor_id; }        // ✅ 수정
        public void setMentorId(Long mentor_id) { this.mentor_id = mentor_id; }
    }
}
