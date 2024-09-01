package com.triptune.global.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PageResponse<T> {
    private int totalPages;     // 전체 페이지 수
    private int currentPage;    // 현재 페이지
    private long totalElements;    // 전체 아이템 수
    private int pageSize;       // 페이지 당 아이템 수
    private List<T> content;

    @Builder
    public PageResponse(int totalPages, int currentPage, long totalElements, int pageSize, List<T> content) {
        this.totalPages = totalPages;
        this.currentPage = currentPage + 1;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.content = content;
    }
}
