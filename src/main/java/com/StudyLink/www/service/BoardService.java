package com.StudyLink.www.service;

import com.StudyLink.www.dto.BoardDTO;
import com.StudyLink.www.dto.BoardFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.entity.Board;
import com.StudyLink.www.entity.File;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BoardService {
    // 추상메서드만 가능한 인터페이스
    // default method : 인터페이스에서 규칙을 잡거나, 로직을 잡거나 할 때 사용
    // 호환성 유지

    /* BoardDTO => Board 객체로 변환
     *  BoardDTO(class) : bno, title, writer, content, readCount, cmtQty, fileQty, regDate, modDate
     *  Board(entity) : bno, title, writer, content, readCount, cmtQty, fileQty
     *  화면 => DB
     * */
    default Board convertDtoToEntity(BoardDTO boardDTO){
        return Board.builder()
                .bno(boardDTO.getBno())
                .title(boardDTO.getTitle())
                .writer(boardDTO.getWriter())
                .content(boardDTO.getContent())
                .readCount(boardDTO.getReadCount())
                .cmtQty(boardDTO.getCmtQty())
                .fileQty(boardDTO.getFileQty())
                .build();
    }
    /* 반대 케이스
     *  DB => 화면
     *  Board => BoardDTO
     * */
    default BoardDTO convertEntityToDto(Board board){
        return BoardDTO.builder()
                .bno(board.getBno())
                .title(board.getTitle())
                .writer(board.getWriter())
                .content(board.getContent())
                .readCount(board.getReadCount())
                .cmtQty(board.getCmtQty())
                .fileQty(board.getFileQty())
                .regDate(board.getRegDate())
                .modDate(board.getModDate())
                .build();
    }


    Long insert(BoardDTO boardDTO);

    Page<BoardDTO> getList(int pageNo, String type, String keyword);

    BoardFileDTO getDetail(long bno);

    //Long modify(BoardDTO boardDTO);
    Long modify(BoardFileDTO boardFileDTO);

    void remove(long bno);

    // FileDTO => FileEntity
    default File convertDtoToEntity(FileDTO fileDTO){
        return File.builder()
                .uuid(fileDTO.getUuid())
                .saveDir(fileDTO.getSaveDir())
                .fileName(fileDTO.getFileName())
                .fileType(fileDTO.getFileType())
                .bno(fileDTO.getBno())
                .fileSize(fileDTO.getFileSize())
                .build();
    }

    // FileEntity => FileDTO
    default FileDTO convertEntityToDto(File file){
        return FileDTO.builder()
                .uuid(file.getUuid())
                .saveDir(file.getSaveDir())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .bno(file.getBno())
                .fileSize(file.getFileSize())
                .regDate(file.getRegDate())
                .modDate((file.getModDate()))
                .build();
    }

    Long insert(BoardFileDTO boardFileDTO);

    long fileRemove(String uuid);

    FileDTO getFile(String uuid);

    List<FileDTO> getTodayFileList(String today);
}

