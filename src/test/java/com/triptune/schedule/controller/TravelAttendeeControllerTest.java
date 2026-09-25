package com.triptune.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.common.fixture.S3Fixture;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.schedule.dto.request.AttendeeRequest;
import com.triptune.schedule.dto.response.AttendeeResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.exception.ConflictAttendeeException;
import com.triptune.schedule.exception.ForbiddenAttendeeException;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.service.TravelAttendeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.triptune.schedule.enums.AttendeePermission.CHAT;
import static com.triptune.schedule.enums.AttendeePermission.READ;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TravelAttendeeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TravelAttendeeControllerTest {

    private static final Long SCHEDULE_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private TravelAttendeeService travelAttendeeService;

    private Member member1;
    private Member member2;

    private String member1ProfileUrl;
    private String member2ProfileUrl;


    @BeforeEach
    void setUp(){
        ProfileImage profileImage1 = ProfileImageFixture.createProfileImage("member1Image");
        member1 = MemberFixture.createNativeTypeMemberWithId(1L, "member1@email.com", profileImage1);
        member1ProfileUrl = S3Fixture.createS3ObjectUrl(profileImage1.getS3ObjectKey());

        ProfileImage profileImage2 = ProfileImageFixture.createProfileImage("member2Image");
        member2 = MemberFixture.createNativeTypeMemberWithId(2L, "member2@email.com", profileImage2);
        member2ProfileUrl = S3Fixture.createS3ObjectUrl(profileImage2.getS3ObjectKey());
    }

    @Test
    @DisplayName("일정 참석자 조회")
    void getAttendees() throws Exception {
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "테스트1");
        TravelAttendee author = TravelAttendeeFixture.createAuthorAttendee(schedule, member1);
        TravelAttendee guest1 = TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ);

        List<AttendeeResponse> response = List.of(
            TravelAttendeeFixture.createAttendeeResponse(author, member1ProfileUrl),
            TravelAttendeeFixture.createAttendeeResponse(guest1, member2ProfileUrl)
        );

        SecurityTestUtils.mockAuthentication(member1);

        given(travelAttendeeService.getAttendeesByScheduleId(anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule.getScheduleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data[0].profileUrl").value(member1ProfileUrl))
                .andExpect(jsonPath("$.data[0].permission").value(author.getPermission().name()))
                .andExpect(jsonPath("$.data[1].nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data[1].profileUrl").value(member2ProfileUrl))
                .andExpect(jsonPath("$.data[1].permission").value(guest1.getPermission().name()));

    }


    @Test
    @DisplayName("일정 참석자 추가")
    void createAttendee() throws Exception {
        // given
        AttendeeRequest request = TravelAttendeeFixture.createAttendeeRequest(member2.getEmail(), CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }


    @ParameterizedTest
    @DisplayName("일정 참석자 추가 시 이메일 빈 값으로 400 반환")
    @ValueSource(strings = {"", " "})
    void createAttendee_invalidNotBlankEmail(String input) throws Exception {
        // given
        AttendeeRequest request = TravelAttendeeFixture.createAttendeeRequest(input, CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 참석자 추가 시 이메일 null 값이 들어와 400 반환")
    void createAttendee_invalidNullEmail() throws Exception {
        // given
        AttendeeRequest request = TravelAttendeeFixture.createAttendeeRequest(null, CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("일정 참석자 추가 시 이메일 형식에 맞지 않아 400 반환")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void createAttendee_invalidEmail(String input) throws Exception {
        // given
        AttendeeRequest request = TravelAttendeeFixture.createAttendeeRequest(input, CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }

    @Test
    @DisplayName("일정 참석자 추가 시 권한 null 값이 들어와 400 반환")
    void createAttendee_invalidNullPermission() throws Exception {
        // given
        AttendeeRequest request = TravelAttendeeFixture.createAttendeeRequest(member2.getEmail(), null);
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("참석자 권한은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 참석자 추가 시 일정 참석자 5명 넘어 409 반환")
    void createAttendee_overFiveAttendees() throws Exception {
        // given
        AttendeeRequest request = TravelAttendeeFixture.createAttendeeRequest(member2.getEmail(), CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        willThrow(new ConflictAttendeeException(ErrorCode.OVER_ATTENDEE_NUMBER))
                .given(travelAttendeeService).createAttendee(anyLong(), anyLong(), any(AttendeeRequest.class));

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.OVER_ATTENDEE_NUMBER.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 요청자가 작성자가 아니여서 403 반환")
    void createAttendee_forbiddenNotAuthor() throws Exception{
        // given
        AttendeeRequest request = TravelAttendeeFixture.createAttendeeRequest(member1.getEmail(), CHAT);
        SecurityTestUtils.mockAuthentication(member2);

        willThrow(new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_SHARE_ATTENDEE))
                .given(travelAttendeeService).createAttendee(anyLong(), anyLong(), any(AttendeeRequest.class));

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 추가하려는 회원 데이터 존재하지 않아 404 반환")
    void createAttendee_attendeeNotFound() throws Exception{
        // given
        AttendeeRequest request = TravelAttendeeFixture.createAttendeeRequest("notMember@email.com", CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        willThrow(new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND))
                .given(travelAttendeeService).createAttendee(anyLong(), anyLong(), any(AttendeeRequest.class));

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 초대자가 이미 참석자여서 409 반환")
    void createAttendee_alreadyAttendee() throws Exception{
        // given
        AttendeeRequest request = TravelAttendeeFixture.createAttendeeRequest(member2.getEmail(), CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        willThrow(new ConflictAttendeeException(ErrorCode.ALREADY_ATTENDEE))
                .given(travelAttendeeService).createAttendee(anyLong(), anyLong(), any(AttendeeRequest.class));


        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정")
    void updateAttendeePermission() throws Exception{
        // given
        AttendeePermissionRequest request = TravelAttendeeFixture.createAttendeePermissionRequest(CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        SCHEDULE_ID, member2.getMemberId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 권한 null 값으로 400 반환")
    void updateAttendeePermission_invalidNullPermission() throws Exception{
        // given
        AttendeePermissionRequest request = TravelAttendeeFixture.createAttendeePermissionRequest(null);
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        SCHEDULE_ID, member2.getMemberId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("참석자 권한은 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 요청자가 작성자가 아니여서 403 반환")
    void updateAttendeePermission_forbiddenNotAuthor() throws Exception{
        // given
        AttendeePermissionRequest request = TravelAttendeeFixture.createAttendeePermissionRequest(CHAT);
        SecurityTestUtils.mockAuthentication(member2);

        willThrow(new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION))
                .given(travelAttendeeService).updateAttendeePermission(any(AttendeePermissionRequest.class), anyLong(), anyLong(), anyLong());

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        SCHEDULE_ID, member1.getMemberId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 참석자 정보 존재하지 않아 404 반환")
    void updateAttendeePermission_attendeeNotFound() throws Exception{
        // given
        AttendeePermissionRequest request = TravelAttendeeFixture.createAttendeePermissionRequest(CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        willThrow(new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND))
                .given(travelAttendeeService).updateAttendeePermission(any(AttendeePermissionRequest.class), anyLong(), anyLong(), anyLong());


        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        SCHEDULE_ID, 1000L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 작성자 접근 권한 수정 시도로 403 반환")
    void updateAttendeePermission_forbiddenUpdateAuthorPermission() throws Exception{
        // given
        AttendeePermissionRequest request = TravelAttendeeFixture.createAttendeePermissionRequest(CHAT);
        SecurityTestUtils.mockAuthentication(member1);

        willThrow(new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION))
                .given(travelAttendeeService).updateAttendeePermission(
                        any(AttendeePermissionRequest.class),
                        anyLong(),
                        anyLong(),
                        anyLong()
                );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        SCHEDULE_ID, member1.getMemberId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기")
    void leaveAttendee() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member2);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("200(성공)"));

    }

    @Test
    @DisplayName("일정 나가기 요청 시 요청자가 작성자여서 403 반환")
    void leaveAttendee_forbiddenAuthor() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        willThrow(new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_LEAVE_AUTHOR))
                .given(travelAttendeeService).leaveAttendee(anyLong(), anyLong());

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", SCHEDULE_ID))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));
    }


    @Test
    @DisplayName("일정 내보내기")
    void removeAttendee() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        SCHEDULE_ID, member2.getMemberId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }


    @Test
    @DisplayName("일정 내보내기 시 작성자 요청이 아니여서 403 반환")
    void removeAttendee_forbiddenAttendee() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member2);

        willThrow(new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE))
                .given(travelAttendeeService).removeAttendee(anyLong(), anyLong(), anyLong());

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        SCHEDULE_ID, member1.getMemberId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage()));
    }

    @Test
    @DisplayName("일정 내보내기 시 참석자를 찾을 수 없어 404 반환")
    void removeAttendee_attendeeNotFound() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);

        willThrow(new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND))
                .given(travelAttendeeService).removeAttendee(anyLong(), anyLong(), anyLong());

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        SCHEDULE_ID, 1000L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
}

    @Test
    @DisplayName("일정 내보내기 시 작성자 내보내기 시도로 403 반환")
    void removeAttendee_forbiddenRemoveAuthor() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);

        willThrow(new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_LEAVE_AUTHOR))
                .given(travelAttendeeService).removeAttendee(anyLong(), anyLong(), anyLong());


        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        SCHEDULE_ID, member1.getMemberId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));

    }
}
