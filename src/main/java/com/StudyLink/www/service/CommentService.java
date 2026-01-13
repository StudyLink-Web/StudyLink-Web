package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommentDTO;
import com.StudyLink.www.entity.Comment;
import org.springframework.data.domain.Page;

public interface CommentService {

    /* =========================
     *  convert
     * ========================= */

    // DTO -> Entity
    default Comment convertDtoToEntity(CommentDTO dto) {
        if (dto == null) return null;

        return Comment.builder()
                .cno(dto.getCno())
                .postId(dto.getPostId())
                .writer(dto.getWriter())
                .content(dto.getContent())
                .build();
    }

    // Entity -> DTO
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

    long post(CommentDTO commentDTO);

    long modify(CommentDTO commentDTO);

    long remove(long cno); // ✅ void -> long (삭제 성공 판단용)

    Page<CommentDTO> getList(Long postId, int page);
}
