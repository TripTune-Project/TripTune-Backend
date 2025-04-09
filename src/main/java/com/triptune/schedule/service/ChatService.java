package com.triptune.schedule.service;

import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.schedule.dto.request.ChatMessageRequest;
import com.triptune.schedule.dto.response.ChatResponse;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.exception.DataNotFoundChatException;
import com.triptune.schedule.exception.ForbiddenChatException;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.util.PageUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelScheduleRepository travelScheduleRepository;


    public Page<ChatResponse> getChatMessages(int page, Long scheduleId) {
        Pageable pageable = PageUtils.chatPageable(page);
        Page<ChatMessage> chatPage = chatMessageRepository.findAllByScheduleId(pageable, scheduleId);

        Set<Long> memberIds = extractMemberId(chatPage.getContent());
        Map<Long, MemberProfileResponse> memberProfileMap = getMemberProfiles(memberIds);

        List<ChatResponse> chatResponses = convertChatResponse(chatPage.getContent(), memberProfileMap);
        return PageUtils.createPage(chatResponses, pageable, chatPage.getTotalElements());
    }

    public Set<Long> extractMemberId(List<ChatMessage> chatMessages){
        return chatMessages.stream()
                .map(ChatMessage::getMemberId)
                .collect(Collectors.toSet());
    }

    public Map<Long, MemberProfileResponse> getMemberProfiles(Set<Long> memberIds){
        return memberRepository.findMembersProfileByMemberId(memberIds)
                .stream()
                .collect(Collectors.toMap(MemberProfileResponse::getMemberId, Function.identity()));

    }

    public List<ChatResponse> convertChatResponse(List<ChatMessage> chatMessages, Map<Long, MemberProfileResponse> memberProfileMap){
        return chatMessages.stream()
                .map(message -> ChatResponse.from(message, memberProfileMap.get(message.getMemberId())))
                .sorted(Comparator.comparing(ChatResponse::getTimestamp))
                .toList();
    }


    public ChatResponse sendChatMessage(ChatMessageRequest chatMessageRequest) {
        validateSchedule(chatMessageRequest.getScheduleId());

        Member member = getMemberByNickname(chatMessageRequest.getNickname());
        TravelAttendee attendee = getTravelAttendee(chatMessageRequest.getScheduleId(), member.getEmail());

        if (!attendee.getPermission().isEnableChat()){
            throw new ForbiddenChatException(ErrorCode.FORBIDDEN_CHAT_ATTENDEE);
        }

        ChatMessage message = chatMessageRepository.save(ChatMessage.of(member, chatMessageRequest));

        return ChatResponse.from(message, member);
    }

    public void validateSchedule(Long scheduleId){
        boolean isExist = travelScheduleRepository.existsById(scheduleId);

        if (!isExist){
            throw new DataNotFoundChatException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
    }

    private Member getMemberByNickname(String nickname){
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new DataNotFoundChatException(ErrorCode.MEMBER_NOT_FOUND));

    }

    private TravelAttendee getTravelAttendee(Long scheduleId, String email){
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_Email(scheduleId, email)
                .orElseThrow(() -> new ForbiddenChatException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));
    }

}
