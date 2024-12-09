package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.request.CreateAttendeeRequest;
import com.triptune.domain.schedule.dto.response.AttendeeResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.AlreadyAttendeeException;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
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

    @Mock
    private TravelScheduleRepository travelScheduleRepository;

    @Mock
    private MemberRepository memberRepository;

    private TravelSchedule schedule1;
    private Member member1;
    private Member member2;
    private Member member3;
    private TravelAttendee attendee1;
    private TravelAttendee attendee2;

    @BeforeEach
    void setUp(){
        member1 = createMember(1L, "member1");
        member2 = createMember(2L, "member2");
        member3 = createMember(3L, "member3");

        ProfileImage member1Image = createProfileImage(1L, "member1Image");
        ProfileImage member2Image = createProfileImage(2L, "member2Image");
        ProfileImage member3Image = createProfileImage(3L, "member3Image");
        member1.setProfileImage(member1Image);
        member2.setProfileImage(member2Image);
        member3.setProfileImage(member3Image);

        schedule1 = createTravelSchedule(1L, "테스트1");
        TravelSchedule schedule2 = createTravelSchedule(2L, "테스트2");

        attendee1 = createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        attendee2 = createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ);
        TravelAttendee attendee3 = createTravelAttendee(member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
    }

    @Test
    @DisplayName("getAttendeesByScheduleId(): 일정 참석자 조회")
    void getAttendeesByScheduleId(){
        // given
        List<TravelAttendee> travelAttendeeList = schedule1.getTravelAttendeeList();

        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(schedule1.getScheduleId()))
                .thenReturn(travelAttendeeList);

        // when
        List<AttendeeResponse> response = attendeeService.getAttendeesByScheduleId(schedule1.getScheduleId());

        // then
        assertEquals(response.size(), travelAttendeeList.size());
        assertEquals(response.get(0).getNickname(), attendee1.getMember().getNickname());
        assertEquals(response.get(0).getRole(), attendee1.getRole().name());
        assertEquals(response.get(0).getProfileUrl(), attendee1.getMember().getProfileImage().getS3ObjectUrl());
        assertEquals(response.get(1).getNickname(), attendee2.getMember().getNickname());
        assertEquals(response.get(1).getRole(), attendee2.getRole().name());
        assertEquals(response.get(1).getProfileUrl(), attendee2.getMember().getProfileImage().getS3ObjectUrl());
    }

    @Test
    @DisplayName("getAttendeesByScheduleId(): 일정 참석자 조회 시 데이터 없는 경우")
    void getAttendeesByScheduleIdNoData(){
        // given
        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(schedule1.getScheduleId()))
                .thenReturn(Collections.emptyList());

        // when
        List<AttendeeResponse> response = attendeeService.getAttendeesByScheduleId(schedule1.getScheduleId());

        // then
        assertEquals(response.size(), 0);
    }


    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가")
    void createAttendee(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(schedule1.getScheduleId(), member1.getUserId(), AttendeeRole.AUTHOR))
                .thenReturn(true);
        when(memberRepository.findByEmail(member3.getEmail())).thenReturn(Optional.of(member3));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(false);


        // when, then
        assertDoesNotThrow(() ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 일정 찾을 수 없어 예외 발생")
    void createAttendeeNotFoundSchedule_dataNotFoundException(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.empty());

        // when, then
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

        assertEquals(fail.getHttpStatus(), ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.SCHEDULE_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 작성자가 아닌 사람의 요청으로 예외 발생")
    void createAttendeeNotAuthor_forbiddenScheduleException(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(schedule1.getScheduleId(), member1.getUserId(), AttendeeRole.AUTHOR))
                .thenReturn(false);

        // when, then
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getMessage());

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 참석자 정보를 찾을 수 없어 예외 발생")
    void createAttendeeNotMember_forbiddenScheduleException(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(schedule1.getScheduleId(), member1.getUserId(), AttendeeRole.AUTHOR))
                .thenReturn(true);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when, then
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 이미 참석자로 존재해 예외 발생")
    void createAttendee_alreadyAttendeeException(){
        // given
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(schedule1.getScheduleId(), member1.getUserId(), AttendeeRole.AUTHOR))
                .thenReturn(true);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member3));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(true);


        // when, then
        AlreadyAttendeeException fail = assertThrows(AlreadyAttendeeException.class, () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getUserId(), createAttendeeRequest));

        assertEquals(fail.getHttpStatus(), ErrorCode.ALREADY_ATTENDEE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.ALREADY_ATTENDEE.getMessage());

    }


    @Test
    @DisplayName("leaveScheduleAsGuest(): 일정 참석자 제거")
    void leaveScheduleAsGuest(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee2));

        // when
        attendeeService.leaveScheduleAsGuest(schedule1.getScheduleId(), member2.getUserId());

        // then
        verify(travelAttendeeRepository, times(1)).deleteById(any());
    }

    @Test
    @DisplayName("leaveScheduleAsGuest(): 일정 참석자 제거 시 참가자 정보가 없어 예외 발생")
    void leaveScheduleAsGuest_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.empty());

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> attendeeService.leaveScheduleAsGuest(schedule1.getScheduleId(), member1.getUserId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("leaveScheduleAsGuest(): 일정 참석자 제거 시 사용자가 작성자여서 예외 발생")
    void leaveScheduleAsGuestIsAuthor_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee1));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> attendeeService.leaveScheduleAsGuest(schedule1.getScheduleId(), member1.getUserId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage());
    }

}
