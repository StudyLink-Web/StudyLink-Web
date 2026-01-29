package com.StudyLink.www.handler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class InquiryPageHandler<T> {

    private int pageNo;
    private int size;
    private long totalElement;
    private int totalPage;

    private int startPage;
    private int endPage;
    private boolean prev;
    private boolean next;

    private int naviSize = 10;

    // ✅ 검색 조건 (컨트롤러와 일치)
    private String category;
    private String status;
    private String keyword;

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
            this.totalPage = 1;
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
