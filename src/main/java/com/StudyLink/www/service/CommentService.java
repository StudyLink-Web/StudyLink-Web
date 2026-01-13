package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommentDTO;
import com.StudyLink.www.entity.Comment;
import org.springframework.data.domain.Page;

public interface CommentService {

    /* =========================
     *  convert
     * ========================= */

    default Comment convertDtoToEntity(CommentDTO dto) {
        if (dto == null) return null;

        return Comment.builder()
                .cno(dto.getCno())
                .postId(dto.getPostId())
                .writer(dto.getWriter())
                .content(dto.getContent())
                .build();
    }

    default CommentDTO convertEntityToDto(Comment c) {
        if (c == null) return null;

        return CommentDTO.builder()
                .cno(c.getCno())
                .postId(c.getPostId())
                .writer(c.getWriter())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    /* =========================
     *  service methods
     * ========================= */

    int post(CommentDTO commentDTO);

    int modify(CommentDTO commentDTO);

    int remove(long cno);

    Page<CommentDTO> getList(Long postId, int page);
}
