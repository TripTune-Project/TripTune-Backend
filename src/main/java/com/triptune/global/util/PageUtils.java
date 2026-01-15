package com.triptune.global.util;

import org.springframework.data.domain.*;

import java.util.List;

public class PageUtils {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 5;
    private static final int SCHEDULE_SIZE = 9;
    private static final int SCHEDULE_MODAL_SIZE = 3;
    private static final int BOOKMARK_SIZE = 8;
    private static final int TRAVEL_SIZE = 4;
    private static final int CHAT_SIZE = 20;

    public static Pageable defaultPageable(int page){
        return PageRequest.of(page - DEFAULT_PAGE, DEFAULT_SIZE);
    }
    public static Pageable schedulePageable(int page){
        return PageRequest.of(page - DEFAULT_PAGE, SCHEDULE_SIZE);
    }
    public static Pageable travelPageable(int page){
        return PageRequest.of(page - DEFAULT_PAGE, TRAVEL_SIZE);
    }

    public static Pageable chatPageable(int page){
        return PageRequest.of(page - DEFAULT_PAGE, CHAT_SIZE, Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    public static Pageable bookmarkPageable(int page){
        return PageRequest.of(page - DEFAULT_PAGE, BOOKMARK_SIZE);
    }

    public static Pageable scheduleModalPageable(int page){
        return PageRequest.of(page - DEFAULT_PAGE, SCHEDULE_MODAL_SIZE);
    }

    public static<T> Page<T> createPage(List<T> content, Pageable pageable, long totalElements){
        return new PageImpl<>(content, pageable, totalElements);
    }

}

