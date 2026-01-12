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
     *  Board 변환 메서드
     * ========================= */

    // BoardDTO -> Board(Entity)
    // createdAt/updatedAt 은 TimeBase(Auditing)가 처리하므로 여기서는 세팅하지 않음
    default Board convertDtoToEntity(BoardDTO boardDTO) {
        return Board.builder()
                .postId(boardDTO.getPostId())
                .userId(boardDTO.getUserId())
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .viewCount(boardDTO.getViewCount())
                .build();
    }

    // Board(Entity) -> BoardDTO
    default BoardDTO convertEntityToDto(Board board) {
        return BoardDTO.builder()
                .postId(board.getPostId())
                .userId(board.getUserId())
                .title(board.getTitle())
                .content(board.getContent())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())   // TimeBase
                .updatedAt(board.getUpdatedAt())   // TimeBase
                .build();
    }

    /* =========================
     *  Board 기능 메서드
     * ========================= */

    // 게시글 등록 (BoardDTO만)
    Long insert(BoardDTO boardDTO);

    // 게시글 목록 (페이징 + 검색)
    Page<BoardDTO> getList(int pageNo, String type, String keyword);

    // 게시글 상세 (첨부파일 포함 DTO)
    BoardFileDTO getDetail(long postId);

    // 게시글 수정 (첨부파일 포함)
    Long modify(BoardFileDTO boardFileDTO);

    // 게시글 삭제
    void remove(long postId);

    /* =========================
     *  File 변환 메서드
     * ========================= */

    // FileDTO -> File(Entity)
    default File convertDtoToEntity(FileDTO fileDTO) {
        return File.builder()
                .uuid(fileDTO.getUuid())
                .saveDir(fileDTO.getSaveDir())
                .fileName(fileDTO.getFileName())
                .fileType(fileDTO.getFileType())
                .postId(fileDTO.getPostId())
                .fileSize(fileDTO.getFileSize())
                .build();
    }

    // File(Entity) -> FileDTO
    // File 엔티티가 TimeBase 상속(또는 createdAt/updatedAt 필드 보유)한다고 가정
    default FileDTO convertEntityToDto(File file) {
        return FileDTO.builder()
                .uuid(file.getUuid())
                .saveDir(file.getSaveDir())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .postId(file.getPostId())
                .fileSize(file.getFileSize())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    /* =========================
     *  파일 포함 등록 (Board + Files)
     * ========================= */

    // 게시글 등록(첨부파일 포함)
    Long insert(BoardFileDTO boardFileDTO);

    // 파일 1개 삭제(물리 삭제는 핸들러/서비스 구현에서)
    long fileRemove(String uuid);

    // 파일 1개 조회
    FileDTO getFile(String uuid);

    // 오늘 날짜의 파일 목록 (배치/정리/썸네일 등)
    List<FileDTO> getTodayFileList(String today);
}
