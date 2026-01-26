package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "community")
public class Community extends TimeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bno")
    private Long bno;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 100, nullable = false)
    private String writer;

    @Column(length = 100)
    private String department;

    @Lob
    @Column
    private String content;

    @Builder.Default
    @Column(name = "read_count", nullable = false)
    private Integer readCount = 0;

    @Builder.Default
    @Column(name = "cmt_qty", nullable = false)
    private Integer cmtQty = 0;

    @Builder.Default
    @Column(name = "file_qty", nullable = false)
    private Integer fileQty = 0;
}
