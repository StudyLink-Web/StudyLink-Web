package com.StudyLink.www.handler;

import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.repository.FileRepository;
import com.StudyLink.www.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@EnableScheduling
@RequiredArgsConstructor
@Component
public class FileSweeper {
    // 매일 정해진 시간에 스케줄러 실행
    // 매일 해당날짜의 경로에 파일(DB)과 폴더안의 파일이 일치하는지 비교
    // DB == 폴더의 파일이 일치하는지 확인
    // 일치하는 파일(DB == file)만 남기고,
    // 일치하지 않는파일(DB != file)은 삭제 (폴더에서 삭제)

    private final BoardService boardService;
    private final String BASE_PATH = "D:\\web_0826_shinjw\\_myProject\\_java\\_fileUpload\\";

    //cron 방식 = 초 분 시 일 월 요일 년도(생략가능)
    @Scheduled(cron = "0 37 17 * * * ")
    public void fileSweeper(){
        log.info(">>>> fileSweeper Start >> {}", LocalDateTime.now());

        // DB에 등록된 파일 리스트 가져오기
        // save_dir 정보필요.
        LocalDate now = LocalDate.now();
        String today = now.toString().replace("-", File.separator);

        // select * from file where save_dir = today
        List<FileDTO> dbFileList = boardService.getTodayFileList(today);
        log.info(">>> dbFileList >> {}", dbFileList);

        List<String> currFile = new ArrayList<>();
        for(FileDTO fileDTO : dbFileList){
            String fileName = today+ File.separator+fileDTO.getUuid()
                    +"_"+fileDTO.getFileName();
            currFile.add(BASE_PATH+fileName);
            // 이미지 파일이라면 썸네일도 추가
            if(fileDTO.getFileType() == 1){
                String thFileName = today+ File.separator+fileDTO.getUuid()
                        +"_th_"+fileDTO.getFileName();
                currFile.add(BASE_PATH+thFileName);
            }
        }
        log.info(">>>> currFile >> {}", currFile);

        // today 경로 기반 저장된 파일 검색(경로만)
        // D:\web_0826_omr\_myProject\_java\_fileUpload\2025\12\26
        File dir = Paths.get(BASE_PATH+today).toFile();

        // 경로안에 있는 파일을 가져오기 (배열로 리턴)
        File[] allFileObject = dir.listFiles();

        // allfileObject와 DB에 있는 파일과 비교
        // DB에 없는 (존재하지 않는) 파일은 삭제
        for(File file : allFileObject){
            String storedFileName = file.toPath().toString();
            if(!currFile.contains(storedFileName)){
                file.delete();  // 리스트에 없다면 삭제
                log.info(">>> delete file >> {}", storedFileName);
            }
        }
        log.info(">>>> fileSweeper End >> {}", LocalDateTime.now());
    }
}

