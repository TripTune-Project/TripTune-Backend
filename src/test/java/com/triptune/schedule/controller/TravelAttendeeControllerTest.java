package com.triptune.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public class TravelAttendeeControllerTest extends ScheduleTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;


    private TravelSchedule schedule1;
    private TravelSchedule schedule2;

    private Member member1;
    private Member member2;
    private Member member3;


    @BeforeEach
    void setUp(){
        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage("member1Image"));
        member1 = memberRepository.save(createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage("member2Image"));
        member2 = memberRepository.save(createNativeTypeMember("member2@email.com", profileImage2));

        ProfileImage profileImage3 = profileImageRepository.save(createProfileImage("member3Image"));
        member3 = memberRepository.save(createNativeTypeMember("member3@email.com", profileImage3));

        schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
    }

    @Test
    @DisplayName("일정 참석자 조회")
    void getAttendees() throws Exception {
        // given
        TravelAttendee author = travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        TravelAttendee guest1 = travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        TravelAttendee guest2 = travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member3, AttendeePermission.ALL));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data[0].profileUrl").value(member1.getProfileImage().getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[0].permission").value(author.getPermission().name()))
                .andExpect(jsonPath("$.data[1].nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data[1].profileUrl").value(member2.getProfileImage().getS3ObjectUrl()))
                .andExpect(jsonPath("$.data[1].permission").value(guest1.getPermission().name()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getAttendees_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", 1000L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 조회 시 접근 권한이 없어 예외 발생")
    void getAttendees_forbiddenAccess() throws Exception {
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member3, AttendeePermission.ALL));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가")
    void createAttendee() throws Exception {
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member3, AttendeePermission.ALL));

        AttendeeRequest request = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 일정 참석자 5명 넘어 예외 발생")
    void createAttendee_overFiveAttendees() throws Exception {
        // given
        ProfileImage profileImage4 = profileImageRepository.save(createProfileImage("test4"));
        Member member4 = memberRepository.save(createNativeTypeMember("member4@email.com", profileImage4));
        ProfileImage profileImage5 = profileImageRepository.save(createProfileImage("test5"));
        Member member5 = memberRepository.save(createNativeTypeMember("member5@email.com", profileImage5));

        travelAttendeeRepository.saveAll(List.of(
                createAuthorTravelAttendee(schedule1, member1),
                createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ),
                createGuestTravelAttendee(schedule1, member3, AttendeePermission.CHAT),
                createGuestTravelAttendee(schedule1, member4, AttendeePermission.READ),
                createGuestTravelAttendee(schedule1, member5, AttendeePermission.ALL)
        ));

        AttendeeRequest request = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.OVER_ATTENDEE_NUMBER.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 요청자가 작성자가 아니여서 예외 발생")
    void createAttendee_forbiddenNotAuthor() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        AttendeeRequest request = createAttendeeRequest(member3.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member2);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_SHARE_ATTENDEE.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 추가하려는 회원 데이터 존재하지 않아 예외 발생")
    void createAttendee_attendeeNotFound() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        AttendeeRequest request = createAttendeeRequest("notMember@email.com", AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일정 참석자 추가 시 초대자가 이미 참석자여서 예외 발생")
    void createAttendee_alreadyAttendee() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        AttendeeRequest request = createAttendeeRequest(member2.getEmail(), AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId())
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
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        TravelAttendee guest = travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), guest.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        TravelAttendee savedAttendee = travelAttendeeRepository.findById(guest.getAttendeeId()).get();
        assertThat(savedAttendee.getPermission()).isEqualTo(AttendeePermission.CHAT);
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 권한 null 값으로 예외 발생")
    void updateAttendeePermission_invalidNullEmail() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        TravelAttendee guest = travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        AttendeePermissionRequest request = createAttendeePermissionRequest(null);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), guest.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("참석자 권한은 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 일정이 존재하지 않아 예외 발생")
    void updateAttendeePermission_scheduleNotFound() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        TravelAttendee guest = travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        1000L, guest.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 요청자가 작성자가 아니여서 예외 발생")
    void updateAttendeePermission_forbiddenNotAuthor() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        TravelAttendee guest = travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member3, AttendeePermission.ALL));

        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member2);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), guest.getAttendeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_UPDATE_ATTENDEE_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 참석자 정보 존재하지 않아 예외 발생")
    void updateAttendeePermission_attendeeNotFound() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 참석자 접근 권한 수정 시 작성자 접근 권한 수정 시도로 예외 발생")
    void updateAttendeePermission_forbiddenUpdateAuthorPermission() throws Exception{
        // given
        TravelAttendee author = travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        AttendeePermissionRequest request = createAttendeePermissionRequest(AttendeePermission.CHAT);
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), author.getAttendeeId())
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
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        TravelAttendee guest = travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        mockAuthentication(member2);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("200(성공)"));

        Optional<TravelAttendee> result = travelAttendeeRepository.findById(guest.getAttendeeId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정 데이터 존재하지 않아 예외 발생")
    void leaveAttendee_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member2);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", 1000L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 나가기 요청 시 요청자가 작성자여서 예외 발생")
    void leaveAttendee_forbiddenAuthor() throws Exception {
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule1.getScheduleId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정에 접근 권한이 없어 예외 발생")
    void leaveAttendee_forbiddenSchedule() throws Exception {
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees", schedule2.getScheduleId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }

    @Test
    @DisplayName("일정 내보내기")
    void removeAttendee() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        TravelAttendee guest = travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), guest.getAttendeeId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        Optional<TravelAttendee> result = travelAttendeeRepository.findById(guest.getAttendeeId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일정 나가기 요청 시 일정 데이터 존재하지 않아 예외 발생")
    void removeAttendee_scheduleNotFound() throws Exception {
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        TravelAttendee guest = travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        1000L, guest.getAttendeeId()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 내보내기 시 작성자 요청이 아니여서 예외 발생")
    void removeAttendee_forbiddenAttendee() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        TravelAttendee guest = travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        mockAuthentication(member2);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), guest.getAttendeeId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_REMOVE_ATTENDEE.getMessage()));
    }

    @Test
    @DisplayName("일정 내보내기 시 참석자를 찾을 수 없어 예외 발생")
    void removeAttendee_attendeeNotFound() throws Exception{
        // given
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), 1000L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
}

    @Test
    @DisplayName("일정 내보내기 시 작성자 내보내기 시도로 예외 발생")
    void removeAttendee_forbiddenRemoveAuthor() throws Exception{
        // given
        TravelAttendee author = travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member3, AttendeePermission.ALL));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/attendees/{attendeeId}",
                        schedule1.getScheduleId(), author.getAttendeeId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_LEAVE_AUTHOR.getMessage()));

    }
}
