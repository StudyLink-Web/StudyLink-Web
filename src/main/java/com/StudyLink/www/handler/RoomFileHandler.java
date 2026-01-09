package com.StudyLink.www.handler;

import com.StudyLink.www.dto.RoomFileDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
public class RoomFileHandler {

    @Value("${file.upload_dir:./uploads}")
    private String uploadDir;

    // ✅ 절대 경로로 변환된 필드
    private File uploadDirFile;

    // 애플리케이션 시작 시 절대 경로로 변환
    @PostConstruct
    public void init() {
        // 절대 경로로 변환 (상대 경로 제거)
        uploadDirFile = Paths.get(uploadDir).toAbsolutePath().toFile();

        log.info("========================================");
        log.info("📁 Upload Directory (설정값): {}", uploadDir);
        log.info("📁 Upload Directory (절대경로): {}", uploadDirFile.getAbsolutePath());
        log.info("📁 Directory exists: {}", uploadDirFile.exists());
        log.info("📁 Can write: {}", uploadDirFile.canWrite());
        log.info("========================================");
    }

    public void removeFile(RoomFileDTO roomFileDTO) {
        try {
            String today = roomFileDTO.getSaveDir();
            File folders = new File(uploadDirFile, today);  // ✅ uploadDirFile 사용
            String originalFileName = roomFileDTO.getFileName();
            String uuidString = roomFileDTO.getUuid();
            String fileName = uuidString + "_" + originalFileName;

            File removeFile = new File(folders, fileName);
            if (removeFile.delete()) {
                log.info("✅ File deleted: {}", removeFile.getAbsolutePath());
            } else {
                log.warn("⚠️ File deletion failed: {}", removeFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("❌ File delete error", e);
        }
    }

    public RoomFileDTO uploadFile(MultipartFile roomFile) {
        try {
            // 날짜 형태로 파일 경로 생성
            LocalDate date = LocalDate.now();
            String today = date.toString().replace("-", File.separator);
            File folders = new File(uploadDirFile, today);  // ✅ uploadDirFile 사용

            // 디렉토리가 없으면 생성
            if (!folders.exists()) {
                if (folders.mkdirs()) {
                    log.info("✅ Created directory: {}", folders.getAbsolutePath());
                } else {
                    log.error("❌ Failed to create directory: {}", folders.getAbsolutePath());
                    throw new RuntimeException("디렉토리 생성 실패");
                }
            }

            // 파일 정보 출력
            log.info("📤 Uploading file - Type: {}, Name: {}",
                    roomFile.getContentType(), roomFile.getOriginalFilename());

            // RoomFileDTO 생성
            RoomFileDTO roomFileDTO = new RoomFileDTO();
            String originalFileName = roomFile.getOriginalFilename();
            roomFileDTO.setFileName(originalFileName);
            roomFileDTO.setFileSize(roomFile.getSize());
            roomFileDTO.setFileType(roomFile.getContentType() != null &&
                    roomFile.getContentType().startsWith("image") ? 1 : 0);
            roomFileDTO.setSaveDir(today);

            // UUID를 사용한 파일명 생성
            UUID uuid = UUID.randomUUID();
            String uuidString = uuid.toString();
            roomFileDTO.setUuid(uuidString);

            // 최종 파일명
            String fileName = uuidString + "_" + originalFileName;
            File storeFile = new File(folders, fileName);

            log.info("📍 Saving to: {}", storeFile.getAbsolutePath());

            // 파일 저장
            roomFile.transferTo(storeFile);

            // 저장 완료 로그
            log.info("✅ File saved successfully!");
            log.info("   📍 Full path: {}", storeFile.getAbsolutePath());
            log.info("   📊 File size: {} bytes", storeFile.length());
            log.info("   ✓ File exists: {}", storeFile.exists());

            return roomFileDTO;

        } catch (Exception e) {
            log.error("❌ File upload error", e);
            throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
        }
    }
}