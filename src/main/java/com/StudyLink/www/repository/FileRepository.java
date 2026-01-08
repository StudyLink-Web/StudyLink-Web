package com.StudyLink.www.repository;

import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, String> {
    List<File> findByBno(Long bno);

    List<File> findBySaveDir(String today);
}
