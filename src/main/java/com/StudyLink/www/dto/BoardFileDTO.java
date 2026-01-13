package com.StudyLink.www.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardFileDTO {

    private BoardDTO boardDTO;
    private List<FileDTO> fileDTOList;

}
