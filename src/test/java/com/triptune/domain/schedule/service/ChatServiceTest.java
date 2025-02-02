package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.profile.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.request.ChatMessageRequest;
import com.triptune.domain.schedule.dto.response.ChatResponse;
import com.triptune.domain.schedule.entity.ChatMessage;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.DataNotFoundChatException;
import com.triptune.domain.schedule.exception.ForbiddenChatException;
import com.triptune.domain.schedule.repository.ChatMessageRepository;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest extends ScheduleTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TravelAttendeeRepository travelAttendeeRepository;

    @Mock
    private TravelScheduleRepository travelScheduleRepository;


    private TravelSchedule schedule;
    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp(){
        schedule = createTravelSchedule(1L, "테스트");

        ProfileImage profileImage1 = createProfileImage(1L, "member1Image", member1);
        ProfileImage profileImage2 = createProfileImage(2L, "member2Image", member2);
        ProfileImage profileImage3 = createProfileImage(3L, "member3Image", member3);

        member1 = createMember(1L, "member1", profileImage1);
        member2 = createMember(2L, "member2", profileImage2);
        member3 = createMember(3L, "member3", profileImage3);
    }

    @Test
    @DisplayName("채팅 메시지 조회")
    void getChatMessages(){
        // given
        Pageable pageable = PageUtil.chatPageable(1);

        ChatMessage message1 = createChatMessage("id1", schedule.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("id2", schedule.getScheduleId(), member1, "hello2");
        ChatMessage message3 = createChatMessage("id3", schedule.getScheduleId(), member1, "hello3");
        ChatMessage message4 = createChatMessage("id4", schedule.getScheduleId(), member2, "hello4");
        ChatMessage message5 = createChatMessage("id5", schedule.getScheduleId(), member3, "hello5");
        ChatMessage message6 = createChatMessage("id6", schedule.getScheduleId(), member1, "hello6");
        List<ChatMessage> messageList = new ArrayList<>(List.of(message1, message2, message3, message4, message5, message6));
        Page<ChatMessage> chatPage = PageUtil.createPage(messageList, pageable, messageList.size());

        when(chatMessageRepository.findAllByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findByMemberId(member1.getMemberId())).thenReturn(Optional.of(member1));
        when(memberRepository.findByMemberId(member2.getMemberId())).thenReturn(Optional.of(member2));
        when(memberRepository.findByMemberId(member3.getMemberId())).thenReturn(Optional.of(member3));

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        List<ChatResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(6);
        assertThat(content.get(0).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getProfileUrl()).isNotNull();
        assertThat(content.get(0).getMessage()).isEqualTo(message1.getMessage());
        assertThat(content.get(1).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(1).getProfileUrl()).isNotNull();
        assertThat(content.get(1).getMessage()).isEqualTo(message2.getMessage());
        assertThat(content.get(2).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(2).getProfileUrl()).isNotNull();
        assertThat(content.get(2).getMessage()).isEqualTo(message3.getMessage());
        assertThat(content.get(3).getNickname()).isEqualTo(member2.getNickname());
        assertThat(content.get(3).getMessage()).isEqualTo(message4.getMessage());
        assertThat(content.get(4).getNickname()).isEqualTo(member3.getNickname());
        assertThat(content.get(4).getMessage()).isEqualTo(message5.getMessage());assertEquals(content.get(0).getNickname(), member1.getNickname());
        assertThat(content.get(5).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(5).getMessage()).isEqualTo(message6.getMessage());

    }

    @Test
    @DisplayName("채팅 메시지 조회 시 한 사용자가 연속적인 채팅을 보낸 경우")
    void getChatMessagesOneMember(){
        // given
        Pageable pageable = PageUtil.chatPageable(1);

        ChatMessage message1 = createChatMessage("id1", schedule.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("id2", schedule.getScheduleId(), member1, "hello2");
        ChatMessage message3 = createChatMessage("id3", schedule.getScheduleId(), member1, "hello3");

        List<ChatMessage> messageList = new ArrayList<>(List.of(message1, message2, message3));
        Page<ChatMessage> chatPage = PageUtil.createPage(messageList, pageable, messageList.size());

        when(chatMessageRepository.findAllByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findByMemberId(anyLong())).thenReturn(Optional.of(member1));

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        List<ChatResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content.get(0).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getProfileUrl()).isNotNull();
        assertThat(content.get(0).getMessage()).isEqualTo(message1.getMessage());
    }

    @Test
    @DisplayName("채팅 메시지 조회 시 연속적인 대화가 없는 경우")
    void getChatMessagesOneChat(){
        // given
        Pageable pageable = PageUtil.chatPageable(1);

        ChatMessage message1 = createChatMessage("id1", schedule.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("id4", schedule.getScheduleId(), member2, "hello2");
        ChatMessage message3 = createChatMessage("id5", schedule.getScheduleId(), member3, "hello3");
        List<ChatMessage> messageList = new ArrayList<>(List.of(message1, message2, message3));
        Page<ChatMessage> chatPage = PageUtil.createPage(messageList, pageable, messageList.size());

        when(chatMessageRepository.findAllByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findByMemberId(member1.getMemberId())).thenReturn(Optional.of(member1));
        when(memberRepository.findByMemberId(member2.getMemberId())).thenReturn(Optional.of(member2));
        when(memberRepository.findByMemberId(member3.getMemberId())).thenReturn(Optional.of(member3));

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        List<ChatResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content.get(0).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getProfileUrl()).isNotNull();
        assertThat(content.get(0).getMessage()).isEqualTo(message1.getMessage());
        assertThat(content.get(1).getNickname()).isEqualTo(member2.getNickname());
        assertThat(content.get(1).getMessage()).isEqualTo(message2.getMessage());
        assertThat(content.get(2).getNickname()).isEqualTo(member3.getNickname());
        assertThat(content.get(2).getMessage()).isEqualTo(message3.getMessage());


    }

    @Test
    @DisplayName("채팅 메시지 조회 시 메시지가 없는 경우")
    void getChatMessagesNotMessages(){
        // given
        Pageable pageable = PageUtil.chatPageable(1);

        Page<ChatMessage> chatPage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(chatMessageRepository.findAllByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();

    }

    @Test
    @DisplayName("채팅 메시지 조회 시 사용자 데이터 존재하지 않아 예외 발생")
    void getChatMessagesNotFoundUser_dataNotFoundException(){
        // given
        Pageable pageable = PageUtil.chatPageable(1);

        ChatMessage message1 = createChatMessage("id1", schedule.getScheduleId(), member1, "hello1");
        List<ChatMessage> messageList = new ArrayList<>(List.of(message1));
        Page<ChatMessage> chatPage = PageUtil.createPage(messageList, pageable, messageList.size());

        when(chatMessageRepository.findAllByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findByMemberId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> chatService.getChatMessages(1, schedule.getScheduleId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("채팅 메시지 저장")
    void sendChatMessage(){
        // given
        ChatMessageRequest request = createChatMessageRequest(schedule.getScheduleId(), member1.getNickname(), "hello1");
        TravelAttendee attendee = createTravelAttendee(1L, member1, schedule, AttendeeRole.AUTHOR, AttendeePermission.ALL);

        when(travelScheduleRepository.existsById(anyLong())).thenReturn(true);
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee));
        when(chatMessageRepository.save(any())).thenReturn(createChatMessage("message1", request.getScheduleId(), member1, request.getMessage()));


        // when
        ChatResponse response = chatService.sendChatMessage(request);

        // then
        assertThat(response.getMessageId()).isNotEmpty();
        assertThat(response.getNickname()).isEqualTo(request.getNickname());
        assertThat(response.getMessage()).isEqualTo(request.getMessage());
    }

    @Test
    @DisplayName("채팅 메시지 저장 시 채팅 권한이 없어 예외 발생")
    void sendChatMessage_ForbiddenChatException1(){
        // given
        ChatMessageRequest request = createChatMessageRequest(schedule.getScheduleId(), member1.getNickname(), "hello1");
        TravelAttendee attendee = createTravelAttendee(1L, member1, schedule, AttendeeRole.GUEST, AttendeePermission.EDIT);

        when(travelScheduleRepository.existsById(anyLong())).thenReturn(true);
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString()))
                .thenReturn(Optional.of(attendee));

        // when
        ForbiddenChatException fail = assertThrows(ForbiddenChatException.class, () -> chatService.sendChatMessage(request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getMessage());
    }

    @Test
    @DisplayName("채팅 메시지 저장 시 채팅 권한이 없어 예외 발생")
    void sendChatMessage_ForbiddenChatException2(){
        // given
        ChatMessageRequest request = createChatMessageRequest(schedule.getScheduleId(), member1.getNickname(), "hello1");
        TravelAttendee attendee = createTravelAttendee(1L, member1, schedule, AttendeeRole.GUEST, AttendeePermission.READ);

        when(travelScheduleRepository.existsById(anyLong())).thenReturn(true);
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString()))
                .thenReturn(Optional.of(attendee));


        // when
        ForbiddenChatException fail = assertThrows(ForbiddenChatException.class, () -> chatService.sendChatMessage(request));
        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getMessage());
    }


    @Test
    @DisplayName("닉네임으로 사용자 정보 조회")
    void findChatMemberByNickname(){
        // given
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member1));

        // when
        Member response = chatService.findChatMemberByNickname(member1.getNickname());

        // then
        assertThat(response.getUserId()).isEqualTo(member1.getUserId());
        assertThat(response.getNickname()).isEqualTo(member1.getNickname());
    }


    @Test
    @DisplayName("닉네임으로 사용자 정보 조회 시 데이터 없어 예외 발생")
    void findChatMemberByNickname_dataNotFoundException(){
        // given
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundChatException fail = assertThrows(DataNotFoundChatException.class, () -> chatService.findChatMemberByNickname(member1.getNickname()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());

    }


    @Test
    @DisplayName("참석자 정보 조회")
    void findTravelAttendee(){
        // given
        TravelAttendee attendee = createTravelAttendee(1L, member1, schedule, AttendeeRole.AUTHOR, AttendeePermission.ALL);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString()))
                .thenReturn(Optional.of(attendee));

        // when
        TravelAttendee response = chatService.findTravelAttendee(schedule.getScheduleId(), member1.getUserId());

        // then
        assertThat(response.getRole()).isEqualTo(attendee.getRole());
        assertThat(response.getPermission()).isEqualTo(attendee.getPermission());
    }


    @Test
    @DisplayName("참석자 정보 조회 시 데이터 없어 예외 발생")
    void findTravelAttendee_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.empty());

        // when
        ForbiddenChatException fail = assertThrows(ForbiddenChatException.class, () -> chatService.findTravelAttendee(schedule.getScheduleId(), member1.getUserId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }
}