package com.StudyLink.www.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${profile.image.upload-dir:uploads/profile}")
    private String profileImageUploadDir;

    @Value("${profile.image.max-size:5242880}")
    private long maxFileSize;

    public String saveProfileImage(MultipartFile file, Long userId) throws IOException {
        log.info("🎬 [파일 저장] 시작 - userId: {}", userId);

        try {
            // 1️⃣ 파일 검증
            log.info("1️⃣ 파일 검증");
            if (file == null || file.isEmpty()) {
                log.error("❌ 파일이 비어있습니다");
                throw new IllegalArgumentException("파일이 비어있습니다");
            }

            log.info("   ✓ 파일명: {}", file.getOriginalFilename());
            log.info("   ✓ 파일 크기: {} bytes", file.getSize());
            log.info("   ✓ Content-Type: {}", file.getContentType());

            if (file.getSize() > maxFileSize) {
                log.error("❌ 파일 크기 초과: {} > {}", file.getSize(), maxFileSize);
                throw new IllegalArgumentException("파일 크기가 5MB를 초과합니다");
            }

            if (!file.getContentType().startsWith("image/")) {
                log.error("❌ 이미지가 아님: {}", file.getContentType());
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
            }

            // 2️⃣ 저장 경로 생성
            log.info("2️⃣ 저장 경로 생성");
            Path userDir = Paths.get(profileImageUploadDir, "user-" + userId);
            Files.createDirectories(userDir);
            log.info("   ✓ 디렉토리 생성: {}", userDir);

            // 3️⃣ 파일명 생성
            log.info("3️⃣ 파일명 생성");
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String filename = "profile_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            Path filePath = userDir.resolve(filename);
            log.info("   ✓ 파일경로: {}", filePath);

            // 4️⃣ 파일 저장
            log.info("4️⃣ 파일 저장 시작");
            byte[] fileBytes = file.getBytes();
            Files.write(filePath, fileBytes);
            log.info("   ✓ 파일 저장 완료: {} bytes", fileBytes.length);

            // 5️⃣ 웹에서 접근 가능한 경로 반환
            log.info("5️⃣ 접근 경로 생성");
            String accessPath = "/uploads/profile/user-" + userId + "/" + filename;
            log.info("   ✓ 접근 경로: {}", accessPath);

            log.info("✅ [파일 저장] 완료!");
            return accessPath;

        } catch (IllegalArgumentException e) {
            log.error("❌ 유효성 검사 실패: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("❌ [파일 저장] 오류 발생!", e);
            throw new IOException("파일 저장 실패: " + e.getMessage(), e);
        }
    }

    public void deleteProfileImage(Long userId) throws IOException {
        try {
            log.info("🎬 [파일 삭제] 시작 - userId: {}", userId);

            Path userDir = Paths.get(uploadDir, "profile", "user-" + userId);

            if (Files.exists(userDir)) {
                Files.list(userDir)
                        .forEach(file -> {
                            try {
                                Files.delete(file);
                                log.info("   ✓ 파일 삭제: {}", file.getFileName());
                            } catch (IOException e) {
                                log.error("   ❌ 파일 삭제 실패: {}", file.getFileName(), e);
                            }
                        });

                Files.deleteIfExists(userDir);
                log.info("   ✓ 디렉토리 삭제됨");
            }

            log.info("✅ [파일 삭제] 완료!");

        } catch (Exception e) {
            log.error("❌ [파일 삭제] 오류 발생!", e);
            throw new IOException("파일 삭제 실패: " + e.getMessage(), e);
        }
    }
}