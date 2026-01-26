package com.StudyLink.www.handler;

import com.StudyLink.www.dto.FileDTO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class CommunityFileHandler {

    // ✅ 커뮤니티 전용 업로드 루트 (반드시 이 경로로 저장)
    private static final String UP_DIR = "D:\\web_0826_shinjw\\_myProject\\_java\\_fileUpload";

    public List<FileDTO> uploadFile(MultipartFile[] files) {

        List<FileDTO> fileList = new ArrayList<>();

        if (files == null || files.length == 0) return fileList;

        LocalDate date = LocalDate.now();
        String today = date.toString().replace("-", File.separator); // ex) 2026\01\26

        File folders = new File(UP_DIR, today);
        if (!folders.exists()) {
            folders.mkdirs();
        }

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) continue;

            String originalFileName = file.getOriginalFilename();
            String contentType = file.getContentType();

            String uuidString = UUID.randomUUID().toString();

            FileDTO fileDTO = FileDTO.builder()
                    .uuid(uuidString)
                    .fileName(originalFileName)
                    .fileSize(file.getSize())
                    .fileType(contentType != null && contentType.startsWith("image") ? 1 : 0)
                    // ✅ DB에는 슬래시(/)로 저장해서 컨트롤러/서비스에서 경로 조립이 안정적
                    .saveDir(today.replace(File.separatorChar, '/')) // ex) 2026/01/26
                    .build();

            String savedFileName = uuidString + "_" + originalFileName;
            File storeFile = new File(folders, savedFileName);

            try {
                // 원본 파일 저장
                file.transferTo(storeFile);

                // 이미지인 경우 썸네일 생성(파일만 생성, DB/DTO에는 저장 안 함)
                if (fileDTO.getFileType() == 1) {
                    String thumbName = uuidString + "_th_" + originalFileName;
                    File thumbnailFile = new File(folders, thumbName);
                    Thumbnails.of(storeFile)
                            .size(100, 100)
                            .toFile(thumbnailFile);
                }

                fileList.add(fileDTO);

            } catch (Exception e) {
                log.error("community file save error", e);
            }
        }

        return fileList;
    }
}
