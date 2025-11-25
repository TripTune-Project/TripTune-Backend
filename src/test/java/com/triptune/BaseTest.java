package com.triptune;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.bookmark.entity.Bookmark;
import com.triptune.common.entity.*;
import com.triptune.global.security.CustomUserDetails;
import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enums.JoinType;
import com.triptune.member.enums.SocialType;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public abstract class BaseTest {

    @Autowired private ObjectMapper objectMapper;

    private final String refreshToken = "MemberRefreshToken";


    protected Member createMember(Long memberId, String email){
        return Member.builder()
                .memberId(memberId)
                .email(email)
                .password("test123@")
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.NATIVE)
                .build();
    }
    protected Member createMember(Long memberId, String email, ProfileImage profileImage){
        return Member.builder()
                .memberId(memberId)
                .profileImage(profileImage)
                .email(email)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.NATIVE)
                .build();
    }

    protected Member createMember(Long memberId, String email, String encodePassword, ProfileImage profileImage){
        return Member.builder()
                .memberId(memberId)
                .profileImage(profileImage)
                .email(email)
                .password(encodePassword)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.NATIVE)
                .build();
    }

    protected Member createMember(Long memberId, String email, String encodePassword, JoinType joinType, ProfileImage profileImage){
        return Member.builder()
                .memberId(memberId)
                .profileImage(profileImage)
                .email(email)
                .password(encodePassword)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(joinType)
                .build();
    }

    protected Member createMember(Long memberId, String email, String encodePassword, JoinType joinType){
        return Member.builder()
                .memberId(memberId)
                .email(email)
                .password(encodePassword)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(joinType)
                .build();
    }

    protected Member createNativeTypeMember(Long memberId, String email, String encodePassword, ProfileImage profileImage){
        return Member.builder()
                .memberId(memberId)
                .profileImage(profileImage)
                .email(email)
                .password(encodePassword)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.NATIVE)
                .isActive(true)
                .build();
    }


    protected Member createNativeTypeMember(Long memberId, String email){
        return Member.builder()
                .memberId(memberId)
                .email(email)
                .password("password12!@")
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.NATIVE)
                .isActive(true)
                .build();
    }


    protected Member createNativeTypeMember(Long memberId, String email, String encodedPassword){
        return Member.builder()
                .memberId(memberId)
                .email(email)
                .password(encodedPassword)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.NATIVE)
                .isActive(true)
                .build();
    }

    protected Member createSocialTypeMember(Long memberId, String email){
        return Member.builder()
                .memberId(memberId)
                .email(email)
                .password(null)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.SOCIAL)
                .isActive(true)
                .build();
    }

    protected Member createSocialTypeMember(Long memberId, String email, ProfileImage profileImage){
        return Member.builder()
                .memberId(memberId)
                .profileImage(profileImage)
                .email(email)
                .password(null)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.SOCIAL)
                .isActive(true)
                .build();
    }

    protected Member createBothTypeMember(Long memberId, String email, String encodedPassword, ProfileImage profileImage){
        return Member.builder()
                .memberId(memberId)
                .profileImage(profileImage)
                .email(email)
                .password(encodedPassword)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.BOTH)
                .isActive(true)
                .build();
    }


    protected Member createBothTypeMember(Long memberId, String email){
        return Member.builder()
                .memberId(memberId)
                .email(email)
                .password("password12!@")
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.BOTH)
                .isActive(true)
                .build();
    }

    protected Member createBothTypeMember(Long memberId, String email, String encodedPassword){
        return Member.builder()
                .memberId(memberId)
                .email(email)
                .password(encodedPassword)
                .nickname(email.split("@")[0])
                .refreshToken(refreshToken)
                .joinType(JoinType.BOTH)
                .isActive(true)
                .build();
    }


    protected ProfileImage createProfileImage(Long profileImageId, String fileName){
        return ProfileImage.builder()
                .profileImageId(profileImageId)
                .s3ObjectUrl("/test/" + fileName + ".jpg")
                .s3ObjectKey("/img/test/" + fileName + ".jpg")
                .originalName(fileName + "_original.jpg")
                .fileName(fileName + ".jpg")
                .fileType("jpg")
                .fileSize(20)
                .build();
    }


    protected ProfileImage createProfileImage(Long profileImageId, String fileName, Member member){
        return ProfileImage.builder()
                .profileImageId(profileImageId)
                .member(member)
                .s3ObjectUrl("/test/" + fileName + ".jpg")
                .s3ObjectKey("/img/test/" + fileName + ".jpg")
                .originalName(fileName + "_original.jpg")
                .fileName(fileName + ".jpg")
                .fileType("jpg")
                .fileSize(20)
                .build();
    }

    protected Country createCountry(){
        return Country.builder().countryName("대한민국").build();
    }

    protected City createCity(Country country){
        return City.builder().cityName("서울").country(country).build();
    }

    protected City createCity(Country country, String cityName){
        return City.builder().cityName(cityName).country(country).build();
    }

    protected District createDistrict(City city, String districtName){
        return District.builder().districtName(districtName).city(city).build();
    }

    protected ApiCategory createApiCategory(){
        return ApiCategory.builder().categoryCode("A0101").categoryName("자연").level(1).build();
    }

    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("테스트 장소명")
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .build();
    }


    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .apiContentType(apiContentType)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("테스트 장소명")
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .build();
    }


    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, int bookmarkCnt){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .apiContentType(apiContentType)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("테스트 장소명")
                .bookmarkCnt(bookmarkCnt)
                .description("테스트 장소 설명")
                .build();
    }

    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, double latitude, double longitude, List<TravelImage> travelImages){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(latitude)
                .longitude(longitude)
                .placeName("테스트 장소명")
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .travelImages(travelImages)
                .build();
    }


    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, double latitude, double longitude){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .apiContentType(apiContentType)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(latitude)
                .longitude(longitude)
                .placeName("테스트 장소명")
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .build();
    }

    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, double latitude, double longitude){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(latitude)
                .longitude(longitude)
                .placeName("테스트 장소명")
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .build();
    }

    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, List<TravelImage> travelImages){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .apiContentType(apiContentType)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("테스트 장소명")
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .travelImages(travelImages)
                .build();
    }

    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, String placeName){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName(placeName)
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .build();
    }


    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, String placeName, List<TravelImage> travelImages){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName(placeName)
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .travelImages(travelImages)
                .build();
    }

    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, String placeName, int bookmarkCnt){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName(placeName)
                .bookmarkCnt(bookmarkCnt)
                .description("테스트 장소 설명")
                .build();
    }

    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, List<TravelImage> travelImages){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("장소")
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .travelImages(travelImages)
                .build();
    }


    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String useTime, List<TravelImage> travelImages){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .apiContentType(apiContentType)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("장소")
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .useTime(useTime)
                .travelImages(travelImages)
                .build();
    }


    protected TravelPlace createTravelPlace(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String checkInTime, String checkOutTime, List<TravelImage> travelImages){
        return TravelPlace.builder()
                .placeId(placeId)
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .apiContentType(apiContentType)
                .address("테스트 주소")
                .detailAddress("테스트 상세주소")
                .homepage("www.test.com")
                .phoneNumber("010-0000-0000")
                .latitude(37.5)
                .longitude(127.0281573537)
                .placeName("장소")
                .bookmarkCnt(0)
                .description("테스트 장소 설명")
                .checkInTime(checkInTime)
                .checkOutTime(checkOutTime)
                .travelImages(travelImages)
                .build();
    }

    protected TravelSchedule createTravelSchedule(Long scheduleId, String scheduleName){
        return TravelSchedule.builder()
                .scheduleId(scheduleId)
                .scheduleName(scheduleName)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .build();
    }


    protected TravelImage createTravelImage(TravelPlace travelPlace, String fileName, boolean isThumbnail){
        return TravelImage.builder()
                .travelPlace(travelPlace)
                .s3ObjectUrl("/test/" + fileName + ".jpg")
                .originalName(fileName + "_original.jpg")
                .fileName(fileName + ".jpg")
                .fileType("jpg")
                .fileSize(20)
                .isThumbnail(isThumbnail)
                .build();
    }


    protected ApiContentType createApiContentType(ThemeType themeType){
        return ApiContentType.builder()
                .contentTypeName(themeType.getApiContentTypeName())
                .apiContentTypeId(themeType.getApiContentTypeId())
                .build();
    }

    protected TravelAttendee createTravelAttendee(Long attendeeId, Member member, TravelSchedule schedule, AttendeeRole role, AttendeePermission permission){
        return TravelAttendee.builder()
                .attendeeId(attendeeId)
                .member(member)
                .travelSchedule(schedule)
                .role(role)
                .permission(permission)
                .build();
    }

    protected TravelRoute createTravelRoute(TravelSchedule schedule, TravelPlace travelPlace, int routeOrder){
        return TravelRoute.builder()
                .travelSchedule(schedule)
                .travelPlace(travelPlace)
                .routeOrder(routeOrder)
                .build();
    }

    protected ChatMessage createChatMessage(String messageId, Long scheduleId, Member member, String message){
        return ChatMessage.builder()
                .messageId(messageId)
                .scheduleId(scheduleId)
                .memberId(member.getMemberId())
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    protected Bookmark createBookmark(Long bookmarkId, Member member, TravelPlace travelPlace){
        return Bookmark.builder()
                .bookmarkId(bookmarkId)
                .member(member)
                .travelPlace(travelPlace)
                .build();
    }

    protected SocialMember createSocialMember(Long socialMemberId, Member member, String socialId, SocialType socialType){
        SocialMember socialMember = SocialMember.builder()
                .socialMemberId(socialMemberId)
                .member(member)
                .socialId(socialId)
                .socialType(socialType)
                .build();

        member.addSocialMember(socialMember);
        return socialMember;
    }

    protected void mockAuthentication(Member member){
        CustomUserDetails userDetails = new CustomUserDetails(member);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    protected String toJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
