package com.StudyLink.www.handler;

import com.StudyLink.www.dto.FileDTO;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
public class FileRemoveHandler {
    @Value("${file.board-dir:./_fileUpload}")
    private String DIR;

    // ✅ 절대 경로로 변환된 필드
    private File uploadDirFile;

    // 애플리케이션 시작 시 절대 경로로 변환
    @PostConstruct
    public void init() {
        // 절대 경로로 변환 (상대 경로 제거)
        uploadDirFile = Paths.get(DIR).toAbsolutePath().toFile();

        log.info("========================================");
        log.info("📁 Upload Directory (설정값): {}", DIR);
        log.info("📁 Upload Directory (절대경로): {}", uploadDirFile.getAbsolutePath());
        log.info("📁 Directory exists: {}", uploadDirFile.exists());
        log.info("📁 Can write: {}", uploadDirFile.canWrite());
        log.info("========================================");
    }

    public boolean removeFile(FileDTO fileDTO){
        // file.delete() // 파일삭제
        // 파일 (이미지라면 썸네일도 같이 삭제)
        boolean isDel = false;

        // 실제 저장되어 있는 경로
        File fileDir = new File(uploadDirFile, fileDTO.getSaveDir());

        String removeFile = fileDTO.getUuid()+"_"+fileDTO.getFileName();
        String removeThFile = fileDTO.getUuid()+"_th_"+fileDTO.getFileName();

        File deleteFile = new File(fileDir, removeFile);
        File deleteThFile = new File(fileDir, removeThFile);

        try {
            // 파일 존재하는지 확인
            if(deleteFile.exists()){
                isDel = deleteFile.delete();  //삭제
                log.info(">>> deleteFile success >> {}", deleteFile);
                if(isDel && fileDTO.getFileType() == 1 && deleteThFile.exists()){
                    isDel = deleteThFile.delete();
                    log.info(">>> deleteThFile success >> {}", deleteThFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isDel;
    }
}
