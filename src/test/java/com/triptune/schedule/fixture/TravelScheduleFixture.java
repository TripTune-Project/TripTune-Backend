package com.triptune.schedule.fixture;


import com.triptune.global.response.page.PageResponse;
import com.triptune.member.entity.Member;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.dto.response.*;
import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.attendee.enums.AttendeeRole;
import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import com.triptune.attendee.service.dto.AuthorDTO;
import com.triptune.travel.dto.response.PlaceResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

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

    public static TravelSchedule createScheduleWithIdAndVersion(Long scheduleId, String scheduleName, Integer scheduleVersion, Integer routeVersion){
        TravelSchedule travelSchedule = TravelSchedule.createTravelSchedule(
                scheduleName,
                LocalDate.now(),
                LocalDate.now()
        );

        ReflectionTestUtils.setField(travelSchedule, "scheduleId", scheduleId);
        ReflectionTestUtils.setField(travelSchedule, "scheduleVersion", scheduleVersion);
        ReflectionTestUtils.setField(travelSchedule, "routeVersion", routeVersion);

        return travelSchedule;
    }



    public static ScheduleCreateRequest createScheduleCreateRequest(String scheduleName, LocalDate startDate, LocalDate endDate) {
        return ScheduleCreateRequest.builder()
                .scheduleName(scheduleName)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public static ScheduleUpdateRequest createScheduleUpdateRequest(String scheduleName, LocalDate startDate, LocalDate endDate, Integer scheduleVersion){
        return ScheduleUpdateRequest.builder()
                .scheduleName(scheduleName)
                .startDate(startDate)
                .endDate(endDate)
                .scheduleVersion(scheduleVersion)
                .build();
    }

    public static ScheduleUpdateResponse createScheduleUpdateResponse(Integer scheduleVersion){
        return ScheduleUpdateResponse.builder()
                .scheduleVersion(scheduleVersion)
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

    public static ScheduleOverviewResponse createOverviewScheduleResponse(TravelSchedule schedule, String author) {
        return ScheduleOverviewResponse
                .builder()
                .scheduleId(schedule.getScheduleId())
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .author(author)
                .build();
    }

    public static ScheduleCreateResponse createScheduleCreateResponse(Long scheduleId) {
        return ScheduleCreateResponse.builder().scheduleId(scheduleId).scheduleVersion(0).build();

    }

    public static ScheduleDetailResponse createScheduleDetailResponse(TravelSchedule schedule, PageResponse<PlaceResponse> placeResponse) {
        return ScheduleDetailResponse.from(schedule, placeResponse);

    }

    public static ScheduleUpdateEventResponse createScheduleUpdateEventResponse(TravelSchedule schedule) {
        return ScheduleUpdateEventResponse.from(schedule);
    }
}
