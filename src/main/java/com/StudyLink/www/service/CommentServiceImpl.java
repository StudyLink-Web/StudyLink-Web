package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommentDTO;
import com.StudyLink.www.entity.Comment;
import com.StudyLink.www.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public long post(CommentDTO dto) {
        if (dto == null || dto.getPostId() == null) {
            log.error("post failed: postId is null. dto={}", dto);
            return 0L;
        }
        if (dto.getWriter() == null || dto.getWriter().trim().isEmpty()) {
            log.error("post failed: writer is null/blank. dto={}", dto);
            return 0L;
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            log.error("post failed: content is null/blank. dto={}", dto);
            return 0L;
        }

        Comment comment = convertDtoToEntity(dto);
        Comment saved = commentRepository.save(comment);
        return saved.getCno() != null ? saved.getCno() : 0L;
    }

    @Transactional
    @Override
    public long modify(CommentDTO dto) {
        if (dto == null || dto.getCno() == null) {
            log.error("modify failed: cno is null. dto={}", dto);
            return 0L;
        }

        Comment origin = commentRepository.findById(dto.getCno())
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. cno=" + dto.getCno()));

        // ✅ 내용만 수정(보통 writer/postId는 유지)
        origin.setContent(dto.getContent());

        Comment saved = commentRepository.save(origin);
        return saved.getCno() != null ? saved.getCno() : 0L;
    }

    @Transactional
    @Override
    public long remove(long cno) {
        if (cno <= 0) return 0L;

        if (!commentRepository.existsById(cno)) {
            return 0L;
        }
        commentRepository.deleteById(cno);
        return 1L;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CommentDTO> getList(Long postId, int page) {
        if (postId == null) return Page.empty();

        int size = 10;
        int pageIndex = Math.max(page - 1, 0);

        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "cno"));
        Page<Comment> result = commentRepository.findByPostId(postId, pageable);

        return result.map(this::convertEntityToDto);
    }
}
