package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommentDTO;
import com.StudyLink.www.entity.Comment;
import org.springframework.data.domain.Page;

public interface CommentService {

    /* =========================
     *  convert
     * ========================= */

    // DTO -> Entity
    default Comment convertDtoToEntity(CommentDTO commentDTO) {
        return Comment.builder()
                .cno(commentDTO.getCno())
                .postId(commentDTO.getPostId())   // ✅ bno -> postId
                .writer(commentDTO.getWriter())
                .content(commentDTO.getContent())
                .build();
    }

    // Entity -> DTO
    default CommentDTO convertEntityToDto(Comment comment) {
        return CommentDTO.builder()
                .cno(comment.getCno())
                .postId(comment.getPostId())      // ✅ bno -> postId
                .writer(comment.getWriter())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt()) // ✅ TimeBase 쓰면 createdAt/updatedAt
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /* =========================
     *  service methods
     * ========================= */

    long post(CommentDTO commentDTO);

    long modify(CommentDTO commentDTO);

    void remove(long cno);

    Page<CommentDTO> getList(Long postId, int page); // ✅ bno -> postId
}
