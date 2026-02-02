package com.triptune.bookmark.service;

import com.triptune.bookmark.fixture.BookmarkFixture;
import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.message.ErrorCode;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookmarkServiceTest  {

    @InjectMocks private BookmarkService bookmarkService;
    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private TravelPlaceRepository travelPlaceRepository;

    private Member member;
    private TravelPlace place1;
    private TravelPlace place2;

    @BeforeEach
    void setUp(){
        Country country = CountryFixture.createCountry();
        City city = CityFixture.createCity(country, "서울");
        District district = DistrictFixture.createDistrict(city, "강남구");
        ApiCategory apiCategory = ApiCategoryFixture.createApiCategory();
        ApiContentType apiContentType = ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS);

        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        member = MemberFixture.createNativeTypeMember("member@email.com", profileImage);
        place1 = TravelPlaceFixture.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                "여행지1"
        );
        place2 = TravelPlaceFixture.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                "여행지2"
        );

    }

    @Test
    @DisplayName("북마크 추가")
    void createBookmark(){
        // given
        Long travelPlace1Id = 1L;
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(travelPlace1Id);

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(place1));

        // when
        assertDoesNotThrow(() -> bookmarkService.createBookmark(1L, request));

        // then
        assertThat(place1.getBookmarkCnt()).isEqualTo(1);
    }

    @Test
    @DisplayName("북마크 추가 시 이미 북마크로 등록 되어 있어 예외 발생")
    void createBookmark_alreadyBookmarked(){
        // given
        Long travelPlace1Id = 1L;
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(travelPlace1Id);

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class,
                () -> bookmarkService.createBookmark(1L, request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.ALREADY_EXISTED_BOOKMARK);
    }

    @Test
    @DisplayName("북마크 추가 시 회원 데이터 없어 예외 발생")
    void createBookmark_memberNotFound(){
        // given
        Long travelPlace1Id = 1L;
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(travelPlace1Id);

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> bookmarkService.createBookmark(1000L, request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("북마크 추가 시 여행지 데이터 없어 예외 발생")
    void createBookmark_placeNotFound(){
        // given
        Long travelPlace1Id = 1L;
        BookmarkRequest request = BookmarkFixture.createBookmarkRequest(travelPlace1Id);

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> bookmarkService.createBookmark(1L, request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.PLACE_NOT_FOUND);

    }

    @Test
    @DisplayName("북마크 삭제")
    void deleteBookmark(){
        // given
        int beforeBookmarkCnt = place2.getBookmarkCnt();

        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(true);
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(place2));

        // when
        assertDoesNotThrow(() -> bookmarkService.deleteBookmark(1L, 2L));

        // then
        verify(bookmarkRepository, times(1)).deleteByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong());
        assertThat(place2.getBookmarkCnt()).isEqualTo(beforeBookmarkCnt-1);
    }

    @Test
    @DisplayName("북마크 삭제 시 북마크 데이터가 존재하지 않아 예외 발생")
    void deleteBookmark_bookmarkNotFound(){
        // given
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(false);

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> bookmarkService.deleteBookmark(1L, 1L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.BOOKMARK_NOT_FOUND);

    }

    @Test
    @DisplayName("북마크 삭제 시 여행지 데이터가 존재하지 않아 예외 발생")
    void deleteBookmark_placeNotFound(){
        // given
        when(bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(anyLong(), anyLong())).thenReturn(true);
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> bookmarkService.deleteBookmark(1L, 1000L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.PLACE_NOT_FOUND);

    }

}