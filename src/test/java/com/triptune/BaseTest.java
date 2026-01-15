package com.triptune;

import com.triptune.bookmark.entity.Bookmark;
import com.triptune.common.entity.*;
import com.triptune.global.security.CustomUserDetails;
import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enums.SocialType;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

public abstract class BaseTest {
    private final String refreshToken = "MemberRefreshToken";
//
//    protected Member createMember(Long memberId, String email){
//        return Member.builder()
//                .memberId(memberId)
//                .email(email)
//                .password("test123@")
//                .nickname(email.split("@")[0])
//                .refreshToken(refreshToken)
//                .joinType(JoinType.NATIVE)
//                .build();
//    }
//    protected Member createMember(Long memberId, String email, ProfileImage profileImage){
//        return Member.builder()
//                .memberId(memberId)
//                .profileImage(profileImage)
//                .email(email)
//                .nickname(email.split("@")[0])
//                .refreshToken(refreshToken)
//                .joinType(JoinType.NATIVE)
//                .build();
//    }
//
//    protected Member createMember(Long memberId, String email, String encodePassword, ProfileImage profileImage){
//        return Member.builder()
//                .memberId(memberId)
//                .profileImage(profileImage)
//                .email(email)
//                .password(encodePassword)
//                .nickname(email.split("@")[0])
//                .refreshToken(refreshToken)
//                .joinType(JoinType.NATIVE)
//                .build();
//    }
//
//    protected Member createMember(Long memberId, String email, String encodePassword, JoinType joinType, ProfileImage profileImage){
//        return Member.builder()
//                .memberId(memberId)
//                .profileImage(profileImage)
//                .email(email)
//                .password(encodePassword)
//                .nickname(email.split("@")[0])
//                .refreshToken(refreshToken)
//                .joinType(joinType)
//                .build();
//    }
//
//    protected Member createMember(Long memberId, String email, String encodePassword, JoinType joinType){
//        return Member.builder()
//                .memberId(memberId)
//                .email(email)
//                .password(encodePassword)
//                .nickname(email.split("@")[0])
//                .refreshToken(refreshToken)
//                .joinType(joinType)
//                .build();
//    }

    protected Member createNativeTypeMember(String email, String encodePassword, ProfileImage profileImage){
        return Member.createNativeMember(
                email,
                encodePassword,
                email.split("@")[0],
                profileImage
        );
    }

    protected Member createNativeTypeMember(String email, ProfileImage profileImage){
        return Member.createNativeMember(
                email,
                "encodedPassword",
                email.split("@")[0],
                profileImage
        );
    }

    protected Member createNativeTypeMemberWithId(Long memberId, String email, ProfileImage profileImage){
        Member member = Member.createNativeMember(
                email,
                "encodedPassword",
                email.split("@")[0],
                profileImage
        );
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }


    protected Member createSocialTypeMember(String email, ProfileImage profileImage){
        return Member.createSocialMember(
                email,
                email.split("@")[0],
                profileImage
        );
    }

    protected Member createSocialTypeMemberWithId(Long memberId, String email, ProfileImage profileImage){
         Member member = Member.createSocialMember(
                email,
                email.split("@")[0],
                profileImage
         );

         ReflectionTestUtils.setField(member, "memberId", memberId);
         return member;

    }

    protected Member createBothTypeMember(String email, String encodedPassword, ProfileImage profileImage){
        Member member = Member.createNativeMember(
                email,
                encodedPassword,
                email.split("@")[0],
                profileImage
        );
        member.linkSocialAccount();

        return member;
    }

    protected Member createBothTypeMember(String email, ProfileImage profileImage){
        Member member = Member.createNativeMember(
                email,
                "encodedPassword",
                email.split("@")[0],
                profileImage
        );
        member.linkSocialAccount();

        return member;
    }

    protected Member createBothTypeMemberWithId(Long memberId, String email, ProfileImage profileImage){
        Member member = Member.createNativeMember(
                email,
                "encodedPassword",
                email.split("@")[0],
                profileImage
        );

        ReflectionTestUtils.setField(member, "memberId", memberId);
        member.linkSocialAccount();

        return member;
    }

    protected ProfileImage createProfileImage(String fileName){
        return ProfileImage.createProfileImage(
                "/test/" + fileName + ".jpg",
                "/img/test/" + fileName + ".jpg",
                fileName + "_original.jpg",
                fileName + ".jpg",
                "jpg",
                20
        );
    }

    protected Country createCountry(){
        return Country.createCountry("대한민국");
    }

    protected City createCity(Country country, String cityName){
        return City.createCity(country, cityName);
    }

    protected District createDistrict(City city, String districtName){
        return District.createDistrict(city, districtName);
    }

    protected ApiCategory createApiCategory(){
        return ApiCategory.createApiCategory(
                "A0101",
                "자연",
                "A01",
                1
        );
    }


