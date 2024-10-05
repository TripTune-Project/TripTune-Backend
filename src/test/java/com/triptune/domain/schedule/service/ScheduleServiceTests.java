package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.CreateScheduleResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.repository.AttendeeRepository;
import com.triptune.domain.schedule.repository.ScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@Slf4j
public class ScheduleServiceTests {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AttendeeRepository attendeeRepository;


    @Test
    @DisplayName("일정 만들기 성공")
    void createSchedule_success(){
        // given
        CreateScheduleRequest request = createScheduleRequest();
        TravelSchedule savedtravelSchedule = createTravelSchedule();
        Member savedMember = createMember();

        when(scheduleRepository.save(any())).thenReturn(savedtravelSchedule);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(savedMember));

        // when
        CreateScheduleResponse response = scheduleService.createSchedule(request, "test");

        // then
        verify(attendeeRepository, times(1)).save(any(TravelAttendee.class));
        assertEquals(response.getScheduleId(), savedtravelSchedule.getScheduleId());

    }

    @Test
    @DisplayName("일정 만들기 실패: 저장된 사용자 정보 없을 경우")
    void createSchedule_CustomUsernameNotFoundException(){
        // given
        CreateScheduleRequest request = createScheduleRequest();
        TravelSchedule travelSchedule = createTravelSchedule();

        when(scheduleRepository.save(any())).thenReturn(travelSchedule);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.createSchedule(request, "test"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }


    private CreateScheduleRequest createScheduleRequest(){
        return CreateScheduleRequest.builder()
                .scheduleName("테스트")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .build();
    }

    private TravelSchedule createTravelSchedule(){
        return TravelSchedule.builder()
                .scheduleId(1L)
                .scheduleName("테스트")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Member createMember(){
        return Member.builder()
                .memberId(1L)
                .userId("test")
                .build();
    }

}
