// src/main/java/com/StudyLink/www/dto/CommunityFileDTO.java
package com.StudyLink.www.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityFileDTO {
    private CommunityDTO communityDTO;
    private List<FileDTO> fileDTOList;
}
