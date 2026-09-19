package com.triptune.bookmark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.fixture.BookmarkFixture;
import com.triptune.bookmark.service.BookmarkService;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.fixture.ProfileImageFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookmarkControllerTest{

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private BookmarkService bookmarkService;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    private Member member;

    @BeforeEach
    void setUp(){
         member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                ProfileImageFixture.createProfileImage("memberImage")
        );
        SecurityTestUtils.mockAuthentication(member);
    }

    @Test
    @DisplayName("북마크 추가")
    void createBookmark() throws Exception{
        // given
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(1L);

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        verify(bookmarkService).createBookmark(anyLong(), any());
    }

    @ParameterizedTest
    @DisplayName("북마크 생성 시 1보다 작은 값 입력으로 400 반환")
    @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
    void createBookmark_invalidMinPlaceId(Long input) throws Exception {
        // given
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(input);

        // when,  then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("여행지 ID는 1 이상의 값이어야 합니다."));
    }

    @Test
    @DisplayName("북마크 생성 시 placeId 값이 null 로 400 반환")
    void createBookmark_invalidNullPlaceId() throws Exception{
        // given
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(null);

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("여행지 ID는 필수 입력 값입니다."));

    }


    @Test
    @DisplayName("북마크 추가 시 이미 북마크로 등록되어 있어 409 반환")
    void createBookmark_alreadyBookmarked() throws Exception{
        // given
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(1L);

        willThrow(new DataExistException(ErrorCode.ALREADY_EXISTED_BOOKMARK))
                .given(bookmarkService).createBookmark(anyLong(), any());

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_BOOKMARK.getMessage()));
    }


    @Test
    @DisplayName("북마크 추가 시 회원 데이터 없어 404 반환")
    void createBookmark_memberNotFound() throws Exception{
        // given
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(1L);

        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(bookmarkService).createBookmark(anyLong(), any());

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("북마크 추가 시 여행지 데이터 없어 404 반환")
    void createBookmark_placeNotFound() throws Exception{
        // given
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(1000L);

        willThrow( new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND))
                .given(bookmarkService).createBookmark(anyLong(), any());

        // when, then
        mockMvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PLACE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("북마크 삭제")
    void deleteBookmark() throws Exception{
        // given
        // when, then
        mockMvc.perform(delete("/api/bookmarks/{placeId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        verify(bookmarkService).deleteBookmark(anyLong(), anyLong());
    }


    @Test
    @DisplayName("북마크 삭제 시 북마크 데이터가 없어 404 반환")
    void deleteBookmark_bookmarkNotFound() throws Exception{
        // given
        willThrow(new DataNotFoundException(ErrorCode.BOOKMARK_NOT_FOUND))
                .given(bookmarkService).deleteBookmark(anyLong(), anyLong());

        // when, then
        mockMvc.perform(delete("/api/bookmarks/{placeId}", 1000L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.BOOKMARK_NOT_FOUND.getMessage()));
    }


}