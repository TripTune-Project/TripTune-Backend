package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.CustomUsernameNotFoundException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.ScheduleRequest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.repository.AttendeeRepository;
import com.triptune.domain.schedule.repository.ScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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
        ScheduleRequest request = createScheduleRequest();
        TravelSchedule travelSchedule = createTravelSchedule();
        Member savedMember = createMember();

        when(scheduleRepository.save(any())).thenReturn(travelSchedule);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(savedMember));

        // when
        scheduleService.createSchedule(request, "test");

        // then
        verify(attendeeRepository, times(1)).save(any(TravelAttendee.class));

    }

    @Test
    @DisplayName("일정 만들기 실패: 저장된 사용자 정보 없을 경우")
    void createSchedule_CustomUsernameNotFoundException(){
        // given
        ScheduleRequest request = createScheduleRequest();
        TravelSchedule travelSchedule = createTravelSchedule();

        when(scheduleRepository.save(any())).thenReturn(travelSchedule);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.empty());

        // when
        CustomUsernameNotFoundException fail = assertThrows(CustomUsernameNotFoundException.class, () -> scheduleService.createSchedule(request, "test"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.NOT_FOUND_USER.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.NOT_FOUND_USER.getMessage());

    }


    private ScheduleRequest createScheduleRequest(){
        return ScheduleRequest.builder()
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
