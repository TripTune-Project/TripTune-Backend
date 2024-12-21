package com.triptune.domain.schedule;

import com.triptune.domain.BaseTest;
import com.triptune.domain.schedule.dto.request.*;
import com.triptune.domain.schedule.enumclass.AttendeePermission;

import java.time.LocalDate;
import java.util.List;

public abstract class ScheduleTest extends BaseTest {
    protected ScheduleCreateRequest createScheduleRequest() {
        return ScheduleCreateRequest.builder()
                .scheduleName("테스트")
                .startDate(LocalDate.now())
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

}
