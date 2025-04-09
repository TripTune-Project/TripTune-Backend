package com.triptune.schedule;

import com.triptune.BaseTest;
import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.schedule.dto.request.*;
import com.triptune.schedule.enumclass.AttendeePermission;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.entity.TravelPlace;

import java.time.LocalDate;
import java.util.List;

public abstract class ScheduleTest extends BaseTest {
    protected ScheduleCreateRequest createScheduleRequest(LocalDate startDate) {
        return ScheduleCreateRequest.builder()
                .scheduleName("테스트")
                .startDate(startDate)
                .endDate(LocalDate.now().plusDays(10))
                .build();
    }

    protected ScheduleUpdateRequest createUpdateScheduleRequest(List<RouteRequest> routeRequestList){
        return ScheduleUpdateRequest.builder()
                .scheduleName("수정 테스트")
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(20))
                .travelRoute(routeRequestList)
                .build();
    }

    protected ScheduleUpdateRequest createUpdateScheduleRequest(String scheduleName, List<RouteRequest> routeRequestList){
        return ScheduleUpdateRequest.builder()
                .scheduleName(scheduleName)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(20))
                .travelRoute(routeRequestList)
                .build();
    }

    protected RouteRequest createRouteRequest(int routeOrder, Long placeId){
        return RouteRequest.of(routeOrder, placeId);
    }

    protected AttendeeRequest createAttendeeRequest(String email, AttendeePermission permission){
        return AttendeeRequest.builder()
                .email(email)
                .permission(permission)
                .build();
    }


    protected AttendeePermissionRequest createAttendeePermissionRequest(AttendeePermission permission) {
        return AttendeePermissionRequest.builder().permission(permission).build();
    }


    protected ChatMessageRequest createChatMessageRequest(Long scheduleId, String nickname, String message) {
        return ChatMessageRequest.builder()
                .scheduleId(scheduleId)
                .nickname(nickname)
                .message(message)
                .build();

    }

    protected RouteCreateRequest createRouteCreateRequest(Long placeId){
        return RouteCreateRequest.builder().placeId(placeId).build();
    }

    protected MemberProfileResponse createMemberProfileResponse(Long memberId, String nickname){
        return MemberProfileResponse.builder()
                .memberId(memberId)
                .nickname(nickname)
                .profileUrl(nickname + ".jpg")
                .build();
    }

}
