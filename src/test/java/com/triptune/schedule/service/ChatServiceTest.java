package com.triptune.schedule.service;

import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.dto.request.ChatMessageRequest;
import com.triptune.schedule.dto.response.ChatResponse;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeePermission;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.schedule.exception.ForbiddenChatException;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.util.PageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest extends ScheduleTest {

    @InjectMocks private ChatService chatService;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;
    @Mock private TravelScheduleRepository travelScheduleRepository;

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

        member1 = createMember(1L, "member1@email.com", profileImage1);
        member2 = createMember(2L, "member2@email.com", profileImage2);
        member3 = createMember(3L, "member3@email.com", profileImage3);
    }

    @Test
    @DisplayName("채팅 메시지 조회")
    void getChatMessages(){
        // given
        Pageable pageable = PageUtils.chatPageable(1);

        ChatMessage message1 = createChatMessage("id1", schedule.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("id2", schedule.getScheduleId(), member1, "hello2");
        ChatMessage message3 = createChatMessage("id3", schedule.getScheduleId(), member1, "hello3");
        ChatMessage message4 = createChatMessage("id4", schedule.getScheduleId(), member2, "hello4");
        ChatMessage message5 = createChatMessage("id5", schedule.getScheduleId(), member3, "hello5");
        ChatMessage message6 = createChatMessage("id6", schedule.getScheduleId(), member1, "hello6");

        List<ChatMessage> messageList = List.of(message1, message2, message3, message4, message5, message6);
        Page<ChatMessage> chatPage = PageUtils.createPage(messageList, pageable, messageList.size());

        List<MemberProfileResponse> memberProfileResponses = List.of(
                createMemberProfileResponse(1L, "member1"),
                createMemberProfileResponse(2L, "member2"),
                createMemberProfileResponse(3L, "member3")
        );

        when(chatMessageRepository.findAllByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findMembersProfileByMemberId(any())).thenReturn(memberProfileResponses);


        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        List<ChatResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(6);
        assertThat(content.get(0).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getProfileUrl()).isNotNull();
        assertThat(content.get(0).getMessage()).isEqualTo(message1.getMessage());
        assertThat(content.get(1).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(1).getMessage()).isEqualTo(message2.getMessage());
        assertThat(content.get(2).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(2).getMessage()).isEqualTo(message3.getMessage());
        assertThat(content.get(3).getNickname()).isEqualTo(member2.getNickname());
        assertThat(content.get(3).getMessage()).isEqualTo(message4.getMessage());
        assertThat(content.get(4).getNickname()).isEqualTo(member3.getNickname());
        assertThat(content.get(4).getMessage()).isEqualTo(message5.getMessage());
        assertThat(content.get(5).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(5).getMessage()).isEqualTo(message6.getMessage());

    }

    @Test
    @DisplayName("채팅 메시지 조회 시 한 사용자가 연속적인 채팅을 보낸 경우")
    void getChatMessagesOneMember(){
        // given
        Pageable pageable = PageUtils.chatPageable(1);

        List<ChatMessage> messageList = List.of(
                createChatMessage("id1", schedule.getScheduleId(), member1, "hello1"),
                createChatMessage("id2", schedule.getScheduleId(), member1, "hello2"),
                createChatMessage("id3", schedule.getScheduleId(), member1, "hello3")
        );

        Page<ChatMessage> chatPage = PageUtils.createPage(messageList, pageable, messageList.size());

        List<MemberProfileResponse> memberProfileResponses = List.of(
                createMemberProfileResponse(1L, "member1"),
                createMemberProfileResponse(2L, "member2")
        );

        when(chatMessageRepository.findAllByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findMembersProfileByMemberId(any())).thenReturn(memberProfileResponses);

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        List<ChatResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content.get(0).getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getProfileUrl()).isNotNull();
        assertThat(content.get(0).getMessage()).isEqualTo("hello1");
    }

    @Test
    @DisplayName("채팅 메시지 조회 시 연속적인 대화가 없는 경우")
    void getChatMessagesOneChat(){
        // given
        Pageable pageable = PageUtils.chatPageable(1);

        ChatMessage message1 = createChatMessage("id1", schedule.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("id4", schedule.getScheduleId(), member2, "hello2");
        ChatMessage message3 = createChatMessage("id5", schedule.getScheduleId(), member3, "hello3");

        List<ChatMessage> messageList = List.of(message1, message2, message3);
        Page<ChatMessage> chatPage = PageUtils.createPage(messageList, pageable, messageList.size());

        List<MemberProfileResponse> memberProfileResponses = List.of(
                createMemberProfileResponse(1L, "member1"),
                createMemberProfileResponse(2L, "member2"),
                createMemberProfileResponse(3L, "member3")
        );

        when(chatMessageRepository.findAllByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);
        when(memberRepository.findMembersProfileByMemberId(any())).thenReturn(memberProfileResponses);


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
        Pageable pageable = PageUtils.chatPageable(1);

        Page<ChatMessage> chatPage = PageUtils.createPage(new ArrayList<>(), pageable, 0);

        when(chatMessageRepository.findAllByScheduleId(pageable, schedule.getScheduleId())).thenReturn(chatPage);

        // when
        Page<ChatResponse> response = chatService.getChatMessages(1, schedule.getScheduleId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();

    }


    @Test
    @DisplayName("채팅 메시지에서 사용자 인덱스 추출")
    void extractMemberId(){
        // given
        ChatMessage message1 = createChatMessage("id1", schedule.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("id4", schedule.getScheduleId(), member2, "hello2");
        ChatMessage message3 = createChatMessage("id5", schedule.getScheduleId(), member3, "hello3");
        List<ChatMessage> messageList = List.of(message1, message2, message3);

        // when
        Set<Long> response = chatService.extractMemberId(messageList);

        // then
        assertThat(response.size()).isEqualTo(3);
        assertThat(response.contains(member1.getMemberId())).isTrue();
        assertThat(response.contains(member2.getMemberId())).isTrue();
        assertThat(response.contains(member3.getMemberId())).isTrue();
    }

    @Test
    @DisplayName("채팅 메시지에서 사용자 인덱스 추출 시 채팅 메시지가 없는 경우")
    void extractMemberId_emptyMessage(){
        // given
        List<ChatMessage> messageList = new ArrayList<>();

        // when
        Set<Long> response = chatService.extractMemberId(messageList);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("사용자 프로필 조회")
    void getMemberProfiles(){
        // given
        Set<Long> request = new HashSet<>();
        request.add(1L);
        request.add(2L);

        List<MemberProfileResponse> memberProfileResponses = List.of(
                createMemberProfileResponse(1L, "member1"),
                createMemberProfileResponse(2L, "member2")
        );

        when(memberRepository.findMembersProfileByMemberId(request))
                .thenReturn(memberProfileResponses);

        // when
        Map<Long, MemberProfileResponse> response = chatService.getMemberProfiles(request);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(1L).getMemberId()).isEqualTo(1);
        assertThat(response.get(1L).getNickname()).isEqualTo("member1");
        assertThat(response.get(2L).getMemberId()).isEqualTo(2);
        assertThat(response.get(2L).getNickname()).isEqualTo("member2");
    }

    @Test
    @DisplayName("사용자 프로필 조회 시 데이터 없는 경우")
    void getMemberProfiles_emptyData(){
        // given
        Set<Long> request = new HashSet<>();

        when(memberRepository.findMembersProfileByMemberId(request))
                .thenReturn(new ArrayList<>());

        // when
        Map<Long, MemberProfileResponse> response = chatService.getMemberProfiles(request);

        // then
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("ChatResponse dto 로 변경")
    void convertChatResponse(){
        // given
        List<ChatMessage> chatMessages = List.of(
                createChatMessage("1", 1L, member1, "메시지1"),
                createChatMessage("2", 1L, member2, "메시지2"),
                createChatMessage("3", 1L, member1, "메시지3")
        );


        Map<Long, MemberProfileResponse> memberProfileMap = new HashMap<>();
        memberProfileMap.put(1L, createMemberProfileResponse(1L, "member1"));
        memberProfileMap.put(2L, createMemberProfileResponse(2L, "member2"));


        // when
        List<ChatResponse> response = chatService.convertChatResponse(chatMessages, memberProfileMap);

        // then
        assertThat(response.size()).isEqualTo(3);
        assertThat(response.get(0).getMessageId()).isEqualTo("1");
        assertThat(response.get(0).getNickname()).isEqualTo("member1");
        assertThat(response.get(1).getMessageId()).isEqualTo("2");
        assertThat(response.get(1).getNickname()).isEqualTo("member2");
        assertThat(response.get(2).getMessageId()).isEqualTo("3");
        assertThat(response.get(2).getNickname()).isEqualTo("member1");
    }


    @Test
    @DisplayName("채팅 메시지 저장")
    void sendChatMessage(){
        // given
        ChatMessageRequest request = createChatMessageRequest(schedule.getScheduleId(), member1.getNickname(), "hello1");
        TravelAttendee attendee = createTravelAttendee(1L, member1, schedule, AttendeeRole.AUTHOR, AttendeePermission.ALL);

        when(travelScheduleRepository.existsById(anyLong())).thenReturn(true);
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong())).thenReturn(Optional.of(attendee));
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
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
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
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(attendee));


        // when
        ForbiddenChatException fail = assertThrows(ForbiddenChatException.class, () -> chatService.sendChatMessage(request));
        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_CHAT_ATTENDEE.getMessage());
    }


}