package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.global.enumclass.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendeeServiceTest extends ScheduleTest {
    @InjectMocks
    private AttendeeService attendeeService;

    @Mock
    private TravelAttendeeRepository travelAttendeeRepository;

    private TravelSchedule schedule1;
    private Member member1;
    private Member member2;
    private TravelAttendee attendee1;
    private TravelAttendee attendee2;

    @BeforeEach
    void setUp(){
        member1 = createMember(1L, "member1");
        member2 = createMember(2L, "member2");
        Member member3 = createMember(3L, "member3");

        schedule1 = createTravelSchedule(1L, "테스트1");
        TravelSchedule schedule2 = createTravelSchedule(2L, "테스트2");

        attendee1 = createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        attendee2 = createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ);
        TravelAttendee attendee3 = createTravelAttendee(member3, schedule1, AttendeeRole.GUEST, AttendeePermission.CHAT);
        TravelAttendee attendee4 = createTravelAttendee(member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2, attendee3)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));
    }

    @Test
    @DisplayName("removeAttendee(): 일정 참석자 제거")
    void removeAttendee(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee2));

        // when
        attendeeService.removeAttendee(schedule1.getScheduleId(), member2.getUserId());

        // then
        verify(travelAttendeeRepository, times(1)).deleteById(any());
    }

    @Test
    @DisplayName("removeAttendee(): 일정 참석자 제거 시 참가자 정보가 없어 예외 발생")
    void removeAttendeeNoAttendeeData_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.empty());

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> attendeeService.removeAttendee(schedule1.getScheduleId(), member1.getUserId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("removeAttendee(): 일정 참석자 제거 시 사용자가 작성자여서 예외 발생")
    void removeAttendeeIsAuthor_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee1));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> attendeeService.removeAttendee(schedule1.getScheduleId(), member1.getUserId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage());
    }

}
