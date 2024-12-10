package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.request.ChatMessageRequest;
import com.triptune.domain.schedule.dto.response.ChatResponse;
import com.triptune.domain.schedule.service.ChatService;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.ApiResponse;
import com.triptune.global.response.pagination.ApiPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Schedule - Chat", description = "일정 만들기 중 채팅 관련 API")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @AttendeeCheck
    @GetMapping("/api/schedules/{scheduleId}/chats")
    @Operation(summary = "채팅 조회", description = "채팅 기록을 조회한다.")
    public ApiPageResponse<ChatResponse> getChatMessages(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam(name = "page") int page){
        Page<ChatResponse> response = chatService.getChatMessages(page, scheduleId);
        return ApiPageResponse.dataResponse(response);
    }

    @MessageMapping("/chats")
    @Operation(summary = "채팅 보내기", description = "메시지를 저장하고 채팅 참가자들에게 메시지를 보낸다.")
    public void sendChatMessage(@Payload ChatMessageRequest chatMessageRequest){
        ChatResponse response = chatService.sendChatMessage(chatMessageRequest);

        messagingTemplate.convertAndSend(
                "/sub/schedules/" + chatMessageRequest.getScheduleId() + "/chats",
                response
        );
    }

}
