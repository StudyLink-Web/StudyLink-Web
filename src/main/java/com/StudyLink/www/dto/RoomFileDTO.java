package com.StudyLink.www.dto;

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
}
