package com.StudyLink.www.handler;

import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.service.BoardService;   // ✅ BoardService import 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // 운영환경에선 application.yml로 빼는 걸 추천
    private static final String BASE_PATH = "D:\\web_0826_shinjw\\_myProject\\_java\\_fileUpload\\";

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
            currFile.add(BASE_PATH + fileName);

            // 이미지 파일이면 썸네일도 포함
            if (fileDTO.getFileType() == 1) {
                String thFileName = today + File.separator + fileDTO.getUuid() + "_th_" + fileDTO.getFileName();
                currFile.add(BASE_PATH + thFileName);
            }
        }
        log.info(">>>> currFile size >> {}", currFile.size());

        // 오늘 날짜 폴더
        Path dirPath = Paths.get(BASE_PATH, today);

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
