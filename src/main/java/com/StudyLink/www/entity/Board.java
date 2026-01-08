package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

/*@Table(name="board") : 테이블 이름 변경 가능.
 * 일반적으로는 클래스명이 테이블명
 * Entity : DB의 테이블 맵핑 클래스
 * DTO :  객체를 생성하는 클래스
 * JPA Auditing : reg_date, mod_date 등록일, 수정일 같은
 * 모든 클래스에 동일하게 사용되는 칼럼을 별도로 관리 => base class로 관리
 * @id  => primary key
 * 기본키 생성 전략 : GeneratedValue
 * auto_increments => GenerationType.IDENTITY
 *
 * @Column(설정=값) => 생략가능
 * */

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="board")
public class Board extends TimeBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;
    @Column(length = 200, nullable = false)
    private String title;
    @Column(length = 200, nullable = false)
    private String writer;
    @Column(length = 2000, nullable = false)
    private String content;
    @Column(name = "read_count", columnDefinition = "int default 0")
    private int readCount;
    @Column(name = "cmt_qty", columnDefinition = "int default 0")
    private int cmtQty;
    @Column(name = "file_qty", columnDefinition = "int default 0")
    private int fileQty;

}

