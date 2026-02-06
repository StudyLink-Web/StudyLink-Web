package com.StudyLink.www.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.board-dir:./_fileUpload}")
    private String DIR;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + uploadDir + "/");
            System.out.println("✅ 파일 저장소 설정 완료: " + uploadDir);
        } catch (Exception e) {
            System.err.println("❌ 파일 저장소 초기화 실패: " + e.getMessage());
        }

        try {
            Files.createDirectories(Paths.get(DIR));
            registry.addResourceHandler("/_fileUpload/**")
                    .addResourceLocations("file:" + DIR + "/");
            System.out.println("✅ 파일 저장소 설정 완료: " + DIR);
        } catch (Exception e) {
            System.err.println("❌ 파일 저장소 초기화 실패: " + e.getMessage());
        }
    }
}
