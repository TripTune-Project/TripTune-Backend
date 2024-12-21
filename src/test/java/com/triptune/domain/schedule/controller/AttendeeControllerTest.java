package com.triptune.domain.schedule.controller;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.member.repository.ProfileImageRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.domain.schedule.dto.request.AttendeeRequest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.SuccessCode;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class AttendeeControllerTest extends ScheduleTest {
    private final WebApplicationContext wac;
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;

    private MockMvc mockMvc;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;

    private Member member1;
    private Member member2;
    private Member member3;

    TravelAttendee attendee1;
    TravelAttendee attendee2;
    TravelAttendee attendee3;

    @Autowired
    public AttendeeControllerTest(WebApplicationContext wac, TravelScheduleRepository travelScheduleRepository, TravelAttendeeRepository travelAttendeeRepository, MemberRepository memberRepository, ProfileImageRepository profileImageRepository) {
        this.wac = wac;
        this.travelScheduleRepository = travelScheduleRepository;
        this.travelAttendeeRepository = travelAttendeeRepository;
        this.memberRepository = memberRepository;
        this.profileImageRepository = profileImageRepository;
    }


    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        member1 = memberRepository.save(createMember(null, "member1"));
        member2 = memberRepository.save(createMember(null, "member2"));
        member3 = memberRepository.save(createMember(null, "member3"));
        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage(null, "member1Image"));
        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage(null, "member2Image"));
        ProfileImage profileImage3 = profileImageRepository.save(createProfileImage(null, "member3Image"));
        member1.setProfileImage(profileImage1);
        member2.setProfileImage(profileImage2);
        member3.setProfileImage(profileImage3);

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));

        attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
    }

    @Test
    @DisplayName("일정 참석자 조회")
    @WithMockUser(username = "member1")
    void getAttendees() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data[0].profileUrl").value(member1.getProfileImage().getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[0].permission").value(attendee1.getPermission().name()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1")
    void getAttendeesNoScheduleData_notFoundException() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member1")
    void getAttendeesNotAccess_forbiddenScheduleException() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가")
    @WithMockUser(username = "member1")
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
    @WithMockUser(username = "member1")
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
    @WithMockUser(username = "member1")
    void createAttendeeOver5_conflictAttendeeException() throws Exception {
        Member member4 = memberRepository.save(createMember(null, "member4"));
        Member member5 = memberRepository.save(createMember(null, "member5"));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member3, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member4, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL));
        TravelAttendee attendee5 = travelAttendeeRepository.save(createTravelAttendee(0L, member5, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL));
        schedule1.getTravelAttendeeList().add(attendee3);
        schedule1.getTravelAttendeeList().add(attendee4);
        schedule1.getTravelAttendeeList().add(attendee5);

        AttendeeRequest attendeeRequest = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.OVER_ATTENDEE_NUMBER.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 요청자가 작성자가 아니여서 예외 발생")
    @WithMockUser(username = "member2")
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
    @WithMockUser(username = "member1")
    void createAttendeeNotMember_dataNotFoundException() throws Exception{
        AttendeeRequest attendeeRequest = createAttendeeRequest("notMember@email.com", AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(attendeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 초대자가 이미 참석자여서 예외 발생")
    @WithMockUser(username = "member1")
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
    @WithMockUser(username = "member1")
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
    @WithMockUser(username = "member1")
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
    @WithMockUser(username = "member2")
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
    @WithMockUser(username = "member1")
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
    @WithMockUser(username = "member1")
    void updateAttendeePermission_forbiddenUpdateAuthorPermissionException() throws Exception{
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}", schedule1.getScheduleId(), attendee1.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_ATTENDEE_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기")
    @WithMockUser(username = "member2")
    void leaveScheduleAsGuest() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("200(성공)"));
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member2")
    void leaveScheduleAsGuest_scheduleDataNotFoundException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 나가기 요청 시 사용자가 작성자여서 예외 발생")
    @WithMockUser(username = "member1")
    void leaveScheduleAsGuestIsAuthor_forbiddenScheduleException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_REMOVE_AUTHOR_ATTENDEE.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기 요청 시 사용자가 일정에 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member1")
    void leaveScheduleAsGuest_forbiddenScheduleException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }
}
