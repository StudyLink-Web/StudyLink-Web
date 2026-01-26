package com.StudyLink.www.controller;

import com.StudyLink.www.dto.CommentDTO;
import com.StudyLink.www.handler.PageHandler;
import com.StudyLink.www.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping(
            value = "/post",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> post(@RequestBody CommentDTO commentDTO,
                                       Authentication authentication) {
        log.info(">>> /comment/post dto = {}", commentDTO);

        // ✅ 로그인 필수(작성)
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("0");
        }

        // ✅ writer는 서버에서 강제 세팅 (클라 조작 방지)
        String loginUser = authentication.getName();
        commentDTO.setWriter(loginUser);

        long cno = commentService.post(commentDTO);
        log.info(">>> /comment/post result cno = {}", cno);

        return cno > 0
                ? ResponseEntity.ok("1")
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("0");
    }

    @GetMapping(
            value = "/list/{postId}/{page}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageHandler<CommentDTO>> list(@PathVariable("postId") Long postId,
                                                        @PathVariable("page") int page) {

        Page<CommentDTO> list = commentService.getList(postId, page);
        PageHandler<CommentDTO> pageHandler = new PageHandler<>(list, page);

        return ResponseEntity.ok(pageHandler);
    }

    @PutMapping(
            value = "/modify",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> modify(@RequestBody CommentDTO commentDTO,
                                         Authentication authentication) {
        log.info(">>> /comment/modify dto = {}", commentDTO);

        // ✅ 로그인 필수(수정)
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("0");
        }

        // ✅ writer 강제 세팅(서비스에서 작성자 검증할 때 사용 가능)
        String loginUser = authentication.getName();
        commentDTO.setWriter(loginUser);

        long result = commentService.modify(commentDTO);
        log.info(">>> /comment/modify result = {}", result);

        return result > 0
                ? ResponseEntity.ok("1")
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("0");
    }

    @DeleteMapping(
            value = "/remove/{cno}",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> remove(@PathVariable("cno") long cno,
                                         Authentication authentication) {
        log.info(">>> /comment/remove cno = {}", cno);

        // ✅ 로그인 필수(삭제)
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("0");
        }

        long result = commentService.remove(cno);
        log.info(">>> /comment/remove result = {}", result);

        return result > 0
                ? ResponseEntity.ok("1")
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("0");
    }
}
