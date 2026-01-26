// src/main/java/com/StudyLink/www/controller/CommunityController.java
package com.StudyLink.www.controller;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.dto.CommunityFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.handler.PageHandler;
import com.StudyLink.www.service.CommunityService;
import com.StudyLink.www.service.CommunityServiceImpl;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
@RequestMapping("/community")
public class CommunityController {

    private final CommunityService communityService;
    private final UserService userService;

    // ✅ ServiceImpl 업로드 메서드 쓰려면 주입(최소 변경)
    private final CommunityServiceImpl communityServiceImpl;

    private static final String UPLOAD_ROOT = "D:/web_0826_shinjw/_myProject/_java/_fileUpload";

    private boolean isLogin(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String loginEmail(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    private Long loginUserId(Authentication authentication) {
        String email = loginEmail(authentication);
        if (email == null || email.isBlank()) return null;
        return userService.findUserIdByUsername(email);
    }

    @GetMapping("/register")
    public String register(Authentication authentication, Model model) {
        if (!isLogin(authentication)) return "redirect:/login";
        model.addAttribute("loginEmail", authentication.getName());
        return "community/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute CommunityDTO communityDTO,
                           @RequestParam(value = "files", required = false) MultipartFile[] files,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

        if (!isLogin(authentication)) return "redirect:/login";

        String email = loginEmail(authentication);
        Long userId = loginUserId(authentication);
        if (userId == null) return "redirect:/login";

        communityDTO.setUserId(userId);
        communityDTO.setEmail(email);
        communityDTO.setWriter(email);
        communityDTO.setRole("USER");
        communityDTO.setReadCount(0);
        communityDTO.setCmtQty(0);

        List<FileDTO> uploaded = communityServiceImpl.uploadAndFilter(files);

        Long savedBno = communityService.insert(
                CommunityFileDTO.builder()
                        .communityDTO(communityDTO)
                        .fileDTOList(uploaded)
                        .build()
        );

        redirectAttributes.addAttribute("bno", savedBno);
        return "redirect:/community/detail";
    }

    // ✅✅✅ 여기(list)만 수정: type/keyword 반영
    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(defaultValue = "1") int pageNo,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String keyword) {

        int safePageNo = Math.max(pageNo, 1);

        // ✅ 검색/정렬 반영된 서비스 메서드 호출
        Page<CommunityDTO> page = communityService.getList(safePageNo, type, keyword);

        PageHandler<CommunityDTO> ph = new PageHandler<>(page, safePageNo, type, keyword);

        model.addAttribute("ph", ph);

        // ✅ 템플릿에서 list를 직접 쓰는 경우를 대비(있어도 문제 없음)
        model.addAttribute("list", ph.getList());

        return "community/list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam Long bno,
                         @RequestParam(required = false) String mode,
                         Model model,
                         Authentication authentication,
                         HttpServletRequest request) {

        CommunityFileDTO communityFileDTO = communityService.getDetail(bno);
        if (communityFileDTO == null || communityFileDTO.getCommunityDTO() == null) return "error/404";

        boolean modifyMode = "modify".equalsIgnoreCase(mode);

        String referer = request.getHeader("Referer");
        boolean fromList = referer != null && referer.contains("/community/list");
        if (fromList && !modifyMode) {
            communityService.increaseReadCount(bno);
            Integer rc = communityFileDTO.getCommunityDTO().getReadCount();
            communityFileDTO.getCommunityDTO().setReadCount((rc == null ? 0 : rc) + 1);
        }

        if (modifyMode) {
            if (!isLogin(authentication)) return "redirect:/login";
            if (!loginEmail(authentication).equals(communityFileDTO.getCommunityDTO().getWriter())) {
                return "redirect:/community/detail?bno=" + bno;
            }
        }

        model.addAttribute("communityFileDTO", communityFileDTO);
        model.addAttribute("mode", modifyMode ? "modify" : "read");
        return "community/detail";
    }

    @PostMapping("/modify")
    public String modify(@ModelAttribute CommunityDTO communityDTO,
                         @RequestParam(value = "files", required = false) MultipartFile[] files,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {

        if (!isLogin(authentication)) return "redirect:/login";

        CommunityFileDTO origin = communityService.getDetail(communityDTO.getBno());
        if (origin == null || origin.getCommunityDTO() == null) return "error/404";

        if (!loginEmail(authentication).equals(origin.getCommunityDTO().getWriter())) {
            return "redirect:/community/detail?bno=" + communityDTO.getBno();
        }

        communityDTO.setUserId(origin.getCommunityDTO().getUserId());
        communityDTO.setEmail(origin.getCommunityDTO().getEmail());
        communityDTO.setWriter(origin.getCommunityDTO().getWriter());
        communityDTO.setRole(origin.getCommunityDTO().getRole());
        communityDTO.setReadCount(origin.getCommunityDTO().getReadCount());
        communityDTO.setCmtQty(origin.getCommunityDTO().getCmtQty());

        List<FileDTO> uploaded = communityServiceImpl.uploadAndFilter(files);

        Long savedBno = communityService.modify(
                CommunityFileDTO.builder()
                        .communityDTO(communityDTO)
                        .fileDTOList(uploaded) // ✅ 새 파일 없으면 null/empty로 보내면 기존 유지
                        .build()
        );

        redirectAttributes.addAttribute("bno", savedBno);
        return "redirect:/community/detail";
    }

    @GetMapping("/remove")
    public String remove(@RequestParam Long bno,
                         Authentication authentication) {

        if (!isLogin(authentication)) return "redirect:/login";

        CommunityFileDTO origin = communityService.getDetail(bno);
        if (origin == null || origin.getCommunityDTO() == null) return "error/404";

        if (!loginEmail(authentication).equals(origin.getCommunityDTO().getWriter())) {
            return "redirect:/community/detail?bno=" + bno;
        }

        communityService.remove(bno);
        return "redirect:/community/list";
    }

    @GetMapping("/file/{uuid}")
    @ResponseBody
    public ResponseEntity<Resource> viewFile(@PathVariable String uuid) throws MalformedURLException {

        FileDTO fileDTO = communityService.getFile(uuid);
        if (fileDTO == null) return ResponseEntity.notFound().build();

        String savedName = fileDTO.getUuid() + "_" + fileDTO.getFileName();
        String normalizedDir = (fileDTO.getSaveDir() == null) ? "" : fileDTO.getSaveDir().replace("\\", "/");

        Path filePath = Paths.get(UPLOAD_ROOT);
        if (!normalizedDir.isBlank()) {
            for (String part : normalizedDir.split("/")) {
                if (!part.isBlank()) filePath = filePath.resolve(part);
            }
        }
        filePath = filePath.resolve(savedName).normalize();

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

        String lower = fileDTO.getFileName().toLowerCase();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (fileDTO.getFileType() == 1) {
            if (lower.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
            else if (lower.endsWith(".gif")) mediaType = MediaType.IMAGE_GIF;
            else if (lower.endsWith(".webp")) mediaType = MediaType.valueOf("image/webp");
            else mediaType = MediaType.IMAGE_JPEG;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);
    }
}
