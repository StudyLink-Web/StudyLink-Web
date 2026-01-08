package com.StudyLink.www.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
public class FileDTO {
    private String uuid;
    private String saveDir;
    private String fileName;
    private int fileType;
    private long bno;
    private long fileSize;
    private LocalDateTime regDate, modDate;
}
