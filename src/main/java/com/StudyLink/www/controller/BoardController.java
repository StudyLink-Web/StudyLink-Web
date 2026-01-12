package com.StudyLink.www.controller;

import com.StudyLink.www.dto.BoardDTO;
import com.StudyLink.www.dto.BoardFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.handler.FileHandler;
import com.StudyLink.www.handler.FileRemoveHandler;
import com.StudyLink.www.handler.PageHandler;
import com.StudyLink.www.service.BoardService;
import com.StudyLink.www.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final FileHandler fileHandler;
    private final UserService userService;

    private static final String UPLOAD_ROOT = "D:/web_0826_shinjw/_myProject/_java/_fileUpload";

    @GetMapping("/register")
    public void register() {}

    @PostMapping("/register")
    public String register(BoardDTO boardDTO,
                           @RequestParam(name = "files", required = false) MultipartFile[] files,
                           Authentication authentication) {

        String username = authentication.getName();
        Long userId = userService.findUserIdByUsername(username);

        boardDTO.setUserId(userId);
        boardDTO.setWriter(username);

        List<FileDTO> fileList = null;
        if (files != null && files.length > 0 && !files[0].isEmpty()) {
            fileList = fileHandler.uploadFile(files);
        }

        boardService.insert(new BoardFileDTO(boardDTO, fileList));
        return "redirect:/board/list";
    }

    @GetMapping("/list")
    public void list(Model model,
                     @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                     @RequestParam(name = "type", required = false) String type,
                     @RequestParam(name = "keyword", required = false) String keyword) {

        Page<BoardDTO> list = boardService.getList(pageNo, type, keyword);
        PageHandler<BoardDTO> pageHandler = new PageHandler<>(list, pageNo, type, keyword);
        model.addAttribute("ph", pageHandler);
    }

    /**
     * ✅ 조회수 정책
     * - "새로고침(F5)" : Referer가 detail이거나 null → 증가 X
     * - "리스트에서 들어옴" : Referer에 /board/list 포함 → 증가 O
     * - "리스트 갔다가 다시 들어옴" : 또 /board/list에서 오므로 증가 O
     */
    @GetMapping("/detail")
    public void detail(@RequestParam("postId") long postId,
                       Model model,
                       HttpServletRequest request) {

        String referer = request.getHeader("Referer");
        boolean fromList = (referer != null && referer.contains("/board/list"));

        if (fromList) {
            try {
                boardService.increaseViewCount(postId);
            } catch (Exception e) {
                log.error("increaseViewCount failed. postId={}", postId, e);
            }
        }

        BoardFileDTO boardFileDTO = boardService.getDetail(postId);
        model.addAttribute("boardFileDTO", boardFileDTO);
    }

    @PostMapping("/modify")
    public String modify(BoardDTO boardDTO,
                         RedirectAttributes redirectAttributes,
                         @RequestParam(name = "files", required = false) MultipartFile[] files,
                         Authentication authentication,
                         HttpSession session) {

        String username = authentication.getName();
        Long userId = userService.findUserIdByUsername(username);

        boardDTO.setUserId(userId);
        boardDTO.setWriter(username);

        List<FileDTO> fileDTOList = null;
        if (files != null && files.length > 0 && !files[0].isEmpty()) {
            fileDTOList = fileHandler.uploadFile(files);
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
    @ResponseBody
    public ResponseEntity<String> fileRemove(@PathVariable("uuid") String uuid) {

        FileDTO removeFile = boardService.getFile(uuid);
        if (removeFile == null) {
            return ResponseEntity.notFound().build();
        }

        FileRemoveHandler fileRemoveHandler = new FileRemoveHandler();
        boolean isDel = fileRemoveHandler.removeFile(removeFile);

        if (!isDel) {
            return ResponseEntity.internalServerError().build();
        }

        Long postId = boardService.fileRemove(uuid);
        return (postId != null && postId > 0)
                ? ResponseEntity.ok("1")
                : ResponseEntity.internalServerError().build();
    }

    // ✅ 파일/이미지 보기: /board/file/{uuid}
    @GetMapping("/file/{uuid}")
    @ResponseBody
    public ResponseEntity<Resource> viewFile(@PathVariable String uuid) throws MalformedURLException {

        FileDTO fileDTO = boardService.getFile(uuid);
        if (fileDTO == null) {
            return ResponseEntity.notFound().build();
        }

        String savedName = fileDTO.getUuid() + "_" + fileDTO.getFileName();
        Path filePath = Paths.get(UPLOAD_ROOT, fileDTO.getSaveDir(), savedName);

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String lower = fileDTO.getFileName().toLowerCase();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (fileDTO.getFileType() == 1) {
            if (lower.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
            else if (lower.endsWith(".gif")) mediaType = MediaType.IMAGE_GIF;
            else mediaType = MediaType.IMAGE_JPEG;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);
    }
}
