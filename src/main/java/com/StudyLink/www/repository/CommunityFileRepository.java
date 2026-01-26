package com.StudyLink.www.repository;

import com.StudyLink.www.entity.CommunityFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityFileRepository extends JpaRepository<CommunityFile, String> {
    List<CommunityFile> findByBno(Long bno);
    void deleteByBno(Long bno);
}
