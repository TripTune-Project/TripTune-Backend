package com.triptune.domain.schedule.controller;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.request.CreateAttendeeRequest;
import com.triptune.domain.schedule.dto.request.CreateScheduleRequest;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class AttendeeControllerTest extends ScheduleTest {
    private final WebApplicationContext wac;
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final MemberRepository memberRepository;

    private MockMvc mockMvc;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;

    private Member member2;
    private Member member3;

    @Autowired
    public AttendeeControllerTest(WebApplicationContext wac, TravelScheduleRepository travelScheduleRepository, TravelAttendeeRepository travelAttendeeRepository, MemberRepository memberRepository) {
        this.wac = wac;
        this.travelScheduleRepository = travelScheduleRepository;
        this.travelAttendeeRepository = travelAttendeeRepository;
        this.memberRepository = memberRepository;
    }


    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        Member member1 = memberRepository.save(createMember(null, "member1"));
        member2 = memberRepository.save(createMember(null, "member2"));
        member3 = memberRepository.save(createMember(null, "member3"));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가")
    @WithMockUser(username = "member1")
    void createAttendee() throws Exception {
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getUserId(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createAttendeeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1")
    void createAttendeeNotFoundSchedule_dataNotFoundException() throws Exception{
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getUserId(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createAttendeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 요청자가 작성자가 아니여서 예외 발생")
    @WithMockUser(username = "member2")
    void createAttendeeNotAuthor_forbiddenScheduleException() throws Exception{
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member3.getUserId(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createAttendeeRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 초대자 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1")
    void createAttendeeNotMember_dataNotFoundException() throws Exception{
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest("notMember", AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createAttendeeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("createAttendee(): 일정 참석자 추가 시 초대자가 이미 참석자여서 예외 발생")
    @WithMockUser(username = "member1")
    void createAttendee_alreadyAttendeeException() throws Exception{
        CreateAttendeeRequest createAttendeeRequest = createAttendeeRequest(member2.getUserId(), AttendeePermission.CHAT);
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createAttendeeRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("removeAttendee(): 일정 참석자 제거")
    @WithMockUser(username = "member2")
    void removeAttendee() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("200(성공)"));
    }


    @Test
    @DisplayName("removeAttendee(): 일정 참석자 제거 시 사용자가 작성자여서 예외 발생")
    @WithMockUser(username = "member1")
    void removeAttendeeIsAuthor_forbiddenScheduleException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage()));
    }

    @Test
    @DisplayName("removeAttendee(): 일정 참석자 제거 시 사용자가 일정에 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member1")
    void removeAttendeeNotAttendee_forbiddenScheduleException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }
}
