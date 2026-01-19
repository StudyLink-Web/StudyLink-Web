package com.StudyLink.www.dto;

import com.StudyLink.www.entity.Favorite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FavoriteDTO
 * 학생-멘토-찜 관계를 나타내는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDTO {
    private Long favoriteId;      // 찜 ID
    private Long studentId;       // 학생 ID
    private String studentName;   // 학생 이름
    private Long mentorId;        // 멘토 ID
    private String mentorName;    // 멘토 이름
    private boolean favorite;     // 찜 여부

    /**
     * Favorite 엔티티를 받아 DTO로 변환하는 생성자
     */
    public FavoriteDTO(Favorite favorite) {
        if(favorite != null) {
            this.favoriteId = favorite.getFavoriteId();
            this.studentId = favorite.getStudentId();
            this.studentName = favorite.getStudent() != null ? favorite.getStudent().getName() : null;
            this.mentorId = favorite.getMentorId();
            this.mentorName = favorite.getMentor() != null ? favorite.getMentor().getName() : null;
            this.favorite = true; // DB에 존재하면 찜 true
        }
    }
}