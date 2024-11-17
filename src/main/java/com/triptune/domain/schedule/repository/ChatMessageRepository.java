package com.triptune.domain.schedule.repository;

import com.triptune.domain.schedule.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    Page<ChatMessage> findChatByScheduleId(Pageable pageable, Long scheduleId);
}
