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
public class FileHandler {
    // 저장될 파일 데이터 + 직접 폴더에 파일을 저장

    private final String UP_DIR = "D:\\web_0826_shinjw\\_myProject\\_java\\_fileUpload";

    public List<FileDTO> uploadFile(MultipartFile[] files){
        List<FileDTO> fileList = new ArrayList<>();

        // 날짜 형태로 파일 구성
        LocalDate date = LocalDate.now(); // 2025-12-24  - => 파일 경로로 변경
        String today = date.toString().replace("-", File.separator);

        File folders = new File(UP_DIR, today);

        // 해당 폴더가 없으면 생성 / 있으면 생성 안함.
        // mkdir(1개의 폴더 생성) / mkdirs(하위 폴더 동시 생성)
        if(!folders.exists()){
            folders.mkdirs();
        }

        // 파일 정보 생성 => FileDTO 생성
        for(MultipartFile file : files){
            log.info(">>> file contentType >> {}", file.getContentType());
            // file : name, size, type, saveDir
            FileDTO fileDTO = new FileDTO();
            String originalFileName = file.getOriginalFilename();
            fileDTO.setFileName(originalFileName);
            fileDTO.setFileSize(file.getSize());
            fileDTO.setFileType(file.getContentType().startsWith("image")? 1 : 0);
            fileDTO.setSaveDir(today);

            // uuid
            UUID uuid = UUID.randomUUID();
            String uuidString = uuid.toString();
            fileDTO.setUuid(uuidString);

            //--- file save
            String fileName = uuidString+"_"+originalFileName;
            String fileThName = uuidString+"_th_"+originalFileName;

            // 실 저장 객체
            //D: ~/2025/12/24/uuid_name
            File storeFile = new File(folders, fileName);

            // 저장
            try {
                file.transferTo(storeFile);
                // 그림 파일만 썸네일 저장
                if(fileDTO.getFileType()==1){
                    File thumbnail = new File(folders, fileThName);
                    Thumbnails.of(storeFile).size(100,100).toFile(thumbnail);
                }
            } catch (Exception e) {
                log.info("file save Error");
                e.printStackTrace();
            }
            fileList.add(fileDTO);
        }

        return fileList;
    }
}
