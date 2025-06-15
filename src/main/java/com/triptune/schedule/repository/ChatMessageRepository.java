package com.triptune.schedule.repository;

import com.triptune.schedule.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    Page<ChatMessage> findAllByScheduleId(Pageable pageable, @Param("scheduleId") Long scheduleId);
    List<ChatMessage> findAllByScheduleId(@Param("scheduleId") Long scheduleId);
    void deleteAllByScheduleId(@Param("scheduleId") Long scheduleId);
}
