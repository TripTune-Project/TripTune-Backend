package com.triptune.schedule.controller;

import com.triptune.schedule.dto.request.ChatMessageRequest;
import com.triptune.schedule.dto.response.ChatResponse;
import com.triptune.schedule.exception.chat.BadRequestChatException;
import com.triptune.schedule.service.ChatMessageService;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.response.pagination.ApiPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Schedule - Chat", description = "일정 만들기 중 채팅 관련 API")
public class ChatMessageController {
    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @AttendeeCheck
    @GetMapping("/api/schedules/{scheduleId}/chats")
    @Operation(summary = "채팅 조회", description = "채팅 기록을 조회한다.")
    public ApiPageResponse<ChatResponse> getChatMessages(@PathVariable(name = "scheduleId") Long scheduleId,
                                                         @RequestParam(name = "page") int page){
        Page<ChatResponse> response = chatMessageService.getChatMessages(page, scheduleId);
        return ApiPageResponse.dataResponse(response);
    }

    @MessageMapping("/chats")
    @Operation(summary = "채팅 보내기", description = "메시지를 저장하고 채팅 참가자들에게 메시지를 보낸다.")
    public void sendChatMessage(@Payload ChatMessageRequest chatMessageRequest){
        if (chatMessageRequest.getMessage().length() > MAX_MESSAGE_LENGTH){
            throw new BadRequestChatException(ErrorCode.CHAT_MESSAGE_TOO_LONG);
        }

        ChatResponse response = chatMessageService.sendChatMessage(chatMessageRequest);

        messagingTemplate.convertAndSend(
                "/sub/schedules/" + chatMessageRequest.getScheduleId() + "/chats",
                response
        );
    }

}
