package com.triptune.global.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageUtil {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 5;
    private static final int SCHEDULE_SIZE = 9;

    public static Pageable defaultPageable(int page){
        return PageRequest.of(page - DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public static Pageable schedulePageable(int page){
        return PageRequest.of(page - DEFAULT_PAGE, SCHEDULE_SIZE);
    }
    public static<T> Page<T> createPage(List<T> content, Pageable pageable, long totalElements){
        return new PageImpl<>(content, pageable, totalElements);
    }
}
