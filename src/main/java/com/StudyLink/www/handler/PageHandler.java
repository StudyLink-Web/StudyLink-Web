package com.StudyLink.www.handler;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@ToString
public class PageHandler<T> {

    private int startPage;
    private int endPage;
    private int totalPage;
    private long totalElement; // 전체 게시글 수
    private int pageNo;
    private boolean prev, next;

    private List<T> list;

    // ✅ 검색
    private String type;
    private String keyword;   // ✅ Keyword -> keyword (소문자 통일)

    // 기본 생성자 (페이징)
    public PageHandler(Page<T> page, int pageNo) {
        this.list = page.getContent();
        this.pageNo = pageNo;

        this.totalPage = page.getTotalPages();
        this.totalElement = page.getTotalElements();

        // ✅ 데이터가 0개면 totalPage = 0 (페이징 계산 의미 없음)
        if (this.totalPage <= 0) {
            this.startPage = 0;
            this.endPage = 0;
            this.prev = false;
            this.next = false;
            return;
        }

        // 페이지 블록(10개 단위)
        this.endPage = (int) Math.ceil(this.pageNo / 10.0) * 10;
        this.startPage = this.endPage - 9;

        // 마지막 블록 보정
        if (this.endPage > this.totalPage) {
            this.endPage = this.totalPage;
        }
        if (this.startPage < 1) {
            this.startPage = 1;
        }

        this.prev = this.startPage > 1;
        this.next = this.endPage < this.totalPage;
    }

    // 검색 포함 생성자
    public PageHandler(Page<T> page, int pageNo, String type, String keyword) {
        this(page, pageNo);
        this.type = type;
        this.keyword = keyword; // ✅ 통일
    }
}
