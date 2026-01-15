package com.triptune.global.response.pagination;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@NoArgsConstructor
public class SchedulePageResponse<T> {
    private int totalPages;             // 전체 페이지 수
    private int currentPage;            // 현재 페이지
    private long totalElements;         // 전체 일정 수
    private long totalSharedElements;   // 전체 공유 일정 수
    private int pageSize;               // 페이지 당 아이템 수
    private List<T> content;

    @Builder
    public SchedulePageResponse(int totalPages, int currentPage, long totalElements, long totalSharedElements, int pageSize, List<T> content) {
        this.totalPages = totalPages;
        this.currentPage = currentPage + 1;
        this.totalElements = totalElements;
        this.totalSharedElements = totalSharedElements;
        this.pageSize = pageSize;
        this.content = content;
    }

    public static<T> SchedulePageResponse<T> of(Page<T> object, long totalElements, long totalSharedElements){
        return SchedulePageResponse.<T>builder()
                .totalPages(object.getTotalPages())
                .currentPage(object.getNumber())
                .totalElements(totalElements)
                .totalSharedElements(totalSharedElements)
                .pageSize(object.getSize())
                .content(object.getContent())
                .build();
    }

    public static <T> SchedulePageResponse<T> ofAll(Page<T> object, long totalSharedElements){
        return SchedulePageResponse.<T>builder()
                .totalPages(object.getTotalPages())
                .currentPage(object.getNumber())
                .totalElements(object.getTotalElements())
                .totalSharedElements(totalSharedElements)
                .pageSize(object.getSize())
                .content(object.getContent())
                .build();
    }

    public static <T> SchedulePageResponse<T> ofShared(Page<T> object, int totalElements){
        return SchedulePageResponse.<T>builder()
                .totalPages(object.getTotalPages())
                .currentPage(object.getNumber())
                .totalElements(totalElements)
                .totalSharedElements(object.getTotalElements())
                .pageSize(object.getSize())
                .content(object.getContent())
                .build();
    }
}
