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

    private BoardDTO boardDTO;          // 게시글 정보
    private List<FileDTO> fileDTOList;  // 첨부파일 목록
}
