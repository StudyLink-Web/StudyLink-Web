package com.StudyLink.www.repository;

import com.StudyLink.www.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, String> {

    // 게시글에 속한 파일 목록 조회
    List<File> findByPostId(Long postId);

    // ✅ 추가: 대표 이미지 1개만 가져오기 (fileType=1)
    Optional<File> findFirstByPostIdAndFileTypeOrderByCreatedAtAsc(Long postId, int fileType);

    // 게시글 삭제 시 파일 전체 삭제
    void deleteByPostId(Long postId);

    // FileSweeper 용 (오늘 날짜 경로 기준)
    List<File> findBySaveDir(String saveDir);
}
