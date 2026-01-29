package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    Optional<Subject> findByName(String name);
}
