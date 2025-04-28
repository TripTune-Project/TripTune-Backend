package com.triptune.bookmark.service;

import com.triptune.bookmark.BookmarkTest;
import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.ApiCategory;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookmarkServiceTest extends BookmarkTest {

    @InjectMocks private BookmarkService bookmarkService;
    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private TravelPlaceRepository travelPlaceRepository;

    private Member member;
    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private TravelPlace travelPlace3;

    @BeforeEach
    void setUp(){
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "강남구");
        ApiCategory apiCategory = createApiCategory();

        member = createMember(1L, "member@email.com");
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, "가장소", 0);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, "가장소",5);
        travelPlace3 = createTravelPlace(3L, country, city, district, apiCategory, "나장소",2);

    }

    @Test
    @DisplayName("북마크 추가")
    void createBookmark(){
        // given
        BookmarkRequest request = createBookmarkRequest(travelPlace1.getPlaceId());

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(travelPlace1));

        // when
        assertDoesNotThrow(() -> bookmarkService.createBookmark(member.getMemberId(), request));

        // then
        assertThat(travelPlace1.getBookmarkCnt()).isEqualTo(1);
    }

    @Test
    @DisplayName("북마크 추가 시 이미 북마크로 등록 되어 있어 예외 발생")
    void createBookmark_dataExistException(){
        // given
        BookmarkRequest request = createBookmarkRequest(travelPlace1.getPlaceId());

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class,
                () -> bookmarkService.createBookmark(member.getMemberId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_EXISTED_BOOKMARK.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_EXISTED_BOOKMARK.getMessage());
    }

    @Test
    @DisplayName("북마크 추가 시 사용자 데이터 없어 예외 발생")
    void createBookmark_memberDataNotFoundException(){
        // given
        BookmarkRequest request = createBookmarkRequest(travelPlace1.getPlaceId());

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> bookmarkService.createBookmark(member.getMemberId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("북마크 추가 시 여행지 데이터 없어 예외 발생")
    void createBookmark_travelPlaceDataNotFoundException(){
        // given
        BookmarkRequest request = createBookmarkRequest(travelPlace1.getPlaceId());

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> bookmarkService.createBookmark(member.getMemberId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.PLACE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.PLACE_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("북마크 삭제")
    void deleteBookmark(){
        // given
        int beforeBookmarkCnt = travelPlace2.getBookmarkCnt();

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(true);
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(travelPlace2));

        // when
        assertDoesNotThrow(() -> bookmarkService.deleteBookmark(member.getMemberId(), travelPlace2.getPlaceId()));

        // then
        verify(bookmarkRepository, times(1)).deleteByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong());
        assertThat(travelPlace2.getBookmarkCnt()).isEqualTo(beforeBookmarkCnt-1);
    }

    @Test
    @DisplayName("북마크 삭제 시 북마크 데이터가 존재하지 않아 예외 발생")
    void deleteBookmark_bookmarkNotFoundException(){
        // given
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(false);

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> bookmarkService.deleteBookmark(member.getMemberId(), 1L));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.BOOKMARK_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.BOOKMARK_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("북마크 삭제 시 여행지 데이터가 존재하지 않아 예외 발생")
    void deleteBookmark_travelPlaceNotFoundException(){
        // given
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(true);
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> bookmarkService.deleteBookmark(member.getMemberId(), 0L));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.PLACE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.PLACE_NOT_FOUND.getMessage());

    }

}