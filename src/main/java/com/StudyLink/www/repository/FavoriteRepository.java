package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Favorite;
import com.StudyLink.www.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * 학생의 즐겨찾기 목록 조회
     */
    List<Favorite> findByStudent(Users student);

    /**
     * 멘토를 즐겨찾기한 학생 수 조회
     */
    long countByMentor(Users mentor);

    /**
     * 특정 학생이 특정 멘토를 즐겨찾기했는지 확인
     */
    boolean existsByStudentAndMentor(Users student, Users mentor);

    /**
     * 학생과 멘토로 즐겨찾기 삭제
     */
    void deleteByStudentAndMentor(Users student, Users mentor);
}
