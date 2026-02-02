package com.triptune.schedule.service;

import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.message.ErrorCode;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
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
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TravelAttendeeServiceTest {
    @InjectMocks private TravelAttendeeService travelAttendeeService;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private MemberRepository memberRepository;

    private TravelSchedule schedule;

    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp(){
        ProfileImage profileImage1 = ProfileImageFixture.createProfileImage("member1Image");
        member1 = MemberFixture.createNativeTypeMember( "member1@email.com", profileImage1);
        ProfileImage profileImage2 = ProfileImageFixture.createProfileImage("member2Image");
        member2 = MemberFixture.createNativeTypeMember("member2@email.com", profileImage2);
        ProfileImage profileImage3 = ProfileImageFixture.createProfileImage("member3Image");
        member3 = MemberFixture.createNativeTypeMember("member3@email.com", profileImage3);

        schedule = TravelScheduleFixture.createTravelSchedule("테스트1");
    }

    @Test
    @DisplayName("일정 참석자 조회")
    void getAttendeesByScheduleId(){
        // given
        List<TravelAttendee> travelAttendees = List.of(
                TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1),
                TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ)
        );

        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(anyLong()))
                .thenReturn(travelAttendees);

        // when
        List<AttendeeResponse> response = travelAttendeeService.getAttendeesByScheduleId(1L);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response)
                .extracting(
                        AttendeeResponse::getNickname,
                        AttendeeResponse::getRole,
                        AttendeeResponse::getProfileUrl
                )
                .containsExactly(
                        tuple(
                                member1.getNickname(),
                                AttendeeRole.AUTHOR.name(),
                                member1.getProfileImage().getS3ObjectUrl()
                        ),
                        tuple(
                                member2.getNickname(),
                                AttendeeRole.GUEST.name(),
                                member2.getProfileImage().getS3ObjectUrl()
                        )
                );
    }

    @Test
    @DisplayName("일정 참석자 조회 시 데이터 없는 경우")
    void getAttendeesByScheduleId_emptyResult(){
        // given
        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(anyLong()))
                .thenReturn(Collections.emptyList());

        // when
        List<AttendeeResponse> response = travelAttendeeService.getAttendeesByScheduleId(1L);

        // then
        assertThat(response.size()).isEqualTo(0);
    }


    @Test
    @DisplayName("일정 참석자 추가")
    void createAttendee(){
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("newMember");
        Member newMember = MemberFixture.createNativeTypeMemberWithId(1L, "newMember@email.com", profileImage);

        AttendeeRequest attendeeRequest = TravelAttendeeFixture.createAttendeeRequest(newMember.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any(AttendeeRole.class)))
                .thenReturn(true);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(newMember));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(false);


        // when, then
        assertDoesNotThrow(
                () ->  travelAttendeeService.createAttendee(1L, 1L, attendeeRequest));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 일정 찾을 수 없어 예외 발생")
    void createAttendee_scheduleNotFound(){
        // given
        AttendeeRequest attendeeRequest = TravelAttendeeFixture.createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () ->  travelAttendeeService.createAttendee(1000L, 1L, attendeeRequest));

        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);

    }

    @Test
    @DisplayName("일정 참석자 추가 시 참석자 5명 넘어 예외 발생")
    void createAttendee_overFiveAttendee() {
        // given
        AttendeeRequest attendeeRequest = TravelAttendeeFixture.createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(5);

        // when, then
        ConflictAttendeeException fail = assertThrows(ConflictAttendeeException.class,
                () ->  travelAttendeeService.createAttendee(1L, 1L, attendeeRequest));

        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.OVER_ATTENDEE_NUMBER);

    }

    @Test
    @DisplayName("일정 참석자 추가 시 작성자가 아닌 사람의 요청으로 예외 발생")
    void createAttendee_forbiddenNotAuthor(){
        // given
        AttendeeRequest attendeeRequest = TravelAttendeeFixture.createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(4);
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any(AttendeeRole.class)))
                .thenReturn(false);

        // when, then
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () ->  travelAttendeeService.createAttendee(1L, 1L, attendeeRequest));

        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_SHARE_ATTENDEE);

    }

    @Test
    @DisplayName("일정 참석자 추가 시 참석자 정보를 찾을 수 없어 예외 발생")
    void createAttendee_forbiddenNotAttendee(){
        // given
        AttendeeRequest attendeeRequest = TravelAttendeeFixture.createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(4);
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any(AttendeeRole.class)))
                .thenReturn(true);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when, then
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () ->  travelAttendeeService.createAttendee(1L, 1L, attendeeRequest));

        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

    }

    @Test
    @DisplayName("일정 참석자 추가 시 이미 참석자로 존재해 예외 발생")
    void createAttendee_alreadyAttendee(){
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("newMember");
        Member newMember = MemberFixture.createNativeTypeMemberWithId(1L, "newMember@email.com", profileImage);

        AttendeeRequest attendeeRequest = TravelAttendeeFixture.createAttendeeRequest(newMember.getEmail(), AttendeePermission.CHAT);

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(4);
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any(AttendeeRole.class)))
                .thenReturn(true);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(newMember));
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(true);


        // when, then
        ConflictAttendeeException fail = assertThrows(ConflictAttendeeException.class,
                () ->  travelAttendeeService.createAttendee(1L, 1L, attendeeRequest));

        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.ALREADY_ATTENDEE);

    }

    @Test
    @DisplayName("일정 참석자 5명 넘는지 확인")
    void validateAttendeeCount(){
        // given
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(4);

        // when, then
        assertDoesNotThrow(() -> travelAttendeeService.validateAttendeeCount(1L));
    }

    @Test
    @DisplayName("일정 참석자 5명 넘어 예외 발생")
    void validateAttendeeCount_overFiveAttendee(){
        // given
        when(travelAttendeeRepository.countByTravelSchedule_ScheduleId(anyLong())).thenReturn(5);

        // when
        ConflictAttendeeException fail = assertThrows(ConflictAttendeeException.class,
                () -> travelAttendeeService.validateAttendeeCount(1L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.OVER_ATTENDEE_NUMBER);
    }

    @Test
    @DisplayName("작성자인지 검증")
    void validateAuthor(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any(AttendeeRole.class)))
                .thenReturn(true);

        // when, then
        assertDoesNotThrow(() -> travelAttendeeService.validateAuthor(1L, 1L, ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));
    }

    @Test
    @DisplayName("작성자인지 검증 시 예외 발생")
    void validateAuthor_forbiddenAttendee(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any(AttendeeRole.class)))
                .thenReturn(false);

        // when, then
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> travelAttendeeService.validateAuthor(1L, 1L, ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE);
    }

    @Test
    @DisplayName("기존 참석자인지 검증")
    void validateAttendeeAlreadyExists(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(false);

        // when, then
        assertDoesNotThrow(() -> travelAttendeeService.validateAttendeeAlreadyExists(1L, 1L));
    }

    @Test
    @DisplayName("기존 참석자인지 검증 시 예외 발생")
    void validateAttendee_alreadyAttendee(){
        // given
        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(true);

        // when, then
        ConflictAttendeeException fail = assertThrows(ConflictAttendeeException.class,
                () -> travelAttendeeService.validateAttendeeAlreadyExists(1L, 1L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.ALREADY_ATTENDEE);
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정")
    void updateAttendeePermission(){
        // given
        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendeeWithId(1L, schedule, member2, AttendeePermission.READ);

        AttendeePermissionRequest request = TravelAttendeeFixture.createAttendeePermissionRequest(AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any(AttendeeRole.class)))
                .thenReturn(true);
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(anyLong(), anyLong()))
                .thenReturn(Optional.of(guest));

        // when
        assertDoesNotThrow(
                () -> travelAttendeeService.updateAttendeePermission(
                        request,
                        1L,
                        1L,
                        guest.getAttendeeId()
                )
        );

        // then
        assertThat(guest.getPermission()).isEqualTo(AttendeePermission.READ);
    }


    @Test
    @DisplayName("일정 참석자 허용 권한 수정 시 요청자가 작성자가 아니여서 예외 발생")
    void updateAttendeePermission_forbiddenNotAuthor(){
        // given
        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        AttendeePermissionRequest request = TravelAttendeeFixture.createAttendeePermissionRequest(AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any())).thenReturn(false);

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> travelAttendeeService.updateAttendeePermission(
                        request,
                        1L,
                        3L,
                        guest.getAttendeeId()
                )
        );

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION);
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 참석자가 정보가 없어 예외 발생")
    void updateAttendeePermission_attendeeNotFound(){
        // given
        AttendeePermissionRequest request = TravelAttendeeFixture.createAttendeePermissionRequest(AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelAttendeeService.updateAttendeePermission(
                        request,
                        1L,
                        1L,
                        1000L
                )
        );

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND);
    }



    @Test
    @DisplayName("일정 참석자 허용 권한 수정 시 작성자의 접근 권한 수정 시도로 예외 발생")
    void updateAttendeePermission_forbiddenUpdateAuthorPermission(){
        // given
        TravelAttendee author = TravelAttendeeFixture.createAuthorTravelAttendeeWithId(1L, schedule, member1);

        AttendeePermissionRequest request = TravelAttendeeFixture.createAttendeePermissionRequest(AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndAttendeeId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> travelAttendeeService.updateAttendeePermission(
                        request,
                        1L,
                        1L,
                        author.getAttendeeId()
                )
        );

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION);
    }



    @Test
    @DisplayName("일정 나가기")
    void leaveAttendee(){
        // given
        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(guest));

        // when
        travelAttendeeService.leaveAttendee(1L, 2L);

        // then
        verify(travelAttendeeRepository, times(1)).deleteById(any());
    }

    @Test
    @DisplayName("일정 나가기 요청 시 참가자 정보가 없어 예외 발생")
    void leaveAttendee_forbiddenAttendee(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> travelAttendeeService.leaveAttendee(1L, 1L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE);
    }

    @Test
    @DisplayName("일정 나가기 요청 시 요청자가 작성자여서 예외 발생")
    void leaveAttendee_forbiddenAuthor(){
        // given
        TravelAttendee author = TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> travelAttendeeService.leaveAttendee(1L, 1L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_LEAVE_AUTHOR);
    }

    @Test
    @DisplayName("일정 내보내기")
    void removeAttendee(){
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("defaultImage");
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", profileImage);
        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendeeWithId(1L, schedule, guestMember, AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(travelAttendeeRepository.findById(anyLong())).thenReturn(Optional.of(guest));

        // when, then
        assertDoesNotThrow(() -> travelAttendeeService.removeAttendee(
                1L,
                1L,
                guest.getAttendeeId()
        ));
    }

    @Test
    @DisplayName("일정 내보내기 시 작성자 요청이 아니여서 예외 발생")
    void removeAttendee_forbiddenNotAuthor(){
        // given
        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any()))
                .thenReturn(false);

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> travelAttendeeService.removeAttendee(
                        1L,
                        1L,
                        guest.getAttendeeId())
        );

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE);
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
                () -> travelAttendeeService.removeAttendee(
                        1L,
                        1L,
                        1000L)
        );

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND);
    }

    @Test
    @DisplayName("일정 내보내기 시 작성자 내보내기 시도로 예외 발생")
    void removeAttendee_forbiddenRemoveAuthor(){
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("removeMember");
        Member removeMember = MemberFixture.createNativeTypeMemberWithId(1L, "removeMember@email.com", profileImage);
        TravelAttendee author = TravelAttendeeFixture.createAuthorTravelAttendeeWithId(1L, schedule, removeMember);

        when(travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(anyLong(), anyLong(), any(AttendeeRole.class)))
                .thenReturn(true);
        when(travelAttendeeRepository.findById(anyLong())).thenReturn(Optional.of(author));

        // when
        ForbiddenAttendeeException fail = assertThrows(ForbiddenAttendeeException.class,
                () -> travelAttendeeService.removeAttendee(
                        1L,
                        removeMember.getMemberId(),
                        author.getAttendeeId())
        );

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_LEAVE_AUTHOR);
    }

}
