package com.triptune.global.response.page;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulePageResponse<T> {
    private int totalPages;             // 전체 페이지 수
    private int currentPage;            // 현재 페이지
    private long totalElements;         // 전체 일정 수
    private long totalSharedElements;   // 전체 공유 일정 수
    private int pageSize;               // 페이지 당 아이템 수
    private List<T> content;

    private SchedulePageResponse(int totalPages, int currentPage, long totalElements, long totalSharedElements, int pageSize, List<T> content) {
        this.totalPages = totalPages;
        this.currentPage = currentPage + 1;
        this.totalElements = totalElements;
        this.totalSharedElements = totalSharedElements;
        this.pageSize = pageSize;
        this.content = content;
    }

    public static<T> SchedulePageResponse<T> of(Page<T> object, long totalElements, long totalSharedElements){
        return new SchedulePageResponse<>(
                object.getTotalPages(),
                object.getNumber(),
                totalElements,
                totalSharedElements,
                object.getSize(),
                object.getContent() != null ? object.getContent() : Collections.emptyList()
        );
    }

    public static <T> SchedulePageResponse<T> ofAllSchedules(Page<T> object, long totalSharedElements){
        return new SchedulePageResponse<>(
                object.getTotalPages(),
                object.getNumber(),
                object.getTotalElements(),
                totalSharedElements,
                object.getSize(),
                object.getContent() != null ? object.getContent() : Collections.emptyList()
        );
    }

    public static <T> SchedulePageResponse<T> ofSharedSchedules(Page<T> object, int totalElements){
        return new SchedulePageResponse<>(
                object.getTotalPages(),
                object.getNumber(),
                totalElements,
                object.getTotalElements(),
                object.getSize(),
                object.getContent() != null ? object.getContent() : Collections.emptyList()
        );
    }
}
