// CommunityFileRepository.java (그대로)
package com.StudyLink.www.repository;

import com.StudyLink.www.entity.CommunityFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityFileRepository extends JpaRepository<CommunityFile, Long> {
    List<CommunityFile> findAllByBnoOrderByFnoDesc(Long bno);
    Optional<CommunityFile> findByUuid(String uuid);
    void deleteAllByBno(Long bno);
}
