package com.StudyLink.www.handler;

import com.StudyLink.www.dto.FileDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FileHandler {
    @Value("${file.board-dir:./_fileUpload}")
    private String UP_DIR;

    // ✅ 절대 경로로 변환된 필드
    private File uploadDirFile;

    // 애플리케이션 시작 시 절대 경로로 변환
    @PostConstruct
    public void init() {
        // 절대 경로로 변환 (상대 경로 제거)
        uploadDirFile = Paths.get(UP_DIR).toAbsolutePath().toFile();

        log.info("========================================");
        log.info("📁 Upload Directory (설정값): {}", UP_DIR);
        log.info("📁 Upload Directory (절대경로): {}", uploadDirFile.getAbsolutePath());
        log.info("📁 Directory exists: {}", uploadDirFile.exists());
        log.info("📁 Can write: {}", uploadDirFile.canWrite());
        log.info("========================================");
    }

    public List<FileDTO> uploadFile(MultipartFile[] files) {

        List<FileDTO> fileList = new ArrayList<>();

        LocalDate date = LocalDate.now();
        String today = date.toString().replace("-", File.separator);

        File folders = new File(uploadDirFile, today);
        if (!folders.exists()) {
            folders.mkdirs();
        }
        log.info(">>> folders {}", folders);
        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) continue;

            String originalFileName = file.getOriginalFilename();
            String contentType = file.getContentType();

            UUID uuid = UUID.randomUUID();
            String uuidString = uuid.toString();

            FileDTO fileDTO = FileDTO.builder()
                    .uuid(uuidString)
                    .fileName(originalFileName)
                    .fileSize(file.getSize())
                    .fileType(contentType != null && contentType.startsWith("image") ? 1 : 0)
                    .saveDir(today)
                    .build();

            String savedFileName = uuidString + "_" + originalFileName;
            File storeFile = new File(folders, savedFileName);

            try {
                // 원본 파일 저장
                file.transferTo(storeFile);

                // 이미지인 경우에만 썸네일 생성 (❗ DB/DTO에는 저장하지 않음)
                if (fileDTO.getFileType() == 1) {
                    String thumbName = uuidString + "_th_" + originalFileName;
                    File thumbnailFile = new File(folders, thumbName);
                    Thumbnails.of(storeFile)
                            .size(100, 100)
                            .toFile(thumbnailFile);
                }

                // ✅ DB에 저장할 DTO는 "원본 파일"만
                fileList.add(fileDTO);

            } catch (Exception e) {
                log.error("file save error", e);
            }
        }

        return fileList;
    }
}
