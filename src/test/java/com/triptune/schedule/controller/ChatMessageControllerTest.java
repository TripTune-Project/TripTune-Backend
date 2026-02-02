package com.triptune.schedule.controller;

import com.triptune.global.security.SecurityTestUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.fixture.ChatMessageFixture;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("mongo")
public class ChatMessageControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;

    private TravelSchedule schedule;
    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp(){
        chatMessageRepository.deleteAll();

        ProfileImage profileImage1 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member1Image"));
        member1 = memberRepository.save(MemberFixture.createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member2Image"));
        member2 = memberRepository.save(MemberFixture.createNativeTypeMember("member2@email.com", profileImage2));

        ProfileImage profileImage3 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member3Image"));
        member3 = memberRepository.save(MemberFixture.createNativeTypeMember("member3@email.com", profileImage3));

        schedule = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule, member3, AttendeePermission.CHAT));

    }

    @Test
    @DisplayName("채팅 내용 조회")
    void getChatMessages() throws Exception {
        // given
        ChatMessage message1 = chatMessageRepository.save(ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello1"));
        ChatMessage message2 = chatMessageRepository.save(ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello2"));
        ChatMessage message3 = chatMessageRepository.save(ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello3"));
        ChatMessage message4 = chatMessageRepository.save(ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member2.getMemberId(), "hello4"));
        ChatMessage message5 = chatMessageRepository.save(ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member3.getMemberId(), "hello5"));
        ChatMessage message6 = chatMessageRepository.save(ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello6"));

        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/chats", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(6))
                .andExpect(jsonPath("$.data.content[0].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[0].message").value(message1.getMessage()))
                .andExpect(jsonPath("$.data.content[1].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].message").value(message2.getMessage()))
                .andExpect(jsonPath("$.data.content[2].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[2].message").value(message3.getMessage()))
                .andExpect(jsonPath("$.data.content[3].nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[3].message").value(message4.getMessage()))
                .andExpect(jsonPath("$.data.content[4].nickname").value(member3.getNickname()))
                .andExpect(jsonPath("$.data.content[4].message").value(message5.getMessage()))
                .andExpect(jsonPath("$.data.content[5].nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[5].message").value(message6.getMessage()));
    }

    @Test
    @DisplayName("채팅 내용 조회 시 데이터가 없는 경우")
    void getChatMessagesNoMessage() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/chats", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

}