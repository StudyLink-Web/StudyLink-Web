package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "community_file")
public class CommunityFile {

    @Id
    private String uuid;      // 파일 PK

    private Long bno;         // community 글 번호

    private String saveDir;   // ex) 2026/01/26
    private String fileName;  // 원본 파일명
    private int fileType;     // 1=image, 0=other

    @Column(name = "file_size", nullable = false)
    private Long fileSize;    // ✅ 추가 (핵심)
}
