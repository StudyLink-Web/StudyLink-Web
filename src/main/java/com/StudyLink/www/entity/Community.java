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

    @Column(name = "read_count", nullable = false)
    private int readCount;

    @Column(name = "cmt_qty", nullable = false)
    private int cmtQty;

    @Column(name = "file_qty", nullable = false)
    private int fileQty;
}
