package com.StudyLink.www.service;

import com.StudyLink.www.dto.BoardDTO;
import com.StudyLink.www.dto.BoardFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.entity.Board;
import com.StudyLink.www.entity.File;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BoardService {

    /* =========================
     *  변환 메서드
     * ========================= */

    default Board convertDtoToEntity(BoardDTO boardDTO) {
        if (boardDTO == null) return null;

        return Board.builder()
                .postId(boardDTO.getPostId())
                .userId(boardDTO.getUserId())
                .writer(boardDTO.getWriter())
                .department(boardDTO.getDepartment())
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .viewCount(boardDTO.getViewCount())
                .build();
    }

    default BoardDTO convertEntityToDto(Board board) {
        if (board == null) return null;

        return BoardDTO.builder()
                .postId(board.getPostId())
                .userId(board.getUserId())
                .writer(board.getWriter())
                .department(board.getDepartment())
                .title(board.getTitle())
                .content(board.getContent())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    default File convertDtoToEntity(FileDTO fileDTO) {
        if (fileDTO == null) return null;

        return File.builder()
                .uuid(fileDTO.getUuid())
                .saveDir(fileDTO.getSaveDir())
                .fileName(fileDTO.getFileName())
                .fileType(fileDTO.getFileType())
                .postId(fileDTO.getPostId())
                .fileSize(fileDTO.getFileSize())
                .build();
    }

    default FileDTO convertEntityToDto(File file) {
        if (file == null) return null;

        return FileDTO.builder()
                .uuid(file.getUuid())
                .saveDir(file.getSaveDir())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .postId(file.getPostId())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    /* =========================
     *  Board 기능
     * ========================= */

    Long insert(BoardDTO boardDTO);

    Long insert(BoardFileDTO boardFileDTO);

    /**
     * ✅ 목록 조회 (검색 + 정렬 + 페이징)
     * @param pageNo 1부터 시작
     * @param type 정렬 기준 ("" or null: 전체/기본, "new": 최신순, "view": 조회수순)
     * @param keyword 검색어 (null/blank면 전체목록)
     */
    Page<BoardDTO> getList(int pageNo, String type, String keyword);

    BoardFileDTO getDetail(long postId);

    Long modify(BoardFileDTO boardFileDTO);

    void remove(long postId);

    /**
     * ✅ 조회수 증가 (Repository의 increaseViewCount 사용)
     */
    void increaseViewCount(long postId);

    /* =========================
     *  File 기능
     * ========================= */

    Long fileRemove(String uuid);

    FileDTO getFile(String uuid);

    List<FileDTO> getTodayFileList(String today);
}
