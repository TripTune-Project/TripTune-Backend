package com.triptune.schedule.fixture;


import com.triptune.global.response.page.PageResponse;
import com.triptune.member.entity.Member;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.dto.response.OverviewScheduleResponse;
import com.triptune.schedule.dto.response.ScheduleCreateResponse;
import com.triptune.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import com.triptune.schedule.service.dto.AuthorDTO;
import com.triptune.travel.dto.response.PlaceResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

public class TravelScheduleFixture {
    
    public static TravelSchedule createSchedule(String scheduleName){
        return TravelSchedule.createTravelSchedule(
                scheduleName,
                LocalDate.now(),
                LocalDate.now()
        );
    }

    public static TravelSchedule createScheduleWithId(Long scheduleId, String scheduleName){
        TravelSchedule travelSchedule = TravelSchedule.createTravelSchedule(
                scheduleName,
                LocalDate.now(),
                LocalDate.now()
        );

        ReflectionTestUtils.setField(travelSchedule, "scheduleId", scheduleId);

        return travelSchedule;
    }


    public static ScheduleCreateRequest createScheduleRequest(String scheduleName, LocalDate startDate, LocalDate endDate) {
        return ScheduleCreateRequest.builder()
                .scheduleName(scheduleName)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public static ScheduleUpdateRequest createUpdateScheduleRequest(String scheduleName, LocalDate startDate, LocalDate endDate, RouteRequest... routeRequest){
        return ScheduleUpdateRequest.builder()
                .scheduleName(scheduleName)
                .startDate(startDate)
                .endDate(endDate)
                .travelRoutes(List.of(routeRequest))
                .build();
    }


    public static ScheduleInfoQueryDto createScheduleInfoQueryDto(TravelSchedule schedule, TravelAttendee current, TravelAttendee author, String thumbnailS3ObjectKey) {
        return ScheduleInfoQueryDto.builder()
                .scheduleId(schedule.getScheduleId())
                .attendeeRole(current.getRole())
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .thumbnailS3ObjectKey(thumbnailS3ObjectKey)
                .authorNickname(author.getMember().getNickname())
                .authorS3ObjectKey(author.getMember().getProfileImage().getS3ObjectKey())
                .build();
    }

    public static ScheduleInfoResponse createScheduleInfoResponse(TravelSchedule schedule,
                                                                  AttendeeRole role,
                                                                  String sinceUpdate,
                                                                  String thumbnailUrl,
                                                                  Member member) {

        AuthorDTO author = AuthorDTO.of(
                member.getNickname(),
                "http://test.com/" + member.getProfileImage().getS3ObjectKey()
        );

        return ScheduleInfoResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .role(role)
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .sinceUpdate(sinceUpdate)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .build();
    }

    public static OverviewScheduleResponse createOverviewScheduleResponse(TravelSchedule schedule, String author) {
        return OverviewScheduleResponse
                .builder()
                .scheduleId(schedule.getScheduleId())
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .author(author)
                .build();
    }

    public static ScheduleCreateResponse createScheduleCreateResponse(Long scheduleId) {
        return ScheduleCreateResponse.builder().scheduleId(scheduleId).build();

    }

    public static ScheduleDetailResponse createScheduleDetailResponse(TravelSchedule schedule, PageResponse<PlaceResponse> placeResponse) {
        return ScheduleDetailResponse.from(schedule, placeResponse);

    }
}
