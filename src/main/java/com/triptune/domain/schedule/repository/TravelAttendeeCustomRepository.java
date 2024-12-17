package com.triptune.domain.schedule.repository;

public interface TravelAttendeeCustomRepository {
    String findAuthorNicknameByScheduleId(Long scheduleId);
}
