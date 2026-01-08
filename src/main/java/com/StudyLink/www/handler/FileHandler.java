package com.StudyLink.www.handler;

import lombok.extern.slf4j.Slf4j;
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
//    private final String UP_DIP = "D:\\web_0826_kkj\\_myProject\\_java\\_fileUpload";
//
//    public void removeFile(FileDTO fileDTO) {
//        String today = fileDTO.getSaveDir();
//
//        File folders = new File(UP_DIP, today);
//
//        // file : name, size, type
//        String originalFileName = fileDTO.getFileName();
//
//        // uuid
//        String uuidString = fileDTO.getUuid();
//
//        // 삭제
//        String fileName = uuidString + "_" + originalFileName;
//        String fileThName = uuidString + "_th_" + originalFileName;
//
//        // D:~/2025/12/24/uuid_fileName
//        File removeFile = new File(folders, fileName);
//        File removeThFile = new File(folders, fileThName);
//        try {
//            removeFile.delete();
//            // 그림파일만 썸네일 저장
//            if (fileDTO.getFileType() == 1) {
//                removeThFile.delete();
//            }
//        } catch (Exception e) {
//            log.info(">>> file save Error");
//            e.printStackTrace();
//        }
//    }
//
//    public List<FileDTO> uploadFile(MultipartFile[] files){
//        List<FileDTO> fileList = new ArrayList<>();
//
//        // 날짜 형태로 파일 구성
//        LocalDate date = LocalDate.now(); // 2025-12-24 => 파일 경로로 변경
//        String today = date.toString().replace("-", File.separator);
//
//        File folders = new File(UP_DIP, today);
//
//        // 해당 폴더가 없으면 생성
//        // mkdir(1개의 폴더 생성) / mkdirs(하위 폴더도 동시에 생성)
//        if (!folders.exists()){
//            folders.mkdirs();
//        }
//
//        // 파일 정보 생성 => FileDTO 생성
//        for (MultipartFile file : files){
//            log.info(">>> file contentType {}", file.getContentType());
//            log.info(">>> file originalFileName {}", file.getOriginalFilename());
//            // file : name, size, type
//            FileDTO fileDTO = new FileDTO();
//            String originalFileName = file.getOriginalFilename();
//            fileDTO.setFileName(originalFileName);
//            fileDTO.setFileSize(file.getSize());
//            fileDTO.setFileType(file.getContentType().startsWith("image") ? 1 : 0);
//            fileDTO.setSaveDir(today);
//
//            // uuid
//            UUID uuid = UUID.randomUUID();
//            String uuidString = uuid.toString();
//            fileDTO.setUuid(uuidString);
//
//            // 저장
//            String fileName = uuidString + "_" + originalFileName;
//            String fileThName = uuidString + "_th_" + originalFileName;
//
//            // 실제 저장 객체
//            // D:~/2025/12/24/uuid_fileName
//            File StoreFile = new File(folders, fileName);
//            try {
//                file.transferTo(StoreFile);
//                // 그림파일만 썸네일 저장
//                if (fileDTO.getFileType() == 1) {
//                    File thumbnail = new File(folders, fileThName);
//                    Thumbnails.of(StoreFile).size(100, 100).toFile(thumbnail);
//                }
//            } catch (Exception e) {
//                log.info(">>> file save Error");
//                e.printStackTrace();
//            }
//            fileList.add(fileDTO);
//        }
//        return fileList;
   // }
}
