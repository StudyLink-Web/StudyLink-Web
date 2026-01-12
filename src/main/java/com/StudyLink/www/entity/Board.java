package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
@Table(name = "board")
public class Board extends TimeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    // FK: Users PK(Long)와 타입 통일
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false) // ✅ content는 TEXT 권장
    private String content;

    @Column(name = "view_count", columnDefinition = "int default 0")
    private int viewCount;

    // created_at, updated_at 은 TimeBase가 처리 (여기엔 선언 X)
}