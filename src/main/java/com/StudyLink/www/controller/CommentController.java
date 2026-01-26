package com.StudyLink.www.controller;

import com.StudyLink.www.dto.CommunityFileDTO;
import com.StudyLink.www.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/community")
public class CommentController {

    private final CommunityService communityService;

    @Value("${app.upload.root:D:/upload}")
    private String uploadRoot;

    @GetMapping("/file/{uuid}")
    @ResponseBody
    public ResponseEntity<Resource> file(@PathVariable String uuid) {
        CommunityFileDTO dto = communityService.getFileByUuid(uuid);
        if (dto == null) return ResponseEntity.notFound().build();

        // ✅ 실제 저장 위치: {uploadRoot}/community/{saveDir}/{uuid}
        Path filePath = Paths.get(uploadRoot, "community", dto.getSaveDir(), dto.getUuid());
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists() || !resource.isReadable()) {
            log.error("file not found. path={}", filePath);
            return ResponseEntity.notFound().build();
        }

        String encodedName = URLEncoder.encode(dto.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedName);

        // 이미지면 브라우저에 뜨게
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (dto.getFileType() == 1) {
            mediaType = MediaTypeFactory.getMediaType(dto.getFileName()).orElse(MediaType.IMAGE_JPEG);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(resource);
    }
}
