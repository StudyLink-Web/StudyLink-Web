package com.StudyLink.www.handler;

import com.StudyLink.www.dto.RoomFileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
public class RoomFileHandler {
    // 저장될 파일 데이터 + 직접 폴더에 파일을 저장
    @Value("${file.upload_dir}")
    private String UP_DIP;

    public void removeFile(RoomFileDTO roomFileDTO) {
        String today = roomFileDTO.getSaveDir();

        File folders = new File(UP_DIP, today);

        // file : name, size, type
        String originalFileName = roomFileDTO.getFileName();

        // uuid
        String uuidString = roomFileDTO.getUuid();

        // 삭제
        String fileName = uuidString + "_" + originalFileName;

        // D:~/2025/12/24/uuid_fileName
        File removeFile = new File(folders, fileName);
        try {
            removeFile.delete();
        } catch (Exception e) {
            log.info(">>> file delete Error");
            e.printStackTrace();
        }
    }

    public RoomFileDTO uploadFile(MultipartFile roomFile){
        // 날짜 형태로 파일 구성
        LocalDate date = LocalDate.now(); // 2025-12-24 => 파일 경로로 변경
        String today = date.toString().replace("-", File.separator);

        File folders = new File(UP_DIP, today);

        // 해당 폴더가 없으면 생성
        // mkdir(1개의 폴더 생성) / mkdirs(하위 폴더도 동시에 생성)
        if (!folders.exists()){
            folders.mkdirs();
        }

        // 파일 정보 생성 => FileDTO 생성
        log.info(">>> file contentType {}", roomFile.getContentType());
        log.info(">>> file originalFileName {}", roomFile.getOriginalFilename());
        // file : name, size, type
        RoomFileDTO roomFileDTO = new RoomFileDTO();
        String originalFileName = roomFile.getOriginalFilename();
        roomFileDTO.setFileName(originalFileName);
        roomFileDTO.setFileSize(roomFile.getSize());
        roomFileDTO.setFileType(roomFile.getContentType().startsWith("image") ? 1 : 0);
        roomFileDTO.setSaveDir(today);

        // uuid
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        roomFileDTO.setUuid(uuidString);

        // 저장
        String fileName = uuidString + "_" + originalFileName;

        // 실제 저장 객체
        // D:~/2025/12/24/uuid_fileName
        File StoreFile = new File(folders, fileName);
        try {
            roomFile.transferTo(StoreFile);
        } catch (Exception e) {
            log.info(">>> file save Error");
            e.printStackTrace();
        }
        return roomFileDTO;
    }
}
