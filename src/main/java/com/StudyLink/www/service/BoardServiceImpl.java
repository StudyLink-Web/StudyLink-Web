package com.StudyLink.www.service;

import com.StudyLink.www.dto.BoardDTO;
import com.StudyLink.www.dto.BoardFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.entity.Board;
import com.StudyLink.www.entity.File;
import com.StudyLink.www.repository.BoardRepository;
import com.StudyLink.www.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final FileRepository fileRepository;

    @Override
    public Long insert(BoardDTO boardDTO) {
        Board saved = boardRepository.save(convertDtoToEntity(boardDTO));
        return saved.getPostId();
    }

    @Override
    public Long insert(BoardFileDTO boardFileDTO) {
        Board saved = boardRepository.save(convertDtoToEntity(boardFileDTO.getBoardDTO()));
        Long postId = saved.getPostId();

        List<FileDTO> files = boardFileDTO.getFileDTOList();
        if (files != null && !files.isEmpty()) {
            for (FileDTO f : files) {
                f.setPostId(postId);
                fileRepository.save(convertDtoToEntity(f));
            }
        }
        return postId;
    }

    @Override
    public Page<BoardDTO> getList(int pageNo, String type, String keyword) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10, Sort.by(Sort.Direction.DESC, "postId"));
        Page<Board> page = boardRepository.searchBoard(type, keyword, pageable);
        return page.map(this::convertEntityToDto);
    }

    @Override
    public BoardFileDTO getDetail(long postId) {
        Board board = boardRepository.findById(postId).orElseThrow();
        List<File> flist = fileRepository.findByPostId(postId);
        List<FileDTO> dtoList = (flist == null)
                ? List.of()
                : flist.stream().map(this::convertEntityToDto).toList();
        return new BoardFileDTO(convertEntityToDto(board), dtoList);
    }

    @Override
    public Long modify(BoardFileDTO boardFileDTO) {
        Board saved = boardRepository.save(convertDtoToEntity(boardFileDTO.getBoardDTO()));
        Long postId = saved.getPostId();

        List<FileDTO> files = boardFileDTO.getFileDTOList();
        if (files != null && !files.isEmpty()) {
            for (FileDTO f : files) {
                f.setPostId(postId);
                fileRepository.save(convertDtoToEntity(f));
            }
        }
        return postId;
    }

    @Override
    public void remove(long postId) {
        fileRepository.deleteByPostId(postId);
        boardRepository.deleteById(postId);
    }

    @Override
    public long fileRemove(String uuid) {
        fileRepository.deleteById(uuid);
        return 1;
    }

    @Override
    public FileDTO getFile(String uuid) {
        File file = fileRepository.findById(uuid).orElseThrow();
        return convertEntityToDto(file);
    }

    @Override
    public List<FileDTO> getTodayFileList(String today) {
        List<File> list = fileRepository.findBySaveDir(today);
        return (list == null) ? List.of() : list.stream().map(this::convertEntityToDto).toList();
    }
}
