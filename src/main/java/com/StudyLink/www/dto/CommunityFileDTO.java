package com.StudyLink.www.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityFileDTO {
    private Long fno;
    private Long bno;
    private String uuid;
    private String fileName;
    private String saveDir;
    private long fileSize;
    private int fileType;
}
