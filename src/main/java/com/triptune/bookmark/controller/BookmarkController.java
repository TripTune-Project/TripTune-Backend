package com.triptune.bookmark.controller;

import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.service.BookmarkService;
import com.triptune.global.response.ApiResponse;
import com.triptune.global.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmark", description = "북마크 관련 API")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    @Operation(summary = "북마크 추가", description = "북마크를 추가합니다.")
    public ApiResponse<?> createBookmark(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                         @Valid @RequestBody BookmarkRequest bookmarkRequest){
        bookmarkService.createBookmark(memberId, bookmarkRequest);

        return ApiResponse.okResponse();
    }

    @DeleteMapping("/{placeId}")
    @Operation(summary = "북마크 취소", description = "북마크를 취소합니다.")
    public ApiResponse<?> deleteBookmark(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                         @PathVariable(name = "placeId") Long placeId){
        bookmarkService.deleteBookmark(memberId, placeId);

        return ApiResponse.okResponse();

    }
}