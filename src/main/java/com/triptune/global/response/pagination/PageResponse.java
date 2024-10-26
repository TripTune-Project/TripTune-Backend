package com.triptune.global.response.pagination;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

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

    public static <T> PageResponse<T> of(Page<T> object){
        return PageResponse.<T>builder()
                .totalPages(object.getTotalPages())
                .currentPage(object.getNumber())
                .totalElements(object.getTotalElements())
                .pageSize(object.getSize())
                .content(object.getContent())
                .build();
    }
}
