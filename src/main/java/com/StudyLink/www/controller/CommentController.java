package com.StudyLink.www.controller;

import com.StudyLink.www.dto.CommentDTO;
import com.StudyLink.www.handler.PageHandler;
import com.StudyLink.www.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
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
    public ResponseEntity<String> post(@RequestBody CommentDTO commentDTO) {
        long cno = commentService.post(commentDTO);
        return cno > 0
                ? new ResponseEntity<>("1", HttpStatus.OK)
                : new ResponseEntity<>("0", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(
            value = "/list/{postId}/{page}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageHandler<CommentDTO>> list(@PathVariable("postId") Long postId,
                                                        @PathVariable("page") int page) {

        Page<CommentDTO> list = commentService.getList(postId, page);
        PageHandler<CommentDTO> pageHandler = new PageHandler<>(list, page);

        return new ResponseEntity<>(pageHandler, HttpStatus.OK);
    }

    @PutMapping(
            value = "/modify",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> modify(@RequestBody CommentDTO commentDTO) {
        long result = commentService.modify(commentDTO);
        return result > 0
                ? new ResponseEntity<>("1", HttpStatus.OK)
                : new ResponseEntity<>("0", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping(
            value = "/remove/{cno}",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> remove(@PathVariable("cno") long cno) {
        long result = commentService.remove(cno);
        return result > 0
                ? new ResponseEntity<>("1", HttpStatus.OK)
                : new ResponseEntity<>("0", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
