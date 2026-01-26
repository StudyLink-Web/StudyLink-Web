package com.StudyLink.www.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String saveProfileImage(String base64Data, Long userId) throws IOException {
        try {
            log.info("🎬 [파일 저장] 시작 - userId: {}", userId);

            // 1️⃣ Base64 데이터 파싱
            log.info("1️⃣ Base64 데이터 파싱");
            String[] parts = base64Data.split(",");
            if (parts.length < 2) {
                throw new IllegalArgumentException("잘못된 Base64 포맷: data:image/...;base64,...");
            }

            String mimeType = parts[0].split("/")[1].split(";")[0];
            log.info("   ✓ MIME 타입: image/{}", mimeType);

            String base64Image = parts[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            log.info("   ✓ Base64 디코딩 완료: {} bytes", imageBytes.length);

            // 2️⃣ 저장 경로 생성
            log.info("2️⃣ 저장 경로 생성");
            Path userDir = Paths.get(uploadDir, "profile", "user-" + userId);
            Files.createDirectories(userDir);
            log.info("   ✓ 디렉토리 생성: {}", userDir);

            // 3️⃣ 파일명 생성
            log.info("3️⃣ 파일명 생성");
            String filename = "profile." + mimeType;
            Path filePath = userDir.resolve(filename);
            log.info("   ✓ 파일경로: {}", filePath);

            // 4️⃣ 기존 파일 삭제 (있으면)
            log.info("4️⃣ 기존 파일 처리");
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("   ✓ 기존 파일 삭제됨");
            }

            // 5️⃣ 파일 저장
            log.info("5️⃣ 파일 저장 시작");
            Files.write(filePath, imageBytes);
            log.info("   ✓ 파일 저장 완료: {} bytes", imageBytes.length);

            // 6️⃣ 웹에서 접근 가능한 경로 반환
            log.info("6️⃣ 접근 경로 생성");
            String accessPath = "/uploads/profile/user-" + userId + "/" + filename;
            log.info("   ✓ 접근 경로: {}", accessPath);

            log.info("✅ [파일 저장] 완료!");
            return accessPath;

        } catch (Exception e) {
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
