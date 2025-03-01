package com.triptune.schedule.repository;

import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mongo")
public class ChatMessageRepositoryTest extends ScheduleTest {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final TravelScheduleRepository travelScheduleRepository;

    private TravelSchedule schedule;

    @Autowired
    public ChatMessageRepositoryTest(ChatMessageRepository chatMessageRepository, MemberRepository memberRepository, TravelScheduleRepository travelScheduleRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.memberRepository = memberRepository;
        this.travelScheduleRepository = travelScheduleRepository;
    }

    @BeforeEach
    void setUp(){
        chatMessageRepository.deleteAll();
        schedule = travelScheduleRepository.save(createTravelSchedule(null, "테스트"));
    }


    @Test
    @DisplayName("일정 id를 통해 채팅 목록 조회")
    void findAllByScheduleId(){
        // given
        Member member1 = memberRepository.save(createMember(null, "member1"));
        Member member2 = memberRepository.save(createMember(null, "member2"));
        ChatMessage message1 = chatMessageRepository.save(createChatMessage("chat1", schedule.getScheduleId(), member1, "hello1"));
        ChatMessage message2 = chatMessageRepository.save(createChatMessage("chat2", schedule.getScheduleId(), member1, "hello2"));
        ChatMessage message3 = chatMessageRepository.save(createChatMessage("chat3", schedule.getScheduleId(), member2, "hello3"));

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
        Member member1 = memberRepository.save(createMember(null, "member1"));
        Member member2 = memberRepository.save(createMember(null, "member2"));
        chatMessageRepository.save(createChatMessage("chat1", schedule.getScheduleId(), member1, "hello1"));
        chatMessageRepository.save(createChatMessage("chat2", schedule.getScheduleId(), member1, "hello2"));
        chatMessageRepository.save(createChatMessage("chat3", schedule.getScheduleId(), member2, "hello3"));


        // when
        chatMessageRepository.deleteAllByScheduleId(schedule.getScheduleId());

        // then
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByScheduleId(schedule.getScheduleId());
        assertThat(chatMessages).isEmpty();
    }


    @Test
    @DisplayName("일정 id를 통해 채팅 삭제 시 채팅 데이터 없는 경우")
    void deleteAllByScheduleId_NoData(){
        // given, when
        chatMessageRepository.deleteAllByScheduleId(schedule.getScheduleId());

        // then
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByScheduleId(schedule.getScheduleId());
        assertThat(chatMessages).isEmpty();
    }

}