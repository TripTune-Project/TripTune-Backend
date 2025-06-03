package com.triptune.schedule.controller;

import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.response.enums.SuccessCode;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.schedule.dto.request.AttendeeRequest;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("h2")
public class AttendeeControllerTest extends ScheduleTest {
    @Autowired private WebApplicationContext wac;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    private MockMvc mockMvc;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;

    private Member member1;
    private Member member2;
    private Member member3;

    TravelAttendee attendee1;
    TravelAttendee attendee2;
    TravelAttendee attendee3;


    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage(null, "member1Image"));
        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage(null, "member2Image"));
        ProfileImage profileImage3 = profileImageRepository.save(createProfileImage(null, "member3Image"));

        member1 = memberRepository.save(createMember(null, "member1@email.com", profileImage1));
        member2 = memberRepository.save(createMember(null, "member2@email.com", profileImage2));
        member3 = memberRepository.save(createMember(null, "member3@email.com", profileImage3));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));

        attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));

    }

    @Test
    @DisplayName("일정 참석자 조회")
    void getAttendees() throws Exception {
        mockAuthentication(member1);

        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data[0].profileUrl").value(member1.getProfileImage().getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[0].permission").value(attendee1.getPermission().name()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getAttendeesNoScheduleData_notFoundException() throws Exception {
        mockAuthentication(member1);

        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 접근 권한이 없어 예외 발생")
    void getAttendeesNotAccess_forbiddenScheduleException() throws Exception {
        mockAuthentication(member1);

        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가")
    void createAttendee() throws Exception {
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member1);

        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 일정 데이터 존재하지 않아 예외 발생")
    void createAttendeeNotFoundSchedule_dataNotFoundException() throws Exception{
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member1);

        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 일정 참석자 5명 넘어 예외 발생")
    void createAttendeeOver5_conflictAttendeeException() throws Exception {
        Member member4 = memberRepository.save(createMember(null, "member4@email.com"));
        Member member5 = memberRepository.save(createMember(null, "member5@email.com"));
        travelAttendeeRepository.save(createTravelAttendee(0L, member3, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL));
        travelAttendeeRepository.save(createTravelAttendee(0L, member4, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL));
        travelAttendeeRepository.save(createTravelAttendee(0L, member5, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL));

        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        mockAuthentication(member1);

        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.OVER_ATTENDEE_NUMBER.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 요청자가 작성자가 아니여서 예외 발생")
    void createAttendeeNotAuthor_forbiddenScheduleException() throws Exception{
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member2);

        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 초대자 데이터 존재하지 않아 예외 발생")
    void createAttendeeNotMember_dataNotFoundException() throws Exception{
        AttendeeRequest attendeeRequest = createAttendeeRequest("notMember@email.com", AttendeePermission.CHAT);
        mockAuthentication(member1);

        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 초대자가 이미 참석자여서 예외 발생")
    void createAttendee_alreadyAttendeeException() throws Exception{
        AttendeeRequest attendeeRequest = createAttendeeRequest(member2.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member1);

        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정")
    void updateAttendeePermission() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 일정이 존재하지 않아 예외 발생")
    void updateAttendeePermission_scheduleDataNotFoundException() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", 0L, attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 요청자가 작성자가 아니여서 예외 발생")
    void updateAttendeePermission_forbiddenScheduleException() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member2);

        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 참석자 정보 존재하지 않아 예외 발생")
    void updateAttendeePermission_attendeeDataNotFoundException() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee3.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 작성자 접근 권한 수정 시도로 예외 발생")
    void updateAttendeePermission_forbiddenUpdateAuthorPermissionException() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee1.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기")
    void leaveAttendee() throws Exception {
        mockAuthentication(member2);

        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("200(성공)"));

        Optional<TravelAttendee> result = travelAttendeeRepository.findById(attendee2.getAttendeeId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정 데이터 존재하지 않아 예외 발생")
    void leaveAttendee_scheduleDataNotFoundException() throws Exception {
        mockAuthentication(member2);

        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 나가기 요청 시 요청자가 작성자여서 예외 발생")
    void leaveAttendeeIsAuthor_forbiddenScheduleException() throws Exception {
        mockAuthentication(member1);

        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정에 접근 권한이 없어 예외 발생")
    void leaveAttendee_forbiddenScheduleException() throws Exception {
        mockAuthentication(member1);

        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }

    @Test
    @DisplayName("일정 내보내기")
    void removeAttendee() throws Exception{
        mockAuthentication(member1);

        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee2.getAttendeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        Optional<TravelAttendee> result = travelAttendeeRepository.findById(attendee2.getAttendeeId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정 데이터 존재하지 않아 예외 발생")
    void removeAttendee_scheduleDataNotFoundException() throws Exception {
        mockAuthentication(member1);

        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", 0L, attendee2.getAttendeeId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 내보내기 시 작성자 요청이 아니여서 예외 발생")
    void removeAttendee_forbiddenAttendeeException() throws Exception{
        mockAuthentication(member2);

        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee2.getAttendeeId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage()));
    }

    @Test
    @DisplayName("일정 내보내기 시 참석자를 찾을 수 없어 예외 발생")
    void removeAttendee_DataNotFoundException() throws Exception{
        mockAuthentication(member1);

        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
}

    @Test
    @DisplayName("일정 내보내기 시 작성자 내보내기 시도로 예외 발생")
    void removeAttendee_ForbiddenRemoveAuthorException() throws Exception{
        mockAuthentication(member1);

        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee1.getAttendeeId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));

    }
}
