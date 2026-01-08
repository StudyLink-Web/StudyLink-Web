package com.StudyLink.www.controller;

import com.StudyLink.www.dto.BoardDTO;
import com.StudyLink.www.dto.BoardFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.handler.FileHandler;
import com.StudyLink.www.handler.FileRemoveHandler;
import com.StudyLink.www.handler.PageHandler;
import com.StudyLink.www.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/board")
@Controller
public class BoardController {

    private final BoardService boardService;
    private final FileHandler fileHandler;

    @GetMapping("/register")
    public void register(){}

    @PostMapping("/register")
    public String register(BoardDTO boardDTO,
                           @RequestParam(name = "files", required = false) MultipartFile[] files) {

        List<FileDTO> fileList = null;

        if (files != null && files.length > 0 && files[0].getSize() > 0) {
            fileList = fileHandler.uploadFile(files);
        }
        log.info(">>> fileList >> {}", fileList);

        Long postId = boardService.insert(new BoardFileDTO(boardDTO, fileList));

        // 등록 후 상세로 보내고 싶으면 아래로 바꾸면 됨:
        // return "redirect:/board/detail?postId=" + postId;

        return "redirect:/board/list";
    }

    @GetMapping("/list")
    public void list(Model model,
                     @RequestParam(name="pageNo", defaultValue = "1", required = false) int pageNo,
                     @RequestParam(name="type", required = false) String type,
                     @RequestParam(name="keyword", required = false) String keyword){

        Page<BoardDTO> list = boardService.getList(pageNo, type, keyword);
        PageHandler<BoardDTO> pageHandler = new PageHandler<>(list, pageNo, type, keyword);
        model.addAttribute("ph", pageHandler);
    }

    @GetMapping("/detail")
    public void detail(@RequestParam("postId") long postId, Model model) {
        BoardFileDTO boardFileDTO = boardService.getDetail(postId);
        model.addAttribute("boardFileDTO", boardFileDTO);
    }

    @PostMapping("/modify")
    public String modify(BoardDTO boardDTO,
                         RedirectAttributes redirectAttributes,
                         @RequestParam(name = "files", required = false) MultipartFile[] files) {

        List<FileDTO> fileDTOList = null;
        log.info(">>> files >> {}", files);

        if (files != null && files.length > 0 && files[0].getSize() > 0) {
            fileDTOList = fileHandler.uploadFile(files);
            log.info(">>> fileDtoList >> {}", fileDTOList);
        }

        Long postId = boardService.modify(new BoardFileDTO(boardDTO, fileDTOList));

        redirectAttributes.addAttribute("postId", postId);
        return "redirect:/board/detail";
    }

    @GetMapping("/remove")
    public String remove(@RequestParam("postId") long postId) {
        boardService.remove(postId);
        return "redirect:/board/list";
    }

    @DeleteMapping("/file/{uuid}")
    public ResponseEntity<String> fileRemove(@PathVariable("uuid") String uuid) {

        FileDTO removeFile = boardService.getFile(uuid);

        FileRemoveHandler fileRemoveHandler = new FileRemoveHandler();
        boolean isDel = fileRemoveHandler.removeFile(removeFile);

        long postId = 0;
        if (isDel) {
            postId = boardService.fileRemove(uuid); // 여기 리턴이 postId인지 확인 필요(아래 참고)
        }

        return postId > 0 ? ResponseEntity.ok("1")
                : ResponseEntity.internalServerError().build();
    }
}
