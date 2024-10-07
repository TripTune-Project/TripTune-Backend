package com.triptune.global.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageableUtil {

    public static Pageable createPageRequest(int page, int size){
        return PageRequest.of(page - 1, size);
    }
}
