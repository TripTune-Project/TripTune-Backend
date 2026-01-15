package com.triptune.schedule.repository;

import com.triptune.global.config.QuerydslConfig;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({QuerydslConfig.class})
@Transactional
@ActiveProfiles("mongo")
public class ChatMessageRepositoryTest extends ScheduleTest {

    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    private TravelSchedule schedule;

    @BeforeEach
    void setUp(){
        chatMessageRepository.deleteAll();
        schedule = travelScheduleRepository.save(createTravelSchedule("테스트"));
    }


    @Test
    @DisplayName("일정 id를 통해 채팅 목록 조회")
    void findAllByScheduleId(){
        // given
        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage("member1"));
        Member member1 = memberRepository.save(createNativeTypeMember("member1@email.com", profileImage1));
        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage("member2"));
        Member member2 = memberRepository.save(createNativeTypeMember("member2@email.com", profileImage2));
        ChatMessage message1 = chatMessageRepository.save(createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello1"));
        ChatMessage message2 = chatMessageRepository.save(createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello2"));
        ChatMessage message3 = chatMessageRepository.save(createChatMessage(schedule.getScheduleId(), member2.getMemberId(), "hello3"));

        // when
        List<ChatMessage> response = chatMessageRepository.findAllByScheduleId(schedule.getScheduleId());

        // then
        assertThat(response.size()).isEqualTo(3);
        assertThat(response.get(0).getMessage()).isEqualTo(message1.getMessage());
        assertThat(response.get(1).getMessage()).isEqualTo(message2.getMessage());
        assertThat(response.get(2).getMessage()).isEqualTo(message3.getMessage());
    }

    @Test
    @DisplayName("일정 id를 통해 채팅 목록 조회")
    void findAllByScheduleId_isEmpty(){
        // given, when
        List<ChatMessage> response = chatMessageRepository.findAllByScheduleId(schedule.getScheduleId());

        // then
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("일정 id를 통해 채팅 삭제")
    void deleteAllByScheduleId(){
        // given
        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage("member1"));
        Member member1 = memberRepository.save(createNativeTypeMember("member1@email.com", profileImage1));
        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage("member2"));
        Member member2 = memberRepository.save(createNativeTypeMember("member2@email.com", profileImage2));
        chatMessageRepository.save(createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello1"));
        chatMessageRepository.save(createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello2"));
        chatMessageRepository.save(createChatMessage(schedule.getScheduleId(), member2.getMemberId(), "hello3"));


        // when
        chatMessageRepository.deleteAllByScheduleId(schedule.getScheduleId());

        // then
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByScheduleId(schedule.getScheduleId());
        assertThat(chatMessages).isEmpty();
    }


    @Test
    @DisplayName("일정 id를 통해 채팅 삭제 시 채팅 데이터 없는 경우")
    void deleteAllByScheduleId_emptyMessages(){
        // given, when
        chatMessageRepository.deleteAllByScheduleId(schedule.getScheduleId());

        // then
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByScheduleId(schedule.getScheduleId());
        assertThat(chatMessages).isEmpty();
    }

}