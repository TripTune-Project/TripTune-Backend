package com.triptune.domain.schedule.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.dto.response.ChatResponse;
import com.triptune.domain.schedule.entity.ChatMessage;
import com.triptune.domain.schedule.repository.ChatMessageRepository;
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

    private ChatResponse convertToChatResponse(ChatMessage message) {
        Member member = getSavedMember(message.getMemberId());
        return ChatResponse.from(member, message);
    }

    public Member getSavedMember(Long memberId){
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.USER_NOT_FOUND));

    }

}
