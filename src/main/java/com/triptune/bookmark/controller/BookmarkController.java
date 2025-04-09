package com.triptune.bookmark.controller;

import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.service.BookmarkService;
import com.triptune.global.response.ApiResponse;
import com.triptune.global.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmark", description = "북마크 관련 API")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    @Operation(summary = "북마크 추가", description = "북마크를 추가합니다.")
    public ApiResponse<?> createBookmark(@Valid @RequestBody BookmarkRequest bookmarkRequest){
        String email = SecurityUtils.getCurrentEmail();
        bookmarkService.createBookmark(email, bookmarkRequest);

        return ApiResponse.okResponse();
    }

    @DeleteMapping("/{placeId}")
    @Operation(summary = "북마크 취소", description = "북마크를 취소합니다.")
    public ApiResponse<?> deleteBookmark(@PathVariable(name = "placeId") Long placeId){
        String email = SecurityUtils.getCurrentEmail();
        bookmarkService.deleteBookmark(email, placeId);

        return ApiResponse.okResponse();

    }
}