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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final FileRepository fileRepository;

    @Override
    @Transactional
    public Long insert(BoardDTO boardDTO) {
        return insert(new BoardFileDTO(boardDTO, null));
    }

    @Override
    @Transactional
    public Long insert(BoardFileDTO boardFileDTO) {
        if (boardFileDTO == null || boardFileDTO.getBoardDTO() == null) {
            throw new IllegalArgumentException("boardFileDTO/boardDTO is null");
        }

        Board saved = boardRepository.save(convertDtoToEntity(boardFileDTO.getBoardDTO()));
        Long postId = saved.getPostId();

        List<FileDTO> files = boardFileDTO.getFileDTOList();
        if (files != null && !files.isEmpty()) {
            for (FileDTO f : files) {
                if (f == null) continue;
                f.setPostId(postId);
                fileRepository.save(convertDtoToEntity(f));
            }
        }
        return postId;
    }

    /* =========================
     * 목록 (검색 + 정렬 + 대표이미지 thumbPath)
     * ========================= */
    @Override
    @Transactional(readOnly = true)
    public Page<BoardDTO> getList(int pageNo, String type, String keyword) {

        int size = 10;
        int pageIndex = Math.max(pageNo - 1, 0);

        // ✅ 정렬 규칙
        // - null/blank/new : 최신순(postId desc)
        // - view : 조회수순(viewCount desc, postId desc)
        Sort sort = Sort.by(Sort.Direction.DESC, "postId");
        if ("view".equalsIgnoreCase(type)) {
            sort = Sort.by(Sort.Direction.DESC, "viewCount")
                    .and(Sort.by(Sort.Direction.DESC, "postId"));
        } else if ("new".equalsIgnoreCase(type) || type == null || type.isBlank()) {
            sort = Sort.by(Sort.Direction.DESC, "postId");
        }

        Pageable pageable = PageRequest.of(pageIndex, size, sort);

        // ✅ 검색어 공백 제거
        String kw = (keyword == null) ? "" : keyword.trim();

        // ✅ 여기 핵심: repository에 있는 메서드는 search(keyword,pageable)
        Page<Board> page = boardRepository.search(kw, pageable);

        // ✅ 대표 이미지(첫 번째 이미지) thumbPath 세팅
        return page.map(board -> {
            BoardDTO dto = convertEntityToDto(board);

            fileRepository.findFirstByPostIdAndFileTypeOrderByCreatedAtAsc(board.getPostId(), 1)
                    .ifPresent(img -> dto.setThumbPath("/board/file/" + img.getUuid()));

            return dto;
        });
    }

    /* =========================
     * 조회수 증가
     * ========================= */
    @Override
    @Transactional
    public void increaseViewCount(long postId) {
        boardRepository.increaseViewCount(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardFileDTO getDetail(long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음 postId=" + postId));

        List<File> flist = fileRepository.findByPostId(postId);
        List<FileDTO> dtoList = (flist == null || flist.isEmpty())
                ? List.of()
                : flist.stream().map(this::convertEntityToDto).toList();

        return new BoardFileDTO(convertEntityToDto(board), dtoList);
    }

    @Override
    @Transactional
    public Long modify(BoardFileDTO boardFileDTO) {
        if (boardFileDTO == null || boardFileDTO.getBoardDTO() == null) {
            throw new IllegalArgumentException("boardFileDTO/boardDTO is null");
        }
        if (boardFileDTO.getBoardDTO().getPostId() == null) {
            throw new IllegalArgumentException("postId is null (modify)");
        }

        Board saved = boardRepository.save(convertDtoToEntity(boardFileDTO.getBoardDTO()));
        Long postId = saved.getPostId();

        List<FileDTO> files = boardFileDTO.getFileDTOList();
        if (files != null && !files.isEmpty()) {
            for (FileDTO f : files) {
                if (f == null) continue;
                f.setPostId(postId);
                fileRepository.save(convertDtoToEntity(f));
            }
        }
        return postId;
    }

    @Override
    @Transactional
    public void remove(long postId) {
        fileRepository.deleteByPostId(postId);
        boardRepository.deleteById(postId);
    }

    @Override
    @Transactional
    public Long fileRemove(String uuid) {
        if (uuid == null || uuid.isBlank()) return 0L;

        File file = fileRepository.findById(uuid).orElse(null);
        if (file == null) return 0L;

        Long postId = file.getPostId();
        fileRepository.deleteById(uuid);

        return (postId == null) ? 0L : postId;
    }

    @Override
    @Transactional(readOnly = true)
    public FileDTO getFile(String uuid) {
        File file = fileRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("파일 없음 uuid=" + uuid));
        return convertEntityToDto(file);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDTO> getTodayFileList(String today) {
        List<File> list = fileRepository.findBySaveDir(today);
        return (list == null || list.isEmpty())
                ? List.of()
                : list.stream().map(this::convertEntityToDto).toList();
    }
}
