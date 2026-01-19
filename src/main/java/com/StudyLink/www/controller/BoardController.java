package com.StudyLink.www.controller;

import com.StudyLink.www.dto.BoardDTO;
import com.StudyLink.www.dto.BoardFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.handler.FileHandler;
import com.StudyLink.www.handler.FileRemoveHandler;
import com.StudyLink.www.handler.PageHandler;
import com.StudyLink.www.service.BoardService;
import com.StudyLink.www.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    /**
     * ✅ 등록 폼: "DB에서 role 실시간 체크"로 MENTOR만 허용
     */
    @PreAuthorize("@authz.isMentor(authentication)")
    @GetMapping("/register")
    public String register(Authentication authentication) {
        // 여기선 @PreAuthorize가 이미 다 걸러줌 (방어적으로 남겨도 됨)
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "board/register";
    }

    /**
     * ✅ 등록 처리: "DB에서 role 실시간 체크"로 MENTOR만 허용
     */
    @PreAuthorize("@authz.isMentor(authentication)")
    @PostMapping("/register")
    public String register(BoardDTO boardDTO,
                           @RequestParam(name = "files", required = false) MultipartFile[] files,
                           Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // principal.getName() 값(=identifier)을 email/nickname 둘 다 대응하도록 UserService에서 처리
        String identifier = authentication.getName();
        Long userId = userService.findUserIdByIdentifier(identifier);

        boardDTO.setUserId(userId);
        boardDTO.setWriter(identifier); // writer를 nickname으로 저장하고 싶으면 여기에서 바꿔도 됨

        List<FileDTO> fileList = null;
        if (files != null && files.length > 0 && files[0] != null && !files[0].isEmpty()) {
            fileList = fileHandler.uploadFile(files);
            if (fileList != null) {
                fileList = fileList.stream()
                        .filter(f -> f != null)
                        .filter(f -> {
                            String u = f.getUuid();
                            String n = f.getFileName();
                            boolean isThumb =
                                    (u != null && u.startsWith("_th_")) ||
                                            (n != null && n.contains("_th_"));
                            return !isThumb;
                        })
                        .toList();
            }
        }

        boardService.insert(new BoardFileDTO(boardDTO, fileList));
        return "redirect:/board/list";
    }

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                       @RequestParam(name = "type", required = false) String type,
                       @RequestParam(name = "keyword", required = false) String keyword) {

        Page<BoardDTO> page = boardService.getList(pageNo, type, keyword);
        PageHandler<BoardDTO> ph = new PageHandler<>(page, pageNo, type, keyword);
        model.addAttribute("ph", ph);

        return "board/list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("postId") long postId,
                         @RequestParam(name = "mode", defaultValue = "read") String mode,
                         Model model,
                         HttpServletRequest request) {

        String referer = request.getHeader("Referer");
        boolean fromList = (referer != null && referer.contains("/board/list"));
        boolean isModifyMode = "modify".equalsIgnoreCase(mode);

        if (fromList && !isModifyMode) {
            try {
                boardService.increaseViewCount(postId);
            } catch (Exception e) {
                log.error("increaseViewCount failed. postId={}", postId, e);
            }
        }

        BoardFileDTO boardFileDTO = boardService.getDetail(postId);
        model.addAttribute("boardFileDTO", boardFileDTO);
        model.addAttribute("mode", mode);

        return "board/detail";
    }

    // ✅ 수정 처리: 인증 필수 + 작성자 검증
    @PostMapping("/modify")
    public String modify(BoardDTO boardDTO,
                         RedirectAttributes redirectAttributes,
                         @RequestParam(name = "files", required = false) MultipartFile[] files,
                         Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String identifier = authentication.getName();
        Long userId = userService.findUserIdByIdentifier(identifier);

        BoardFileDTO origin = boardService.getDetail(boardDTO.getPostId());
        if (origin == null || origin.getBoardDTO() == null ||
                origin.getBoardDTO().getUserId() == null ||
                !origin.getBoardDTO().getUserId().equals(userId)) {
            return "redirect:/error/403";
        }

        boardDTO.setUserId(userId);
        boardDTO.setWriter(identifier);

        List<FileDTO> fileDTOList = null;
        if (files != null && files.length > 0 && files[0] != null && !files[0].isEmpty()) {
            fileDTOList = fileHandler.uploadFile(files);
            if (fileDTOList != null) {
                fileDTOList = fileDTOList.stream()
                        .filter(f -> f != null)
                        .filter(f -> {
                            String u = f.getUuid();
                            String n = f.getFileName();
                            boolean isThumb =
                                    (u != null && u.startsWith("_th_")) ||
                                            (n != null && n.contains("_th_"));
                            return !isThumb;
                        })
                        .toList();
            }
        }

        Long postId = boardService.modify(new BoardFileDTO(boardDTO, fileDTOList));

        redirectAttributes.addAttribute("postId", postId);
        return "redirect:/board/detail";
    }

    // ✅ 삭제: 인증 필수 + 작성자 검증
    @GetMapping("/remove")
    public String remove(@RequestParam("postId") long postId,
                         Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String identifier = authentication.getName();
        Long userId = userService.findUserIdByIdentifier(identifier);

        BoardFileDTO origin = boardService.getDetail(postId);
        if (origin == null || origin.getBoardDTO() == null ||
                origin.getBoardDTO().getUserId() == null ||
                !origin.getBoardDTO().getUserId().equals(userId)) {
            return "redirect:/error/403";
        }

        boardService.remove(postId);
        return "redirect:/board/list";
    }

    // ✅ 파일 삭제: 인증 필수 + 작성자 검증
    @DeleteMapping("/file/{uuid}")
    @ResponseBody
    public ResponseEntity<String> fileRemove(@PathVariable("uuid") String uuid,
                                             Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("0");
        }

        String identifier = authentication.getName();
        Long userId = userService.findUserIdByIdentifier(identifier);

        FileDTO removeFile = boardService.getFile(uuid);
        if (removeFile == null) {
            return ResponseEntity.notFound().build();
        }

        BoardFileDTO origin = boardService.getDetail(removeFile.getPostId());
        if (origin == null || origin.getBoardDTO() == null ||
                origin.getBoardDTO().getUserId() == null ||
                !origin.getBoardDTO().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("0");
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
