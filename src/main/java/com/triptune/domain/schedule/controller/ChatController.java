package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.response.ChatResponse;
import com.triptune.domain.schedule.service.ChatService;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.pagination.ApiPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules/{scheduleId}")
@Tag(name = "Schedule - Chat", description = "일정 만들기 중 채팅 관련 API")
public class ChatController {

    private final ChatService chatService;

    @AttendeeCheck
    @GetMapping("/chats")
    @Operation(summary = "채팅 조회", description = "채팅 기록을 조회한다.")
    public ApiPageResponse<ChatResponse> getChatMessages(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam(name = "page") int page){
        Page<ChatResponse> response = chatService.getChatMessages(page, scheduleId);

        return ApiPageResponse.dataResponse(response);
    }

}
