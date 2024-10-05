package com.triptune.domain.schedule.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.repository.AttendeeRepository;
import com.triptune.domain.schedule.repository.ScheduleRepository;
import com.triptune.domain.schedule.service.ScheduleService;
import com.triptune.global.util.HttpRequestEndpointChecker;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class ScheduleApiControllerTests {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @MockBean
    private AttendeeRepository attendeeRepository;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private HttpRequestEndpointChecker httpRequestEndpointChecker;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("일정 만들기 성공")
    @WithMockUser(username = "test")
    void createSchedule_success() throws Exception{
        // given
        CreateScheduleRequest request = createScheduleRequest();
        TravelSchedule savedTravelSchedule = createTravelSchedule();

        Member mockMember = new Member();
        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.of(mockMember));

        when(scheduleRepository.save(any(TravelSchedule.class))).thenReturn(savedTravelSchedule);
        when(attendeeRepository.save(any(TravelAttendee.class))).thenReturn(new TravelAttendee());

        // when, then
        mockMvc.perform(post("/api/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    @DisplayName("일정 만들기 실패: 필요 입력값이 다 안들어온 경우")
    @WithMockUser(username = "test")
    void createSchedule_MethodArgumentNotValidException() throws Exception{
        // given
        CreateScheduleRequest request = createScheduleRequest();
        request.setStartDate(null);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));;
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

    private String toJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

}
