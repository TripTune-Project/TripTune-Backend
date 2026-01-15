package com.triptune.schedule;

import com.triptune.BaseTest;
import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.member.entity.Member;
import com.triptune.schedule.dto.request.*;
import com.triptune.schedule.enums.AttendeePermission;

import java.time.LocalDate;
import java.util.List;

public abstract class ScheduleTest extends BaseTest {
    protected ScheduleCreateRequest createScheduleRequest(String scheduleName, LocalDate startDate, LocalDate endDate) {
        return ScheduleCreateRequest.builder()
                .scheduleName(scheduleName)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    protected ScheduleUpdateRequest createUpdateScheduleRequest(String scheduleName, LocalDate startDate, LocalDate endDate, RouteRequest... routeRequest){
        return ScheduleUpdateRequest.builder()
                .scheduleName(scheduleName)
                .startDate(startDate)
                .endDate(endDate)
                .travelRoutes(List.of(routeRequest))
                .build();
    }


    protected RouteRequest createRouteRequest(Integer routeOrder, Long placeId){
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

    protected MemberProfileResponse createMemberProfileResponse(Long memberId, String nickname, String profileUrl){
        return MemberProfileResponse.builder()
                .memberId(memberId)
                .nickname(nickname)
                .profileUrl(profileUrl)
                .build();
    }

}
