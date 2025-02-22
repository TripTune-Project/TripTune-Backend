package com.triptune.schedule.repository;

public interface TravelAttendeeCustomRepository {
    String findAuthorNicknameByScheduleId(Long scheduleId);
}
