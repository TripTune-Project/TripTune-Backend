package com.triptune.schedule.controller;

import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.SuccessCode;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.schedule.dto.request.AttendeeRequest;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeePermission;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

        member1 = memberRepository.save(createMember(null, "member1", profileImage1));
        member2 = memberRepository.save(createMember(null, "member2", profileImage2));
        member3 = memberRepository.save(createMember(null, "member3", profileImage3));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));

        attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));

    }

    @Test
    @DisplayName("일정 참석자 조회")
    @WithMockUser(username = "member1@email.com")
    void getAttendees() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data[0].profileUrl").value(member1.getProfileImage().getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[0].permission").value(attendee1.getPermission().name()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void getAttendeesNoScheduleData_notFoundException() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void getAttendeesNotAccess_forbiddenScheduleException() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가")
    @WithMockUser(username = "member1@email.com")
    void createAttendee() throws Exception {
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void createAttendeeNotFoundSchedule_dataNotFoundException() throws Exception{
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 일정 참석자 5명 넘어 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void createAttendeeOver5_conflictAttendeeException() throws Exception {
        Member member4 = memberRepository.save(createMember(null, "member4"));
        Member member5 = memberRepository.save(createMember(null, "member5"));
        travelAttendeeRepository.save(createTravelAttendee(0L, member3, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL));
        travelAttendeeRepository.save(createTravelAttendee(0L, member4, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL));
        travelAttendeeRepository.save(createTravelAttendee(0L, member5, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL));


        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.OVER_ATTENDEE_NUMBER.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 요청자가 작성자가 아니여서 예외 발생")
    @WithMockUser(username = "member2@email.com")
    void createAttendeeNotAuthor_forbiddenScheduleException() throws Exception{
        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 초대자 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void createAttendeeNotMember_dataNotFoundException() throws Exception{
        AttendeeRequest attendeeRequest = createAttendeeRequest("notMember@email.com", AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 초대자가 이미 참석자여서 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void createAttendee_alreadyAttendeeException() throws Exception{
        AttendeeRequest attendeeRequest = createAttendeeRequest(member2.getEmail(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정")
    @WithMockUser(username = "member1@email.com")
    void updateAttendeePermission() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 일정이 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void updateAttendeePermission_scheduleDataNotFoundException() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", 0L, attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 요청자가 작성자가 아니여서 예외 발생")
    @WithMockUser(username = "member2@email.com")
    void updateAttendeePermission_forbiddenScheduleException() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 참석자 정보 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void updateAttendeePermission_attendeeDataNotFoundException() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee3.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 작성자 접근 권한 수정 시도로 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void updateAttendeePermission_forbiddenUpdateAuthorPermissionException() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee1.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기")
    @WithMockUser(username = "member2@email.com")
    void leaveAttendee() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("200(성공)"));

        Optional<TravelAttendee> result = travelAttendeeRepository.findById(attendee2.getAttendeeId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member2@email.com")
    void leaveAttendee_scheduleDataNotFoundException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 나가기 요청 시 사용자가 작성자여서 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void leaveAttendeeIsAuthor_forbiddenScheduleException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기 요청 시 사용자가 일정에 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void leaveAttendee_forbiddenScheduleException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }

    @Test
    @DisplayName("일정 내보내기")
    @WithMockUser(username = "member1@email.com")
    void removeAttendee() throws Exception{
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee2.getAttendeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        Optional<TravelAttendee> result = travelAttendeeRepository.findById(attendee2.getAttendeeId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void removeAttendee_scheduleDataNotFoundException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", 0L, attendee2.getAttendeeId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 내보내기 시 작성자 요청이 아니여서 예외 발생")
    @WithMockUser(username = "member2@email.com")
    void removeAttendee_forbiddenAttendeeException() throws Exception{
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee2.getAttendeeId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage()));
    }

    @Test
    @DisplayName("일정 내보내기 시 참석자를 찾을 수 없어 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void removeAttendee_DataNotFoundException() throws Exception{
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
}

    @Test
    @DisplayName("일정 내보내기 시 작성자 내보내기 시도로 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void removeAttendee_ForbiddenRemoveAuthorException() throws Exception{
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee1.getAttendeeId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));

    }
}
