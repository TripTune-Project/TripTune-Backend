package com.triptune.domain.schedule.controller;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.member.repository.ProfileImageRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.entity.ChatMessage;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.ChatMessageRepository;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo
@ActiveProfiles("mongo")
public class ChatControllerTest extends ScheduleTest {
    private final WebApplicationContext wac;
    private final TravelScheduleRepository travelScheduleRepository;
    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final ChatMessageRepository chatMessageRepository;

    private MockMvc mockMvc;

    private TravelSchedule schedule;
    private Member member1;
    private Member member2;
    private Member member3;

    @Autowired
    public ChatControllerTest(WebApplicationContext wac, TravelScheduleRepository travelScheduleRepository, MemberRepository memberRepository, ProfileImageRepository profileImageRepository, TravelAttendeeRepository travelAttendeeRepository, ChatMessageRepository chatMessageRepository) {
        this.wac = wac;
        this.travelScheduleRepository = travelScheduleRepository;
        this.memberRepository = memberRepository;
        this.profileImageRepository = profileImageRepository;
        this.travelAttendeeRepository = travelAttendeeRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        chatMessageRepository.deleteAll();

        member1 = memberRepository.save(createMember(null, "member1"));
        member2 = memberRepository.save(createMember(null, "member2"));
        member3 = memberRepository.save(createMember(null, "member3"));
        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage(null, "member1Image"));
        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage(null, "member2Image"));
        ProfileImage profileImage3 = profileImageRepository.save(createProfileImage(null, "member3Image"));
        member1.setProfileImage(profileImage1);
        member2.setProfileImage(profileImage2);
        member3.setProfileImage(profileImage3);

        schedule = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member3, schedule, AttendeeRole.GUEST, AttendeePermission.CHAT));

        schedule.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2, attendee3)));

    }


    @Test
    @DisplayName("채팅 내용 조회")
    @WithMockUser(username = "member1")
    void getChatMessages() throws Exception {
        ChatMessage message1 = chatMessageRepository.save(createChatMessage("id1", schedule.getScheduleId(), member1, "hello1"));
        ChatMessage message2 = chatMessageRepository.save(createChatMessage("id2", schedule.getScheduleId(), member1, "hello2"));
        ChatMessage message3 = chatMessageRepository.save(createChatMessage("id3", schedule.getScheduleId(), member1, "hello3"));
        ChatMessage message4 = chatMessageRepository.save(createChatMessage("id4", schedule.getScheduleId(), member2, "hello4"));
        ChatMessage message5 = chatMessageRepository.save(createChatMessage("id5", schedule.getScheduleId(), member3, "hello5"));
        ChatMessage message6 = chatMessageRepository.save(createChatMessage("id6", schedule.getScheduleId(), member1, "hello6"));

        mockMvc.perform(get("/api/schedules/{scheduleId}/chats", schedule.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
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
    @WithMockUser(username = "member1")
    void getChatMessagesNoMessage() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/chats", schedule.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

}