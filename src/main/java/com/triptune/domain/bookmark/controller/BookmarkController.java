package com.triptune.domain.bookmark.controller;

import com.triptune.domain.bookmark.dto.request.BookmarkRequest;
import com.triptune.domain.bookmark.service.BookmarkService;
import com.triptune.global.response.ApiResponse;
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
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        bookmarkService.createBookmark(userId, bookmarkRequest);

        return ApiResponse.okResponse();
    }

    @DeleteMapping("/{placeId}")
    @Operation(summary = "북마크 취소", description = "북마크를 취소합니다.")
    public ApiResponse<?> deleteBookmark(@PathVariable(name = "placeId") Long placeId){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        bookmarkService.deleteBookmark(userId, placeId);

        return ApiResponse.okResponse();

    }
}