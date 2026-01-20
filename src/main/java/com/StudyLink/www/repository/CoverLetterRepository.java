package com.StudyLink.www.repository;

import com.StudyLink.www.entity.CoverLetter;
import com.StudyLink.www.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {
    List<CoverLetter> findByUserOrderByCreatedAtDesc(Users user);
}
