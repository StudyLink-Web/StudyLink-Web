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
        if (dto == null || dto.getPostId() == null) return 0;
        if (dto.getWriter() == null || dto.getWriter().trim().isEmpty()) return 0;
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) return 0;

        Comment saved = commentRepository.save(convertDtoToEntity(dto));
        return (saved.getCno() != null && saved.getCno() > 0) ? 1 : 0;
    }

    @Transactional
    @Override
    public int modify(CommentDTO dto) {
        if (dto == null || dto.getCno() == null || dto.getCno() <= 0) return 0;
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) return 0;

        Comment origin = commentRepository.findById(dto.getCno()).orElse(null);
        if (origin == null) return 0;

        // ✅ 작성자 검증 (Controller에서 writer 주입됨)
        if (!origin.getWriter().equals(dto.getWriter())) return 0;

        origin.setContent(dto.getContent());
        commentRepository.save(origin);
        return 1;
    }

    @Transactional
    @Override
    public int remove(long cno) {
        if (cno <= 0) return 0;

        Comment origin = commentRepository.findById(cno).orElse(null);
        if (origin == null) return 0;

        commentRepository.delete(origin);
        return 1;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CommentDTO> getList(Long postId, int page) {
        if (postId == null) return Page.empty();

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                10,
                Sort.by(Sort.Direction.DESC, "cno")
        );

        return commentRepository.findByPostId(postId, pageable)
                .map(this::convertEntityToDto);
    }
}
