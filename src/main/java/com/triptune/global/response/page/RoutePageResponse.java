package com.triptune.global.response.page;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutePageResponse<T> {
    private Integer routeVersion;       // 여행 루트 버전
    private int totalPages;             // 전체 페이지 수
    private int currentPage;            // 현재 페이지
    private long totalElements;         // 전체 아이템 수
    private int pageSize;               // 페이지 당 아이템 수
    private List<T> content;

    private RoutePageResponse(Integer routeVersion, int totalPages, int currentPage, long totalElements, int pageSize, List<T> content) {
        this.routeVersion = routeVersion;
        this.totalPages = totalPages;
        this.currentPage = currentPage + 1;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.content = content;
    }

    public static<T> RoutePageResponse<T> of(Integer routeVersion, Page<T> object){
        return new RoutePageResponse<>(
                routeVersion,
                object.getTotalPages(),
                object.getNumber(),
                object.getTotalElements(),
                object.getSize(),
                object.getContent() != null ? object.getContent() : Collections.emptyList()
        );
    }

}
