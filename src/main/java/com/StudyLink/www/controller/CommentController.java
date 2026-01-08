package com.StudyLink.www.controller;




import com.StudyLink.www.dto.CommentDTO;
import com.StudyLink.www.handler.PageHandler;
import com.StudyLink.www.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/comment/*")
@RestController
public class CommentController {
    private final CommentService commentService;

   /* @PostMapping("/post")
    @ResponseBody
    public String post(@RequestBody CommentDTO commentDTO){
        long cno = commentService.post(commentDTO);
        return cno > 0 ? "1" : "0";
    }*/

    @PostMapping(value = "/post",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> post(@RequestBody CommentDTO commentDTO){
        long cno = commentService.post(commentDTO);
        return cno > 0 ? new ResponseEntity<String>("1", HttpStatus.OK)
                : new ResponseEntity<String>("0", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*@GetMapping(value = "/list/{bno}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CommentDTO>> list(@PathVariable("bno") Long bno){
        List<CommentDTO> list = commentService.getList(bno);
        return new ResponseEntity<List<CommentDTO>>(list, HttpStatus.OK);
    }*/

    @GetMapping(value = "/list/{bno}/{page}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageHandler<CommentDTO>> list(@PathVariable("bno") Long bno,
                                                        @PathVariable("page") int page){
        Page<CommentDTO> list = commentService.getList(bno, page);

        PageHandler<CommentDTO> pageHandler = new PageHandler<>(list, page);

        return new ResponseEntity<PageHandler<CommentDTO>>(pageHandler, HttpStatus.OK);
    }

    @PutMapping(value = "/modify",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> modify(@RequestBody CommentDTO commentDTO){
        long cno = commentService.modify(commentDTO);
        return cno>0 ? new ResponseEntity<String>("1", HttpStatus.OK)
                : new ResponseEntity<String>("0", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/remove/{cno}")
    public ResponseEntity<String> remove(@PathVariable("cno") long cno){
        commentService.remove(cno);
        return cno>0 ? new ResponseEntity<String>("1", HttpStatus.OK)
                : new ResponseEntity<String>("0", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
