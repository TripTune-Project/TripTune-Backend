package com.triptune.schedule.service;

import com.triptune.global.s3.S3ObjectManager;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.schedule.dto.request.ChatMessageRequest;
import com.triptune.schedule.dto.response.ChatResponse;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.exception.chat.DataNotFoundChatException;
import com.triptune.schedule.exception.chat.ForbiddenChatException;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final S3ObjectManager s3ObjectManager;


    public Page<ChatResponse> getChatMessages(int page, Long scheduleId) {
        Pageable pageable = PageUtils.chatPageable(page);
        Page<ChatMessage> chatPage = chatMessageRepository.findAllByScheduleId(pageable, scheduleId);

        Set<Long> memberIds = extractMemberId(chatPage.getContent());
        Map<Long, MemberProfileResponse> memberProfileMap = getMemberProfiles(memberIds);

        List<ChatResponse> chatResponses = convertChatResponse(chatPage.getContent(), memberProfileMap);
        return PageUtils.createPage(chatResponses, pageable, chatPage.getTotalElements());
    }

    private Set<Long> extractMemberId(List<ChatMessage> chatMessages){
        return chatMessages.stream()
                .map(ChatMessage::getMemberId)
                .collect(Collectors.toSet());
    }

    private Map<Long, MemberProfileResponse> getMemberProfiles(Set<Long> memberIds){
        List<Member> members = memberRepository.findByIds(memberIds);
        Map<Long, MemberProfileResponse> memberProfileMap = new HashMap<>();

        for (Member member : members) {
            String profileUrl = s3ObjectManager.generateS3ObjectUrl(member.getProfileImage().getS3ObjectKey());
            MemberProfileResponse memberProfileRes = MemberProfileResponse.of(member.getMemberId(), member.getNickname(), profileUrl);
            memberProfileMap.put(member.getMemberId(), memberProfileRes);
        }

        return memberProfileMap;

    }

    private List<ChatResponse> convertChatResponse(List<ChatMessage> chatMessages, Map<Long, MemberProfileResponse> memberProfileMap){
        return chatMessages.stream()
                .map(message -> ChatResponse.from(message, memberProfileMap.get(message.getMemberId())))
                .sorted(Comparator.comparing(ChatResponse::getTimestamp))
                .toList();
    }


    @Transactional
    public ChatResponse sendChatMessage(ChatMessageRequest chatMessageRequest) {
        validateSchedule(chatMessageRequest.getScheduleId());

        Member member = getMemberByNickname(chatMessageRequest.getNickname());
        TravelAttendee attendee = getTravelAttendee(chatMessageRequest.getScheduleId(), member.getMemberId());

        validateEnableChat(attendee);

        ChatMessage chatMessage = ChatMessage.createChatMessage(
                chatMessageRequest.getScheduleId(),
                member.getMemberId(),
                chatMessageRequest.getMessage()
        );
        chatMessageRepository.save(chatMessage);

        String profileUrl = s3ObjectManager.generateS3ObjectUrl(member.getProfileImage().getS3ObjectKey());

        return ChatResponse.of(chatMessage, member, profileUrl);
    }


    private void validateSchedule(Long scheduleId){
        if (!travelScheduleRepository.existsById(scheduleId)){
            throw new DataNotFoundChatException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
    }

    private Member getMemberByNickname(String nickname){
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new DataNotFoundChatException(ErrorCode.MEMBER_NOT_FOUND));

    }

    private TravelAttendee getTravelAttendee(Long scheduleId, Long memberId){
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId)
                .orElseThrow(() -> new ForbiddenChatException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));
    }

    private void validateEnableChat(TravelAttendee attendee) {
        if (!attendee.isEnableChat()){
            throw new ForbiddenChatException(ErrorCode.FORBIDDEN_CHAT_ATTENDEE);
        }
    }

}
