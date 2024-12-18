package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.request.ChatMessageRequest;
import com.triptune.domain.schedule.dto.response.ChatResponse;
import com.triptune.domain.schedule.entity.ChatMessage;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.exception.DataNotFoundChatException;
import com.triptune.domain.schedule.exception.ForbiddenChatException;
import com.triptune.domain.schedule.repository.ChatMessageRepository;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelScheduleRepository travelScheduleRepository;


    public Page<ChatResponse> getChatMessages(int page, Long scheduleId) {
        Pageable pageable = PageUtil.chatPageable(page);
        Page<ChatMessage> chatPage = chatMessageRepository.findChatByScheduleId(pageable, scheduleId);

        List<ChatResponse> chatResponseList = chatPage.getContent().isEmpty()
                ? Collections.emptyList()
                : convertToChatResponseList(chatPage.getContent());

        return PageUtil.createPage(chatResponseList, pageable, chatPage.getTotalElements());
    }

    public List<ChatResponse> convertToChatResponseList(List<ChatMessage> messageList) {
        return messageList.stream()
                .map(this::convertToChatResponse)
                .toList();
    }

    public ChatResponse convertToChatResponse(ChatMessage message) {
        Member member = getMemberByMemberId(message.getMemberId());
        return ChatResponse.from(member, message);
    }


    public Member getMemberByMemberId(Long memberId){
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }


    public ChatResponse sendChatMessage(ChatMessageRequest chatMessageRequest) {
        validateSchedule(chatMessageRequest.getScheduleId());

        Member member = findChatMemberByNickname(chatMessageRequest.getNickname());
        TravelAttendee attendee = findTravelAttendee(chatMessageRequest.getScheduleId(), member.getUserId());

        if (!attendee.getPermission().isEnableChat()){
            throw new ForbiddenChatException(ErrorCode.FORBIDDEN_CHAT_ATTENDEE);
        }

        ChatMessage message = ChatMessage.of(member, chatMessageRequest);
        message = chatMessageRepository.save(message);

        return ChatResponse.from(member, message);
    }

    private void validateSchedule(Long scheduleId){
        boolean isExist = travelScheduleRepository.existsById(scheduleId);

        if (!isExist){
            throw new DataNotFoundChatException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
    }


    public Member findChatMemberByNickname(String nickname){
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new DataNotFoundChatException(ErrorCode.USER_NOT_FOUND));

    }

    public TravelAttendee findTravelAttendee(Long scheduleId, String userId){
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId)
                .orElseThrow(() -> new ForbiddenChatException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE));
    }

}
