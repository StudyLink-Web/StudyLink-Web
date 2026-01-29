package com.StudyLink.www.handler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class InquiryPageHandler<T> {

    // 커뮤니티 템플릿 호환 필드명
    private int pageNo;            // 현재 페이지
    private int size;              // 페이지당 개수(보통 10)
    private long totalElement;     // 전체 개수
    private int totalPage;         // 전체 페이지 수

    private int startPage;         // 시작 페이지
    private int endPage;           // 끝 페이지
    private boolean prev;          // 이전
    private boolean next;          // 다음

    private int naviSize = 10;     // 네비 개수

    // 검색용(템플릿 유지용)
    private String type;
    private String keyword;

    // 목록(템플릿에서 ph.list로 사용)
    private List<T> list;

    public InquiryPageHandler(int pageNo, Page<T> result) {
        this(pageNo, result, 10);
    }

    public InquiryPageHandler(int pageNo, Page<T> result, int naviSize) {
        this.pageNo = pageNo;
        this.naviSize = (naviSize <= 0 ? 10 : naviSize);

        if (result == null) {
            this.size = 10;
            this.totalElement = 0;
            this.totalPage = 0;
            this.list = List.of();
            this.startPage = 1;
            this.endPage = 1;
            this.prev = false;
            this.next = false;
            return;
        }

        this.size = result.getSize();
        this.totalElement = result.getTotalElements();
        this.totalPage = Math.max(result.getTotalPages(), 1);
        this.list = (result.getContent() == null ? List.of() : result.getContent());

        this.startPage = ((pageNo - 1) / this.naviSize) * this.naviSize + 1;
        this.endPage = Math.min(this.startPage + this.naviSize - 1, this.totalPage);

        this.prev = this.startPage > 1;
        this.next = this.endPage < this.totalPage;
    }
}
