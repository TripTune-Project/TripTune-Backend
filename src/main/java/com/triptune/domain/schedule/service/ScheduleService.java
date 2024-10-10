package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.AttendeeDTO;
import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.CreateScheduleResponse;
import com.triptune.domain.schedule.dto.ScheduleResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.AttendeeRepository;
import com.triptune.domain.schedule.repository.ScheduleRepository;
import com.triptune.domain.travel.dto.TravelSimpleResponse;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.PageResponse;
import com.triptune.global.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final AttendeeRepository attendeeRepository;
    private final TravelRepository travelRepository;

    public CreateScheduleResponse createSchedule(CreateScheduleRequest createScheduleRequest, String userId){
        TravelSchedule travelSchedule = TravelSchedule.builder()
                .scheduleName(createScheduleRequest.getScheduleName())
                .startDate(createScheduleRequest.getStartDate())
                .endDate(createScheduleRequest.getEndDate())
                .createdAt(LocalDateTime.now())
                .build();

        TravelSchedule savedTravelSchedule = scheduleRepository.save(travelSchedule);

        Member member = getSavedMember(userId);

        TravelAttendee travelAttendee = TravelAttendee.builder()
                .travelSchedule(savedTravelSchedule)
                .member(member)
                .role(AttendeeRole.AUTHOR)
                .permission(AttendeePermission.ALL)
                .build();

        attendeeRepository.save(travelAttendee);

        return CreateScheduleResponse.entityToDto(savedTravelSchedule);
    }

    public ScheduleResponse getSchedule(Long scheduleId, int page) {
        TravelSchedule schedule = getSavedSchedule(scheduleId);

        // 여행지 정보: Page<TravelPlace> -> PageResponse<TravelSimpleResponse> 로 변경
        Page<TravelSimpleResponse> travelPlacesDTO = getSimpleTravelPlacesByJunggu(page);
        PageResponse<TravelSimpleResponse> placeDTOList = PageResponse.of(travelPlacesDTO);

        List<AttendeeDTO> attendeeDTOList = attendeeRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId())
                .stream()
                .map(AttendeeDTO::entityToDTO)
                .toList();
        return ScheduleResponse.entityToDTO(schedule, placeDTOList, attendeeDTOList);
    }

    public Page<TravelSimpleResponse> getTravelPlaces(Long scheduleId, int page) {
        getSavedSchedule(scheduleId);
        return getSimpleTravelPlacesByJunggu(page);
    }


    public Page<TravelSimpleResponse> getSimpleTravelPlacesByJunggu(int page) {
        Pageable pageable = PageableUtil.createPageRequest(page, 5);
        Page<TravelPlace> travelPlaces = travelRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구");

        return TravelSimpleResponse.entityPageToDtoPage(travelPlaces, pageable);
    }


    public Member getSavedMember(String userId){
        return memberRepository.findByUserId(userId)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    public TravelSchedule getSavedSchedule(Long scheduleId){
        return scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND));
    }


}
