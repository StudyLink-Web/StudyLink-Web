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
@RequestMapping("/board/*")
@Controller
public class BoardController {
    private final BoardService boardService;
    private final FileHandler fileHandler;

    @GetMapping("/register")
    public void register(){}

    @PostMapping("/register")
    public String register(BoardDTO boardDTO,
                           @RequestParam(name = "files", required = false)MultipartFile[] files){
        // 파일처리
        // 저장될 파일 데이터 + 직접 폴더에 파일을 저장
        List<FileDTO> fileList = null;
        if(files != null && files[0].getSize() > 0){
            // 핸들러 호출
            fileList = fileHandler.uploadFile(files);
        }
        log.info(">>> fileList >> {}", fileList);

        BoardFileDTO boardFileDTO = new BoardFileDTO(boardDTO, fileList);
        Long bno = boardService.insert(boardFileDTO);

        //Long bno = boardService.insert(boardDTO);
        //log.info(">>>> insert id >> {}", bno);
        return "redirect:/board/list";
    }

    @GetMapping("/list")
    public void list(Model model,
                     @RequestParam(name="pageNo", defaultValue = "1", required = false) int pageNo,
                     @RequestParam(name="type", required = false) String type,
                     @RequestParam(name = "keyword", required = false) String keyword){
        Page<BoardDTO> list = boardService.getList(pageNo, type, keyword);
        PageHandler<BoardDTO> pageHandler = new PageHandler<>(list, pageNo, type, keyword);
        model.addAttribute("ph", pageHandler);
    }

    @GetMapping("/detail")
    public void detail(@RequestParam("bno") long bno, Model model){
        BoardFileDTO boardFileDTO = boardService.getDetail(bno);
        model.addAttribute("boardFileDTO", boardFileDTO);
    }

    @PostMapping("/modify")
    public String modify(BoardDTO boardDTO,
                         RedirectAttributes redirectAttributes,
                         @RequestParam(name = "files", required = false) MultipartFile[] files){
        List<FileDTO>fileDTOList = null;
        log.info(">>> files >> {}", files);
        if(files != null && files[0].getSize() > 0){
            fileDTOList = fileHandler.uploadFile(files);
            log.info(">>> fileDtoList >> {}", fileDTOList);
        }

        Long bno = boardService.modify(new BoardFileDTO(boardDTO, fileDTOList));
        redirectAttributes.addAttribute("bno", boardDTO.getBno());
        return "redirect:/board/detail";
    }

    @GetMapping("/remove")
    public String remove(@RequestParam("bno") long bno){
        boardService.remove(bno);
        return "redirect:/board/list";
    }

    @DeleteMapping("/file/{uuid}")
    public ResponseEntity<String> fileRemove(@PathVariable("uuid")String uuid){
        // 파일을 먼저 삭제하고, DB의 데이터 삭제
        FileDTO removeFile = boardService.getFile(uuid);
        FileRemoveHandler fileRemoveHandler = new FileRemoveHandler();
        boolean isDel = fileRemoveHandler.removeFile(removeFile);

        // DB 데이터 삭제
        long bno = 0;
        if(isDel){
            bno = boardService.fileRemove(uuid);
        }
        return bno > 0 ? ResponseEntity.ok("1") :
                ResponseEntity.internalServerError().build();
    }
}
