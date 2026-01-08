package com.StudyLink.www.dto;

import com.StudyLink.www.entity.RoomFile;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
public class RoomFileDTO {
    private String uuid;
    private String saveDir;
    private String fileName;
    private int fileType;
    private long roomId;
    private long fileSize;

    public RoomFileDTO(RoomFile roomFile) {
        this.uuid = roomFile.getUuid();
        this.saveDir = roomFile.getSaveDir();
        this.fileName = roomFile.getFileName();
        this.fileType = roomFile.getFileType();
        this.roomId = roomFile.getRoomId();
        this.fileSize = roomFile.getFileSize();
    }
}
