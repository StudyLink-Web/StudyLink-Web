package com.StudyLink.www.handler;

import com.StudyLink.www.dto.FileDTO;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FileRemoveHandler {
    private final String DIR = "D:\\web_0826_shinjw\\_myProject\\_java\\_fileUpload";

    public boolean removeFile(FileDTO fileDTO){
        // file.delete() // 파일삭제
        // 파일 (이미지라면 썸네일도 같이 삭제)
        boolean isDel = false;

        // 실제 저장되어 있는 경로
        File fileDir = new File(DIR, fileDTO.getSaveDir());

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
