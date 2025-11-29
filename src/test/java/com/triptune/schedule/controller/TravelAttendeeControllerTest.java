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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("h2")
public class TravelAttendeeControllerTest extends ScheduleTest {
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
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data[0].profileUrl").value(member1.getProfileImage().getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[0].permission").value(attendee1.getPermission().name()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getAttendees_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 접근 권한이 없어 예외 발생")
    void getAttendees_forbiddenAccess() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가")
    void createAttendee() throws Exception {
        // given
        AttendeeRequest request = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }


    @ParameterizedTest
    @DisplayName("일정 참석자 추가 시 이메일 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void createAttendee_invalidNotBlankEmail(String input) throws Exception {
        // given
        AttendeeRequest request = createAttendeeRequest(input, AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 참석자 추가 시 이메일 null 값이 들어와 예외 발생")
    void createAttendee_invalidNullEmail() throws Exception {
        // given
        AttendeeRequest request = createAttendeeRequest(null, AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("일정 참석자 추가 시 이메일 형식에 맞지 않아 예외 발생")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void createAttendee_invalidEmail(String input) throws Exception {
        // given
        AttendeeRequest request = createAttendeeRequest(input, AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }

    @Test
    @DisplayName("일정 참석자 추가 시 권한 null 값이 들어와 예외 발생")
    void createAttendee_invalidNullPermission() throws Exception {
        // given
        AttendeeRequest request = createAttendeeRequest(member3.getEmail(), null);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("참석자 권한은 필수 입력 값입니다.")));
    }


    @Test
    @DisplayName("일정 참석자 추가 시 일정 데이터 존재하지 않아 예외 발생")
    void createAttendee_scheduleNotFound() throws Exception{
        // given
        AttendeeRequest request = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", 1000L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 일정 참석자 5명 넘어 예외 발생")
    void createAttendee_overFiveAttendees() throws Exception {
        // given
        Member member4 = memberRepository.save(createMember(null, "member4@email.com"));
        Member member5 = memberRepository.save(createMember(null, "member5@email.com"));

        travelAttendeeRepository.saveAll(List.of(
                createTravelAttendee(null, member3, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL),
                createTravelAttendee(null, member4, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL),
                createTravelAttendee(null, member5, schedule1, AttendeeRole.GUEST, AttendeePermission.ALL)
        ));

        AttendeeRequest request = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.OVER_ATTENDEE_NUMBER.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 요청자가 작성자가 아니여서 예외 발생")
    void createAttendee_forbiddenNotAuthor() throws Exception{
        // given
        AttendeeRequest request = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member2);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 추가하려는 회원 데이터 존재하지 않아 예외 발생")
    void createAttendee_attendeeNotFound() throws Exception{
        // given
        AttendeeRequest request = createAttendeeRequest("notMember@email.com", AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 초대자가 이미 참석자여서 예외 발생")
    void createAttendee_alreadyAttendee() throws Exception{
        // given
        AttendeeRequest request = createAttendeeRequest(member2.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정")
    void updateAttendeePermission() throws Exception{
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 권한 null 값으로 예외 발생")
    void updateAttendeePermission_invalidNullEmail() throws Exception{
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(null);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("참석자 권한은 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 일정이 존재하지 않아 예외 발생")
    void updateAttendeePermission_scheduleNotFound() throws Exception{
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        0L, attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 요청자가 작성자가 아니여서 예외 발생")
    void updateAttendeePermission_forbiddenNotAuthor() throws Exception{
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member2);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), attendee2.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 참석자 정보 존재하지 않아 예외 발생")
    void updateAttendeePermission_attendeeNotFound() throws Exception{
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), attendee3.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 작성자 접근 권한 수정 시도로 예외 발생")
    void updateAttendeePermission_forbiddenUpdateAuthorPermission() throws Exception{
        // given
        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), attendee1.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_AUTHOR_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기")
    void leaveAttendee() throws Exception {
        // given
        mockAuthentication(member2);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("200(성공)"));

        Optional<TravelAttendee> result = travelAttendeeRepository.findById(attendee2.getAttendeeId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정 데이터 존재하지 않아 예외 발생")
    void leaveAttendee_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member2);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 나가기 요청 시 요청자가 작성자여서 예외 발생")
    void leaveAttendee_forbiddenAuthor() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정에 접근 권한이 없어 예외 발생")
    void leaveAttendee_forbiddenSchedule() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }

    @Test
    @DisplayName("일정 내보내기")
    void removeAttendee() throws Exception{
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), attendee2.getAttendeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        Optional<TravelAttendee> result = travelAttendeeRepository.findById(attendee2.getAttendeeId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정 데이터 존재하지 않아 예외 발생")
    void removeAttendee_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        0L, attendee2.getAttendeeId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 내보내기 시 작성자 요청이 아니여서 예외 발생")
    void removeAttendee_forbiddenAttendee() throws Exception{
        // given
        mockAuthentication(member2);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), attendee2.getAttendeeId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage()));
    }

    @Test
    @DisplayName("일정 내보내기 시 참석자를 찾을 수 없어 예외 발생")
    void removeAttendee_attendeeNotFound() throws Exception{
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
}

    @Test
    @DisplayName("일정 내보내기 시 작성자 내보내기 시도로 예외 발생")
    void removeAttendee_forbiddenRemoveAuthor() throws Exception{
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), attendee1.getAttendeeId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));

    }
}
