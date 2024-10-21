package com.triptune.global.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageUtil {

    public static Pageable createPageRequest(int page, int size){
        return PageRequest.of(page - 1, size);
    }
    public static<T> Page<T> createPage(List<T> content, Pageable pageable, long totalElements){
        return new PageImpl<>(content, pageable, totalElements);
    }
}
