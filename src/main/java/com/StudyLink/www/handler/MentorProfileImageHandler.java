package com.StudyLink.www.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 멘토 프로필 이미지 처리 (저장, 삭제, 유효성 검사)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MentorProfileImageHandler {

    @Value("${upload.profile-image-dir:uploads/profiles}")
    private String uploadDir;

    @Value("${upload.max-file-size:10485760}") // 10MB
    private long maxFileSize;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp")
    );

    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp")
    );

    /**
     * 프로필 이미지 저장
     *
     * @param file     업로드 파일
     * @param userId   사용자 ID
     * @return 저장된 파일의 URL
     * @throws IOException 파일 저장 실패 시
     */
    public String saveProfileImage(MultipartFile file, Long userId) throws IOException {
        // 1️⃣ 파일 유효성 검사
        validateFile(file);

        // 2️⃣ 저장 디렉토리 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                log.warn("⚠️  업로드 디렉토리 생성 실패: {}", uploadDir);
            }
        }

        // 3️⃣ 파일명 생성 (기존 파일 덮어쓰기 위해 userId 기반)
        String filename = generateFilename(userId, file.getOriginalFilename());
        String filepath = uploadDir + File.separator + filename;

        log.info("📸 프로필 이미지 저장: {}", filepath);

        // 4️⃣ 파일 저장
        file.transferTo(new File(filepath));

        // 5️⃣ 반환 URL (상대경로)
        String imageUrl = "/uploads/profiles/" + filename;
        log.info("✅ 이미지 저장 완료: {}", imageUrl);

        return imageUrl;
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일 크기 검사
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다 (최대 10MB)");
        }

        // 확장자 검사
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("잘못된 파일명입니다");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다 (jpg, png, gif, webp만 가능)");
        }

        // MIME 타입 검사
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("잘못된 파일 형식입니다");
        }
    }

    /**
     * 파일명 생성
     * 포맷: user_{userId}_{timestamp}.{extension}
     */
    private String generateFilename(Long userId, String originalFilename) {
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".png";

        return "user_" + userId + "_" + System.currentTimeMillis() + extension;
    }

    /**
     * 기존 프로필 이미지 삭제
     *
     * @param imageUrl 기존 이미지 URL (예: /uploads/profiles/user_1_1234567890.jpg)
     */
    public void deleteProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("/uploads/profiles/")) {
            return;
        }

        try {
            // URL에서 파일명 추출
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String filepath = uploadDir + File.separator + filename;

            Path path = Paths.get(filepath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("✅ 기존 이미지 삭제: {}", filepath);
            }
        } catch (IOException e) {
            log.warn("⚠️  이미지 삭제 실패: {}", imageUrl, e);
            // 이미지 삭제 실패해도 진행 (매우 중요하지 않음)
        }
    }

    /**
     * 이미지 URL이 유효한지 확인
     */
    public boolean isValidImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }

        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String filepath = uploadDir + File.separator + filename;
            return Files.exists(Paths.get(filepath));
        } catch (Exception e) {
            return false;
        }
    }
}
