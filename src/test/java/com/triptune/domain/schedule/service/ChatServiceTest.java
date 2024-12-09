package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.request.ChatMessageRequest;
import com.triptune.domain.schedule.dto.response.ChatResponse;
import com.triptune.domain.schedule.entity.ChatMessage;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.ChatNotFoundException;
import com.triptune.domain.schedule.exception.ForbiddenChatException;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.ChatMessageRepository;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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


    private TravelSchedule schedule;
    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp(){
        schedule = createTravelSchedule(1L, "테스트");
        member1 = createMember(1L, "member1");
        member2 = createMember(2L, "member2");
        member3 = createMember(3L, "member3");

        ProfileImage member1Image = createProfileImage(1L, "member1Image");
        ProfileImage member2Image = createProfileImage(2L, "member2Image");
        ProfileImage member3Image = createProfileImage(3L, "member3Image");
        member1.setProfileImage(member1Image);
        member2.setProfileImage(member2Image);
        member3.setProfileImage(member3Image);
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

        when(chatMessageRepository.findChatByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findByMemberId(member1.getMemberId())).thenReturn(Optional.of(member1));
        when(memberRepository.findByMemberId(member2.getMemberId())).thenReturn(Optional.of(member2));
        when(memberRepository.findByMemberId(member3.getMemberId())).thenReturn(Optional.of(member3));

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        List<ChatResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 6);
        assertEquals(content.get(0).getNickname(), member1.getNickname());
        assertNotNull(content.get(0).getProfileUrl());
        assertEquals(content.get(0).getMessage(), message1.getMessage());
        assertEquals(content.get(1).getNickname(), member1.getNickname());
        assertNotNull(content.get(1).getProfileUrl());
        assertEquals(content.get(1).getMessage(), message2.getMessage());
        assertEquals(content.get(2).getNickname(), member1.getNickname());
        assertNotNull(content.get(2).getProfileUrl());
        assertEquals(content.get(2).getMessage(), message3.getMessage());
        assertEquals(content.get(3).getNickname(), member2.getNickname());
        assertEquals(content.get(3).getMessage(), message4.getMessage());
        assertEquals(content.get(4).getNickname(), member3.getNickname());
        assertEquals(content.get(4).getMessage(), message5.getMessage());assertEquals(content.get(0).getNickname(), member1.getNickname());
        assertEquals(content.get(5).getNickname(), member1.getNickname());
        assertEquals(content.get(5).getMessage(), message6.getMessage());

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

        when(chatMessageRepository.findChatByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findByMemberId(member1.getMemberId())).thenReturn(Optional.of(member1));

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        List<ChatResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 3);
        assertEquals(content.get(0).getNickname(), member1.getNickname());
        assertNotNull(content.get(0).getProfileUrl());
        assertEquals(content.get(0).getMessage(), message1.getMessage());
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

        when(chatMessageRepository.findChatByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findByMemberId(member1.getMemberId())).thenReturn(Optional.of(member1));
        when(memberRepository.findByMemberId(member2.getMemberId())).thenReturn(Optional.of(member2));
        when(memberRepository.findByMemberId(member3.getMemberId())).thenReturn(Optional.of(member3));

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        List<ChatResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 3);
        assertEquals(content.get(0).getNickname(), member1.getNickname());
        assertNotNull(content.get(0).getProfileUrl());
        assertEquals(content.get(0).getMessage(), message1.getMessage());
        assertEquals(content.get(1).getNickname(), member2.getNickname());
        assertEquals(content.get(1).getMessage(), message2.getMessage());
        assertEquals(content.get(2).getNickname(), member3.getNickname());
        assertEquals(content.get(2).getMessage(), message3.getMessage());


    }

    @Test
    @DisplayName("채팅 메시지 조회 시 메시지가 없는 경우")
    void getChatMessagesNotMessages(){
        // given
        Pageable pageable = PageUtil.chatPageable(1);

        Page<ChatMessage> chatPage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(chatMessageRepository.findChatByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        List<ChatResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 0);
        assertTrue(content.isEmpty());

    }

    @Test
    @DisplayName("채팅 메시지 조회 시 사용자 데이터 존재하지 않아 예외 발생")
    void getChatMessagesNotFoundUser_dataNotFoundException(){
        // given
        Pageable pageable = PageUtil.chatPageable(1);

        ChatMessage message1 = createChatMessage("id1", schedule.getScheduleId(), member1, "hello1");
        List<ChatMessage> messageList = new ArrayList<>(List.of(message1));
        Page<ChatMessage> chatPage = PageUtil.createPage(messageList, pageable, messageList.size());

        when(chatMessageRepository.findChatByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findByMemberId(member1.getMemberId())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> chatService.getChatMessages(1, schedule.getScheduleId()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("채팅 메시지 저장")
    void sendChatMessage(){
        // given
        ChatMessageRequest request = createChatMessageRequest(schedule.getScheduleId(), member1.getNickname(), "hello1");
        TravelAttendee attendee = createTravelAttendee(member1, schedule, AttendeeRole.AUTHOR, AttendeePermission.ALL);

        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString()))
                .thenReturn(Optional.of(attendee));


        // when
        ChatResponse response = chatService.sendChatMessage(request);

        // then
        assertEquals(response.getNickname(), request.getNickname());
        assertEquals(response.getMessage(), request.getMessage());
    }

    @Test
    @DisplayName("채팅 메시지 저장 시 채팅 권한이 없어 예외 발생")
    void sendChatMessage_ForbiddenChatException1(){
        // given
        ChatMessageRequest request = createChatMessageRequest(schedule.getScheduleId(), member1.getNickname(), "hello1");
        TravelAttendee attendee = createTravelAttendee(member1, schedule, AttendeeRole.GUEST, AttendeePermission.EDIT);

        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString()))
                .thenReturn(Optional.of(attendee));


        // when
        ForbiddenChatException fail = assertThrows(ForbiddenChatException.class, () -> chatService.sendChatMessage(request));
        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getMessage());
    }

    @Test
    @DisplayName("채팅 메시지 저장 시 채팅 권한이 없어 예외 발생")
    void sendChatMessage_ForbiddenChatException2(){
        // given
        ChatMessageRequest request = createChatMessageRequest(schedule.getScheduleId(), member1.getNickname(), "hello1");
        TravelAttendee attendee = createTravelAttendee(member1, schedule, AttendeeRole.GUEST, AttendeePermission.READ);

        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString()))
                .thenReturn(Optional.of(attendee));


        // when
        ForbiddenChatException fail = assertThrows(ForbiddenChatException.class, () -> chatService.sendChatMessage(request));
        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getMessage());
    }

    @Test
    @DisplayName("사용자 인덱스로 사용자 정보 조회")
    void getMemberByMemberId(){
        // given
        when(memberRepository.findByMemberId(anyLong())).thenReturn(Optional.of(member1));

        // when
        Member response = chatService.getMemberByMemberId(member1.getMemberId());

        // then
        assertEquals(response.getUserId(), member1.getUserId());
        assertEquals(response.getNickname(), member1.getNickname());
    }

    @Test
    @DisplayName("사용자 인덱스로 사용자 정보 조회 시 데이터 없어 예외 발생")
    void getMemberByMemberId_dataNotFoundException(){
        // given
        when(memberRepository.findByMemberId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> chatService.getMemberByMemberId(member1.getMemberId()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("닉네임으로 사용자 정보 조회")
    void getChatMemberByNickname(){
        // given
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member1));

        // when
        Member response = chatService.getChatMemberByNickname(member1.getNickname());

        // then
        assertEquals(response.getUserId(), member1.getUserId());
        assertEquals(response.getNickname(), member1.getNickname());
    }


    @Test
    @DisplayName("닉네임으로 사용자 정보 조회 시 데이터 없어 예외 발생")
    void getChatMemberByNickname_dataNotFoundException(){
        // given
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.empty());

        // when
        ChatNotFoundException fail = assertThrows(ChatNotFoundException.class, () -> chatService.getChatMemberByNickname(member1.getNickname()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }


    @Test
    @DisplayName("참석자 정보 조회")
    void getTravelAttendee(){
        // given
        TravelAttendee attendee = createTravelAttendee(member1, schedule, AttendeeRole.AUTHOR, AttendeePermission.ALL);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString()))
                .thenReturn(Optional.of(attendee));

        // when
        TravelAttendee response = chatService.getTravelAttendee(schedule.getScheduleId(), member1.getUserId());

        // then
        assertEquals(response.getRole(), attendee.getRole());
        assertEquals(response.getPermission(), attendee.getPermission());
    }


    @Test
    @DisplayName("참석자 정보 조회 시 데이터 없어 예외 발생")
    void getTravelAttendee_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.empty());

        // when
        ForbiddenChatException fail = assertThrows(ForbiddenChatException.class, () -> chatService.getTravelAttendee(schedule.getScheduleId(), member1.getUserId()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }
}