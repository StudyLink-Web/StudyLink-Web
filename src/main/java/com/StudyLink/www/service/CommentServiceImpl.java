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
    public int post(CommentDTO dto) {
        if (dto == null || dto.getPostId() == null) {
            log.error("post failed: postId is null. dto={}", dto);
            return 0;
        }
        if (dto.getWriter() == null || dto.getWriter().trim().isEmpty()) {
            log.error("post failed: writer is null/blank. dto={}", dto);
            return 0;
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            log.error("post failed: content is null/blank. dto={}", dto);
            return 0;
        }

        Comment comment = convertDtoToEntity(dto);
        Comment saved = commentRepository.save(comment);
        return (saved.getCno() != null && saved.getCno() > 0) ? 1 : 0;
    }

    @Transactional
    @Override
    public int modify(CommentDTO dto) {
        if (dto == null || dto.getCno() == null || dto.getCno() <= 0) {
            log.error("modify failed: cno is null/invalid. dto={}", dto);
            return 0;
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            log.error("modify failed: content is null/blank. dto={}", dto);
            return 0;
        }

        Comment origin = commentRepository.findById(dto.getCno())
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. cno=" + dto.getCno()));

        origin.setContent(dto.getContent());

        commentRepository.save(origin);
        return 1;
    }

    @Transactional
    @Override
    public int remove(long cno) {
        if (cno <= 0) return 0;

        if (!commentRepository.existsById(cno)) {
            return 0;
        }
        commentRepository.deleteById(cno);
        return 1;
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
