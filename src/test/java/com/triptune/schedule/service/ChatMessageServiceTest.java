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
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.exception.chat.ForbiddenChatException;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.global.message.ErrorCode;
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
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest extends ScheduleTest {

    @InjectMocks private ChatMessageService chatMessageService;
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
        schedule = createTravelSchedule("테스트");

        ProfileImage profileImage1 = createProfileImage("member1Image");
        member1 = createNativeTypeMember("member1@email.com", profileImage1);

        ProfileImage profileImage2 = createProfileImage("member2Image");
        member2 = createNativeTypeMember( "member2@email.com", profileImage2);

        ProfileImage profileImage3 = createProfileImage("member3Image");
        member3 = createNativeTypeMember("member3@email.com", profileImage3);
    }

    @Test
    @DisplayName("채팅 메시지 조회")
    void getChatMessages(){
        // given
        Pageable pageable = PageUtils.chatPageable(1);

        ChatMessage message1 = createChatMessage(1L, 1L, "hello1");
        ChatMessage message2 = createChatMessage(1L, 1L, "hello2");
        ChatMessage message3 = createChatMessage(1L, 1L, "hello3");
        ChatMessage message4 = createChatMessage(1L, 2L, "hello4");
        ChatMessage message5 = createChatMessage(1L, 3L, "hello5");
        ChatMessage message6 = createChatMessage(1L, 1L, "hello6");

        List<ChatMessage> messages = List.of(message1, message2, message3, message4, message5, message6);
        Page<ChatMessage> chatPage = PageUtils.createPage(messages, pageable, messages.size());

        List<MemberProfileResponse> memberProfileResponses = List.of(
                createMemberProfileResponse(1L, member1.getNickname(), member1.getProfileImage().getS3ObjectUrl()),
                createMemberProfileResponse(2L, member2.getNickname(), member2.getProfileImage().getS3ObjectUrl()),
                createMemberProfileResponse(3L, member3.getNickname(), member3.getProfileImage().getS3ObjectUrl())
        );

        when(chatMessageRepository.findAllByScheduleId(any(Pageable.class), anyLong())).thenReturn(chatPage);
        when(memberRepository.findMembersProfileByMemberId(any())).thenReturn(memberProfileResponses);


        // when
        Page<ChatResponse> response = chatMessageService.getChatMessages(1, 1L);

        // then
        List<ChatResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(6);

        assertThat(content)
                .extracting(
                        ChatResponse::getNickname,
                        ChatResponse::getProfileUrl,
                        ChatResponse::getMessage
                )
                .containsExactly(
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message1.getMessage()
                        ),
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message2.getMessage()
                        ),
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message3.getMessage()
                        ),
                        tuple(
                                member2.getNickname(),
                                member2.getProfileImage().getS3ObjectUrl(),
                                message4.getMessage()
                        ),
                        tuple(
                                member3.getNickname(),
                                member3.getProfileImage().getS3ObjectUrl(),
                                message5.getMessage()
                        ),
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message6.getMessage()
                        )
                );

    }

    @Test
    @DisplayName("채팅 메시지 조회 시 한 회원이 연속적인 채팅을 보낸 경우")
    void getChatMessagesOneMember(){
        // given
        Pageable pageable = PageUtils.chatPageable(1);

        ChatMessage message1 = createChatMessage(1L, 1L, "hello1");
        ChatMessage message2 = createChatMessage(1L, 1L, "hello2");
        ChatMessage message3 = createChatMessage(1L, 1L, "hello3");

        List<ChatMessage> messages = List.of(message1, message2, message3);
        Page<ChatMessage> chatPage = PageUtils.createPage(messages, pageable, messages.size());

        List<MemberProfileResponse> memberProfileResponses = List.of(
                createMemberProfileResponse(1L, member1.getNickname(), member1.getProfileImage().getS3ObjectUrl()),
                createMemberProfileResponse(2L, member2.getNickname(), member2.getProfileImage().getS3ObjectUrl())
        );

        when(chatMessageRepository.findAllByScheduleId(any(Pageable.class), anyLong())).thenReturn(chatPage);
        when(memberRepository.findMembersProfileByMemberId(any())).thenReturn(memberProfileResponses);

        // when
        Page<ChatResponse> response = chatMessageService.getChatMessages(1, 1L);

        // then
        List<ChatResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content)
                .extracting(
                        ChatResponse::getNickname,
                        ChatResponse::getProfileUrl,
                        ChatResponse::getMessage
                )
                .containsExactly(
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message1.getMessage()
                        ),
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message2.getMessage()
                        ),
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message3.getMessage()
                        )
                );
    }

    @Test
    @DisplayName("채팅 메시지 조회 시 연속적인 대화가 없는 경우")
    void getChatMessagesOneChat(){
        // given
        Pageable pageable = PageUtils.chatPageable(1);

        ChatMessage message1 = createChatMessage(1L, 1L, "hello1");
        ChatMessage message2 = createChatMessage(1L, 2L, "hello2");
        ChatMessage message3 = createChatMessage(1L, 3L, "hello3");

        List<ChatMessage> messages = List.of(message1, message2, message3);
        Page<ChatMessage> chatPage = PageUtils.createPage(messages, pageable, messages.size());

        List<MemberProfileResponse> memberProfileResponses = List.of(
                createMemberProfileResponse(1L, member1.getNickname(), member1.getProfileImage().getS3ObjectUrl()),
                createMemberProfileResponse(2L, member2.getNickname(), member2.getProfileImage().getS3ObjectUrl()),
                createMemberProfileResponse(3L, member3.getNickname(), member3.getProfileImage().getS3ObjectUrl())
        );

        when(chatMessageRepository.findAllByScheduleId(any(Pageable.class), anyLong())).thenReturn(chatPage);
        when(memberRepository.findMembersProfileByMemberId(any())).thenReturn(memberProfileResponses);


        // when
        Page<ChatResponse> response = chatMessageService.getChatMessages(1, 1L);

        // then
        List<ChatResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content)
                .extracting(
                        ChatResponse::getNickname,
                        ChatResponse::getProfileUrl,
                        ChatResponse::getMessage
                )
                .containsExactly(
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message1.getMessage()
                        ),
                        tuple(
                                member2.getNickname(),
                                member2.getProfileImage().getS3ObjectUrl(),
                                message2.getMessage()
                        ),
                        tuple(
                                member3.getNickname(),
                                member3.getProfileImage().getS3ObjectUrl(),
                                message3.getMessage()
                        )
                );

    }

    @Test
    @DisplayName("채팅 메시지 조회 시 메시지가 없는 경우")
    void getChatMessagesNotMessages(){
        // given
        Pageable pageable = PageUtils.chatPageable(1);

        Page<ChatMessage> chatPage = PageUtils.createPage(new ArrayList<>(), pageable, 0);

        when(chatMessageRepository.findAllByScheduleId(any(Pageable.class), anyLong())).thenReturn(chatPage);

        // when
        Page<ChatResponse> response = chatMessageService.getChatMessages(1, 1L);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();

    }


    @Test
    @DisplayName("채팅 메시지에서 회원 인덱스 추출")
    void extractMemberId(){
        // given
        ChatMessage message1 = createChatMessage(1L, 1L, "hello1");
        ChatMessage message2 = createChatMessage(1L, 2L, "hello2");
        ChatMessage message3 = createChatMessage(1L, 3L, "hello3");
        List<ChatMessage> messages = List.of(message1, message2, message3);

        // when
        Set<Long> response = chatMessageService.extractMemberId(messages);

        // then
        assertThat(response.size()).isEqualTo(3);
        assertThat(response)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    @DisplayName("채팅 메시지에서 회원 인덱스 추출 시 채팅 메시지가 없는 경우")
    void extractMemberId_emptyMessage(){
        // given
        List<ChatMessage> messages = new ArrayList<>();

        // when
        Set<Long> response = chatMessageService.extractMemberId(messages);

        // then
        assertThat(response.size()).isEqualTo(0);
        assertThat(response.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("회원 프로필 조회")
    void getMemberProfiles(){
        // given
        Set<Long> request = new HashSet<>();
        request.add(1L);
        request.add(2L);

        List<MemberProfileResponse> memberProfileResponses = List.of(
                createMemberProfileResponse(1L, member1.getNickname(), member1.getProfileImage().getS3ObjectUrl()),
                createMemberProfileResponse(2L, member2.getNickname(), member2.getProfileImage().getS3ObjectUrl())
        );

        when(memberRepository.findMembersProfileByMemberId(request))
                .thenReturn(memberProfileResponses);

        // when
        Map<Long, MemberProfileResponse> response = chatMessageService.getMemberProfiles(request);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.values())
                .extracting(
                        MemberProfileResponse::getNickname,
                        MemberProfileResponse::getProfileUrl
                )
                .containsExactly(
                        tuple(member1.getNickname(), member1.getProfileImage().getS3ObjectUrl()),
                        tuple(member2.getNickname(), member2.getProfileImage().getS3ObjectUrl())
                );
    }

    @Test
    @DisplayName("회원 프로필 조회 시 데이터 없는 경우")
    void getMemberProfiles_emptyData(){
        // given
        Set<Long> request = new HashSet<>();

        when(memberRepository.findMembersProfileByMemberId(request)).thenReturn(Collections.emptyList());

        // when
        Map<Long, MemberProfileResponse> response = chatMessageService.getMemberProfiles(request);

        // then
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("ChatResponse dto 로 변경")
    void convertChatResponse(){
        // given
        ChatMessage message1 = createChatMessage(1L, 1L, "메시지1");
        ChatMessage message2 = createChatMessage(1L, 2L, "메시지2");
        ChatMessage message3 = createChatMessage(1L, 1L, "메시지3");

        List<ChatMessage> chatMessages = List.of(message1, message2, message3);

        MemberProfileResponse response1 = createMemberProfileResponse(1L, member1.getNickname(), member1.getProfileImage().getS3ObjectUrl());
        MemberProfileResponse response2 = createMemberProfileResponse(2L, member2.getNickname(), member2.getProfileImage().getS3ObjectUrl());

        Map<Long, MemberProfileResponse> memberProfileMap = new HashMap<>();
        memberProfileMap.put(1L, response1);
        memberProfileMap.put(2L, response2);

        // when
        List<ChatResponse> response = chatMessageService.convertChatResponse(chatMessages, memberProfileMap);

        // then
        assertThat(response.size()).isEqualTo(3);
        assertThat(response)
                .extracting(
                        ChatResponse::getNickname,
                        ChatResponse::getProfileUrl,
                        ChatResponse::getMessage
                )
                .containsExactly(
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message1.getMessage()
                        ),
                        tuple(
                                member2.getNickname(),
                                member2.getProfileImage().getS3ObjectUrl(),
                                message2.getMessage()
                        ),
                        tuple(
                                member1.getNickname(),
                                member1.getProfileImage().getS3ObjectUrl(),
                                message3.getMessage()
                        )
                );
    }


    @Test
    @DisplayName("채팅 메시지 저장")
    void sendChatMessage(){
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMemberWithId(1L, "member@email.com", profileImage);

        ChatMessageRequest request = createChatMessageRequest(1L, member.getNickname(), "hello1");
        TravelAttendee author = createAuthorTravelAttendee(schedule, member);

        ChatMessage message = createChatMessage(1L, 1L, request.getMessage());

        when(travelScheduleRepository.existsById(anyLong())).thenReturn(true);
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(chatMessageRepository.save(any())).thenReturn(message);


        // when
        ChatResponse response = chatMessageService.sendChatMessage(request);

        // then
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileUrl()).isEqualTo(member.getProfileImage().getS3ObjectUrl());
        assertThat(response.getMessage()).isEqualTo(message.getMessage());
    }

    @Test
    @DisplayName("채팅 메시지 저장 시 채팅 권한이 없어 예외 발생")
    void sendChatMessage_ForbiddenChatException1(){
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMemberWithId(1L, "member@email.com", profileImage);

        ChatMessageRequest request = createChatMessageRequest(1L, member.getNickname(), "hello1");
        TravelAttendee guest = createGuestTravelAttendee(schedule, member, AttendeePermission.EDIT);

        when(travelScheduleRepository.existsById(anyLong())).thenReturn(true);
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(guest));

        // when
        ForbiddenChatException fail = assertThrows(ForbiddenChatException.class, () -> chatMessageService.sendChatMessage(request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_CHAT_ATTENDEE);
    }

    @Test
    @DisplayName("채팅 메시지 저장 시 채팅 권한이 없어 예외 발생")
    void sendChatMessage_ForbiddenChatException2(){
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMemberWithId(1L, "member@email.com", profileImage);

        ChatMessageRequest request = createChatMessageRequest(1L, member.getNickname(), "hello1");
        TravelAttendee guest = createGuestTravelAttendee(schedule, member, AttendeePermission.READ);

        when(travelScheduleRepository.existsById(anyLong())).thenReturn(true);
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(guest));


        // when
        ForbiddenChatException fail = assertThrows(ForbiddenChatException.class, () -> chatMessageService.sendChatMessage(request));
        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_CHAT_ATTENDEE);
    }


}