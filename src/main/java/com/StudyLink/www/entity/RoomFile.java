package com.StudyLink.www.entity;

import com.StudyLink.www.dto.RoomFileDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "room_file")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomFile {
    @Id
    private String uuid;

    @Column(name = "save_dir", nullable = false)
    private String saveDir;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false, columnDefinition = "int default 0")
    private int fileType;

    private long roomId;

    @Column(name = "file_size")
    private long fileSize;

    public RoomFile(RoomFileDTO roomFileDTO) {
        this.uuid = roomFileDTO.getUuid();
        this.saveDir = roomFileDTO.getSaveDir();
        this.fileName = roomFileDTO.getFileName();
        this.fileType = roomFileDTO.getFileType();
        this.roomId = roomFileDTO.getRoomId();
        this.fileSize = roomFileDTO.getFileSize();
    }
}
