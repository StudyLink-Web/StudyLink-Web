// CommunityFile.java (DB 컬럼명에 100% 맞춘 버전)
package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "community_file")
public class CommunityFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fno")
    private Long fno;

    @Column(name = "bno", nullable = false)
    private Long bno;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "file_type", nullable = false)
    private int fileType; // 1:image, 0:etc

    @Column(name = "save_dir", nullable = false, length = 255)
    private String saveDir;

    @Column(name = "uuid", nullable = false, length = 100)
    private String uuid;
}
