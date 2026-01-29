package com.StudyLink.www.service;

import com.StudyLink.www.dto.BoardDTO;
import com.StudyLink.www.dto.BoardFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.entity.Board;
import com.StudyLink.www.entity.File;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.BoardRepository;
import com.StudyLink.www.repository.FileRepository;
import com.StudyLink.www.repository.UserRepository;
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
    private final UserRepository userRepository;

    private boolean isThumb(File f) {
        if (f == null) return false;
        String u = f.getUuid();
        String n = f.getFileName();
        return (u != null && u.startsWith("_th_")) || (n != null && n.contains("_th_"));
    }

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

        BoardDTO dto = boardFileDTO.getBoardDTO();
        Users user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Board saved = boardRepository.save(convertDtoToEntity(dto, user));
        Long postId = saved.getPostId();

        List<FileDTO> files = boardFileDTO.getFileDTOList();
        if (files != null) {
            for (FileDTO f : files) {
                if (f == null || f.isThumbnail()) continue;
                f.setPostId(postId);
                fileRepository.save(convertDtoToEntity(f));
            }
        }
        return postId;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoardDTO> getList(int pageNo, String type, String keyword) {

        int size = 12;
        int pageIndex = Math.max(pageNo - 1, 0);

        Sort sort = "view".equalsIgnoreCase(type)
                ? Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "postId"))
                : Sort.by(Sort.Direction.DESC, "postId");

        Pageable pageable = PageRequest.of(pageIndex, size, sort);
        String kw = (keyword == null) ? "" : keyword.trim();

        Page<Board> page = boardRepository.search(kw, pageable);

        return page.map(board -> {
            BoardDTO dto = convertEntityToDto(board);
            fileRepository.findFirstByPostIdAndFileTypeOrderByCreatedAtAsc(board.getPostId(), 1)
                    .filter(img -> !isThumb(img))
                    .ifPresent(img -> dto.setThumbPath("/board/file/" + img.getUuid()));
            return dto;
        });
    }

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

        List<FileDTO> dtoList = (flist == null) ? List.of() :
                flist.stream()
                        .filter(f -> f != null && !isThumb(f))
                        .map(this::convertEntityToDto)
                        .toList();

        return new BoardFileDTO(convertEntityToDto(board), dtoList);
    }

    @Override
    @Transactional
    public Long modify(BoardFileDTO boardFileDTO) {
        if (boardFileDTO == null || boardFileDTO.getBoardDTO() == null) {
            throw new IllegalArgumentException("boardFileDTO/boardDTO is null");
        }

        BoardDTO dto = boardFileDTO.getBoardDTO();
        Users user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Board saved = boardRepository.save(convertDtoToEntity(dto, user));
        Long postId = saved.getPostId();

        fileRepository.deleteByPostId(postId);

        List<FileDTO> files = boardFileDTO.getFileDTOList();
        if (files != null) {
            for (FileDTO f : files) {
                if (f == null || f.isThumbnail()) continue;
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

        return postId == null ? 0L : postId;
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
        return (list == null) ? List.of() :
                list.stream()
                        .filter(f -> f != null && !isThumb(f))
                        .map(this::convertEntityToDto)
                        .toList();
    }
}