    // 숙박(checkInTime, checkOutTime not null / userTime null)
    protected TravelPlace createLodgingTravelPlace(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                null,
                "15:00",
                "11:00",
                "www.test.com",
                "010-0000-0000",
                37.5,
                127.0281573537,
                placeName + "상세설명",
                0
        );
    }

    // 숙박 외(checkInTime, checkOutTime null / userTime not null)
    protected TravelPlace createTravelPlace(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                37.5,
                127.0281573537,
                placeName + "상세설명",
                0
        );
    }

    protected TravelPlace createTravelPlaceWithId(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName){
        TravelPlace travelPlace = TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                37.5,
                127.0281573537,
                placeName + "상세설명",
                0
        );

        ReflectionTestUtils.setField(travelPlace, "placeId", placeId);
        return travelPlace;
    }

    // 위도, 경도 지정
    protected TravelPlace createTravelPlaceWithLocation(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, double latitude, double longitude){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                latitude,
                longitude,
                placeName+ " 상세설명",
                0
        );
    }


    protected TravelPlace createTravelPlaceWithIdAndLocation(Long placeId, Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, double latitude, double longitude){
        TravelPlace travelPlace = TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                latitude,
                longitude,
                placeName+ " 상세설명",
                0
        );

        ReflectionTestUtils.setField(travelPlace, "placeId", placeId);
        return travelPlace;
    }


    // 북마크 횟수 지정
    protected TravelPlace createTravelPlaceWithBookmarkCnt(Country country, City city, District district, ApiCategory apiCategory, ApiContentType apiContentType, String placeName, int bookmarkCnt){
        return TravelPlace.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                placeName,
                placeName + " 테스트 주소",
                placeName + " 테스트 상세주소",
                "09:00~18:00",
                null,
                null,
                "www.test.com",
                "010-0000-0000",
                37.5,
                127.0281573537,
                placeName + " 상세설명",
                bookmarkCnt
        );
    }


    protected TravelSchedule createTravelSchedule(String scheduleName){
        return TravelSchedule.createTravelSchedule(
                scheduleName,
                LocalDate.now(),
                LocalDate.now()
        );
    }

    protected TravelSchedule createTravelScheduleWithId(Long scheduleId, String scheduleName){
        TravelSchedule travelSchedule = TravelSchedule.createTravelSchedule(
                scheduleName,
                LocalDate.now(),
                LocalDate.now()
        );

        ReflectionTestUtils.setField(travelSchedule, "scheduleId", scheduleId);

        return travelSchedule;
    }


    protected TravelImage createTravelImage(TravelPlace travelPlace, String fileName, boolean isThumbnail){
        return TravelImage.createTravelImage(
                travelPlace,
                "/test/" + fileName + ".jpg",
                "/img/test/" + fileName + ".jpg",
                fileName + "_original.jpg",
                fileName + ".jpg",
                "jpg",
                20,
                isThumbnail
        );
    }


    protected ApiContentType createApiContentType(ThemeType themeType){
        return ApiContentType.createApiContentType(themeType.getApiContentTypeName(), themeType.getApiContentTypeId());
    }

    protected TravelAttendee createAuthorTravelAttendee(TravelSchedule schedule, Member member){
        return TravelAttendee.createAuthor(schedule, member);
    }

    protected TravelAttendee createAuthorTravelAttendeeWithId(Long attendeeId, TravelSchedule schedule, Member member){
        TravelAttendee attendee = TravelAttendee.createAuthor(schedule, member);
        ReflectionTestUtils.setField(attendee, "attendeeId", attendeeId);
        return attendee;
    }

    protected TravelAttendee createGuestTravelAttendee(TravelSchedule schedule, Member member, AttendeePermission permission){
        return TravelAttendee.createGuest(schedule, member, permission);
    }

    protected TravelAttendee createGuestTravelAttendeeWithId(Long attendeeId, TravelSchedule schedule, Member member, AttendeePermission permission){
        TravelAttendee attendee = TravelAttendee.createGuest(schedule, member, permission);
        ReflectionTestUtils.setField(attendee, "attendeeId", attendeeId);
        return attendee;
    }

    protected TravelRoute createTravelRoute(TravelSchedule schedule, TravelPlace travelPlace, int routeOrder){
        return TravelRoute.createTravelRoute(
                schedule,
                travelPlace,
                routeOrder
        );
    }

    protected ChatMessage createChatMessage(Long scheduleId, Long memberId, String message){
        return ChatMessage.createChatMessage(
                scheduleId,
                memberId,
                message
        );
    }

    protected Bookmark createBookmark(Member member, TravelPlace travelPlace){
        return Bookmark.createBookmark(member, travelPlace);
    }

    protected SocialMember createSocialMember(Member member, SocialType socialType, String socialId){
        return SocialMember.createSocialMember(
                member,
                socialType,
                socialId
        );
    }

    protected void mockAuthentication(Member member){
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

}
