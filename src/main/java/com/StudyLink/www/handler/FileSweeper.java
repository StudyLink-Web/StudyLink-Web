package com.StudyLink.www.handler;

import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.service.BoardService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class FileSweeper {

    private final BoardService boardService;

    @Value("${file.board-dir:./_fileUpload}")
    private String BASE_PATH;

    // ✅ 절대 경로로 변환된 필드
    private File uploadDirFile;

    // 애플리케이션 시작 시 절대 경로로 변환
    @PostConstruct
    public void init() {
        // 절대 경로로 변환 (상대 경로 제거)
        uploadDirFile = Paths.get(BASE_PATH).toAbsolutePath().toFile();

        log.info("========================================");
        log.info("📁 Upload Directory (설정값): {}", BASE_PATH);
        log.info("📁 Upload Directory (절대경로): {}", uploadDirFile.getAbsolutePath());
        log.info("📁 Directory exists: {}", uploadDirFile.exists());
        log.info("📁 Can write: {}", uploadDirFile.canWrite());
        log.info("========================================");
    }

    // cron = 초 분 시 일 월 요일
    @Scheduled(cron = "0 37 17 * * *")
    public void fileSweeper() {
        log.info(">>>> fileSweeper Start >> {}", LocalDateTime.now());

        LocalDate now = LocalDate.now();
        String today = now.toString().replace("-", File.separator); // 예: 2026\01\08

        // DB에 등록된 파일 리스트 가져오기
        List<FileDTO> dbFileList = boardService.getTodayFileList(today);
        if (dbFileList == null) {
            dbFileList = List.of();
        }
        log.info(">>> dbFileList size >> {}", dbFileList.size());

        // DB에 있는 파일의 "전체 경로" 목록 만들기
        List<String> currFile = new ArrayList<>();
        for (FileDTO fileDTO : dbFileList) {
            String fileName = today + File.separator + fileDTO.getUuid() + "_" + fileDTO.getFileName();
            currFile.add(uploadDirFile + fileName);

            // 이미지 파일이면 썸네일도 포함
            if (fileDTO.getFileType() == 1) {
                String thFileName = today + File.separator + fileDTO.getUuid() + "_th_" + fileDTO.getFileName();
                currFile.add(uploadDirFile + thFileName);
            }
        }
        log.info(">>>> currFile size >> {}", currFile.size());

        // 오늘 날짜 폴더
        Path dirPath = Paths.get(uploadDirFile.getAbsolutePath(), today);

        // ✅ 폴더가 없으면 종료 (NPE 방지)
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            log.info(">>>> skip: directory not found >> {}", dirPath);
            log.info(">>>> fileSweeper End >> {}", LocalDateTime.now());
            return;
        }

        File[] allFileObject = dirPath.toFile().listFiles();
        if (allFileObject == null || allFileObject.length == 0) {
            log.info(">>>> skip: no files in directory >> {}", dirPath);
            log.info(">>>> fileSweeper End >> {}", LocalDateTime.now());
            return;
        }

        // 폴더 파일들과 DB 목록 비교해서 DB에 없는 파일 삭제
        for (File file : allFileObject) {
            String storedFileName = file.toPath().toString();

            if (!currFile.contains(storedFileName)) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info(">>> delete file >> {}", storedFileName);
                } else {
                    log.warn(">>> failed to delete file >> {}", storedFileName);
                }
            }
        }

        log.info(">>>> fileSweeper End >> {}", LocalDateTime.now());
    }
}
