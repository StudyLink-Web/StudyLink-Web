package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommentDTO;
import com.StudyLink.www.entity.Board;
import com.StudyLink.www.entity.Comment;
import com.StudyLink.www.repository.BoardRepository;
import com.StudyLink.www.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    @Override
    public long post(CommentDTO commentDTO) {

        Board board = boardRepository.findById(commentDTO.getPostId())
                .orElseThrow();

        Comment comment = convertDtoToEntity(commentDTO);
        commentRepository.save(comment);

        return comment.getCno();
    }

    @Override
    public long modify(CommentDTO commentDTO) {
        Comment comment = convertDtoToEntity(commentDTO);
        commentRepository.save(comment);
        return comment.getCno();
    }

    @Override
    public void remove(long cno) {
        commentRepository.deleteById(cno);
    }

    @Override
    public Page<CommentDTO> getList(Long postId, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "cno"));
        Page<Comment> result = commentRepository.findByPostId(postId, pageable);
        return result.map(this::convertEntityToDto);
    }
}