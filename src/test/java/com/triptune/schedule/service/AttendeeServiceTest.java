package com.triptune.schedule.service;

import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.schedule.dto.request.AttendeeRequest;
import com.triptune.schedule.dto.response.AttendeeResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.exception.ConflictAttendeeException;
import com.triptune.schedule.exception.ForbiddenAttendeeException;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendeeServiceTest extends ScheduleTest {
    @InjectMocks private AttendeeService attendeeService;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private MemberRepository memberRepository;

    private TravelSchedule schedule1;
    private Member member1;
    private Member member2;
    private Member member3;
    private TravelAttendee attendee1;
    private TravelAttendee attendee2;

    @BeforeEach
    void setUp(){
        ProfileImage profileImage1 = createProfileImage(1L, "member1Image");
        ProfileImage profileImage2 = createProfileImage(2L, "member2Image");
        ProfileImage profileImage3 = createProfileImage(3L, "member3Image");
        member1 = createMember(1L, "member1@email.com", profileImage1);
        member2 = createMember(2L, "member2@email.com", profileImage2);
        member3 = createMember(3L, "member3@email.com", profileImage3);

        schedule1 = createTravelSchedule(1L, "테스트1");
        TravelSchedule schedule2 = createTravelSchedule(2L, "테스트2");

        attendee1 = createTravelAttendee(1L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        attendee2 = createTravelAttendee(2L, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ);
        TravelAttendee attendee3 = createTravelAttendee(3L, member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);
        schedule2.addTravelAttendee(attendee3);
    }

    @Test
    @DisplayName("일정 참석자 조회")
    void getAttendeesByScheduleId(){
        // given
        List<TravelAttendee> travelAttendees = schedule1.getTravelAttendees();

        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(schedule1.getScheduleId()))
                .thenReturn(travelAttendees);

        // when
        List<AttendeeResponse> response = attendeeService.getAttendeesByScheduleId(schedule1.getScheduleId());

        // then
        assertThat(response.size()).isEqualTo(travelAttendees.size());
        assertThat(response.get(0).getNickname()).isEqualTo(attendee1.getMember().getNickname());
        assertThat(response.get(0).getRole()).isEqualTo(attendee1.getRole().name());
        assertThat(response.get(0).getProfileUrl()).isEqualTo(attendee1.getMember().getProfileImage().getS3ObjectUrl());
        assertThat(response.get(1).getNickname()).isEqualTo(attendee2.getMember().getNickname());
        assertThat(response.get(1).getRole()).isEqualTo(attendee2.getRole().name());
        assertThat(response.get(1).getProfileUrl()).isEqualTo(attendee2.getMember().getProfileImage().getS3ObjectUrl());
    }

    @Test
    @DisplayName("일정 참석자 조회 시 데이터 없는 경우")
    void getAttendeesByScheduleIdNoData(){
        // given
        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(schedule1.getScheduleId()))
                .thenReturn(Collections.emptyList());

        // when
        List<AttendeeResponse> response = attendeeService.getAttendeesByScheduleId(schedule1.getScheduleId());

        // then
        assertThat(response.size()).isEqualTo(0);
    }


    @Test
    @DisplayName("일정 참석자 추가")
    void createAttendee(){
        // given
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(schedule1.getScheduleId(), member1.getMemberId(), AttendeeRole.AUTHOR))
                .thenReturn(true);
        when(memberRepository.findByEmail(member3.getEmail())).thenReturn(Optional.of(member3));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(false);


        // when, then
        assertDoesNotThrow(() ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendeeRequest));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 일정 찾을 수 없어 예외 발생")
    void createAttendee_scheduleNotFound(){
        // given
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendeeRequest));

        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("일정 참석자 추가 시 참석자 5명 넘어 예외 발생")
    void createAttendee_overFiveAttendee() {
        // given
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(5);

        // when, then
        ConflictAttendeeException fail = assertThrows(ConflictAttendeeException.class,
                () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendeeRequest));

        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.OVER_ATTENDEE_NUMBER.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.OVER_ATTENDEE_NUMBER.getMessage());

    }

    @Test
    @DisplayName("일정 참석자 추가 시 작성자가 아닌 사람의 요청으로 예외 발생")
    void createAttendee_forbiddenNotAuthor(){
        // given
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(4);
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(schedule1.getScheduleId(), member1.getMemberId(), AttendeeRole.AUTHOR))
                .thenReturn(false);

        // when, then
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendeeRequest));

        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getMessage());

    }

    @Test
    @DisplayName("일정 참석자 추가 시 참석자 정보를 찾을 수 없어 예외 발생")
    void createAttendee_forbiddenNotAttendee(){
        // given
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(4);
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(schedule1.getScheduleId(), member1.getMemberId(), AttendeeRole.AUTHOR))
                .thenReturn(true);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when, then
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendeeRequest));

        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("일정 참석자 추가 시 이미 참석자로 존재해 예외 발생")
    void createAttendee_alreadyAttendee(){
        // given
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(4);
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(schedule1.getScheduleId(), member1.getMemberId(), AttendeeRole.AUTHOR))
                .thenReturn(true);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member3));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(true);


        // when, then
        ConflictAttendeeException fail = assertThrows(ConflictAttendeeException.class,
                () ->  attendeeService.createAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendeeRequest));

        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_ATTENDEE.getMessage());

    }

    @Test
    @DisplayName("일정 참석자 5명 넘는지 확인")
    void validateAttendeeCount(){
        // given
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(4);

        // when, then
        assertDoesNotThrow(() -> attendeeService.validateAttendeeCount(schedule1.getScheduleId()));
    }

    @Test
    @DisplayName("일정 참석자 5명 넘어 예외 발생")
    void validateAttendeeCount_overFiveAttendee(){
        // given
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(5);

        // when
        ConflictAttendeeException fail = assertThrows(ConflictAttendeeException.class,
                () -> attendeeService.validateAttendeeCount(schedule1.getScheduleId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.OVER_ATTENDEE_NUMBER.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.OVER_ATTENDEE_NUMBER.getMessage());
    }

    @Test
    @DisplayName("작성자인지 검증")
    void validateAuthor(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any())).thenReturn(true);

        // when, then
        assertDoesNotThrow(() -> attendeeService.validateAuthor(schedule1.getScheduleId(), member1.getMemberId(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));
    }

    @Test
    @DisplayName("작성자인지 검증 시 예외 발생")
    void validateAuthor_forbiddenAttendee(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any())).thenReturn(false);

        // when, then
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> attendeeService.validateAuthor(schedule1.getScheduleId(), member1.getMemberId(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("기존 참석자인지 검증")
    void validateAttendeeAlreadyExists(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(false);

        // when, then
        assertDoesNotThrow(() -> attendeeService.validateAttendeeAlreadyExists(schedule1.getScheduleId(), member1.getMemberId()));
    }

    @Test
    @DisplayName("기존 참석자인지 검증 시 예외 발생")
    void validateAttendee_alreadyAttendee(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(true);

        // when, then
        ConflictAttendeeException fail = assertThrows(ConflictAttendeeException.class,
                () -> attendeeService.validateAttendeeAlreadyExists(schedule1.getScheduleId(), member1.getMemberId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_ATTENDEE.getMessage());
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정")
    void updateAttendeePermission(){
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any())).thenReturn(true);
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(anyLong(), anyLong())).thenReturn(Optional.of(attendee2));

        // when
        assertDoesNotThrow(() -> attendeeService.updateAttendeePermission(schedule1.getScheduleId(), member1.getMemberId(), attendee2.getAttendeeId(), request));

        // then
        assertThat(attendee2.getPermission()).isEqualTo(AttendeePermission.READ);
    }


    @Test
    @DisplayName("일정 참석자 허용 권한 수정 시 요청자가 작성자가 아니여서 예외 발생")
    void updateAttendeePermission_forbiddenNotAuthor(){
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any())).thenReturn(false);

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> attendeeService.updateAttendeePermission(schedule1.getScheduleId(), member3.getMemberId(), attendee2.getAttendeeId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION.getMessage());
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 참석자가 정보가 없어 예외 발생")
    void updateAttendeePermission_attendeeNotFound(){
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any())).thenReturn(true);
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> attendeeService.updateAttendeePermission(schedule1.getScheduleId(), member1.getMemberId(), attendee2.getAttendeeId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND.getMessage());
    }



    @Test
    @DisplayName("일정 참석자 허용 권한 수정 시 작성자의 접근 권한 수정 시도로 예외 발생")
    void updateAttendeePermission_forbiddenUpdateAuthorPermission(){
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any())).thenReturn(true);
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(anyLong(), anyLong())).thenReturn(Optional.of(attendee1));

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> attendeeService.updateAttendeePermission(schedule1.getScheduleId(), member1.getMemberId(), attendee1.getAttendeeId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION.getMessage());
    }



    @Test
    @DisplayName("일정 나가기")
    void leaveAttendee(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(Optional.of(attendee2));

        // when
        attendeeService.leaveAttendee(schedule1.getScheduleId(), member2.getMemberId());

        // then
        verify(travelAttendeeRepository, times(1)).deleteById(any());
    }

    @Test
    @DisplayName("일정 나가기 요청 시 참가자 정보가 없어 예외 발생")
    void leaveAttendee_forbiddenAttendee(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> attendeeService.leaveAttendee(schedule1.getScheduleId(), member1.getMemberId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("일정 나가기 요청 시 요청자가 작성자여서 예외 발생")
    void leaveAttendee_forbiddenAuthor(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(Optional.of(attendee1));

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> attendeeService.leaveAttendee(schedule1.getScheduleId(), member1.getMemberId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage());
    }

    @Test
    @DisplayName("일정 내보내기")
    void removeAttendee(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(travelAttendeeRepository.findById(anyLong())).thenReturn(Optional.of(attendee2));

        // when
        // then
        assertDoesNotThrow(() -> attendeeService.removeAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendee2.getAttendeeId()));
    }

    @Test
    @DisplayName("일정 내보내기 시 작성자 요청이 아니여서 예외 발생")
    void removeAttendee_forbiddenNotAuthor(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any()))
                .thenReturn(false);

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> attendeeService.removeAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendee2.getAttendeeId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage());
    }

    @Test
    @DisplayName("일정 내보내기 시 참석자를 찾을 수 없어 예외 발생")
    void removeAttendee_attendeeNotFound(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(travelAttendeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> attendeeService.removeAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendee2.getAttendeeId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("일정 내보내기 시 작성자 내보내기 시도로 예외 발생")
    void removeAttendee_forbiddenRemoveAuthor(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(travelAttendeeRepository.findById(anyLong())).thenReturn(Optional.of(attendee1));

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> attendeeService.removeAttendee(schedule1.getScheduleId(), member1.getMemberId(), attendee2.getAttendeeId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage());
    }

}
