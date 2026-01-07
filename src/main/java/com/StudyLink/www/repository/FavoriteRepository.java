package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByStudentIdAndMentorId(Long studentId, Long mentorId);

    List<Favorite> findByStudentId(Long studentId);

    List<Favorite> findByMentorId(Long mentorId);

    boolean existsByStudentIdAndMentorId(Long studentId, Long mentorId);

    void deleteByStudentIdAndMentorId(Long studentId, Long mentorId);
}
