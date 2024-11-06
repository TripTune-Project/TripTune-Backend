package com.triptune.domain.schedule.repository;

import com.triptune.domain.schedule.entity.TravelAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelAttendeeRepository extends JpaRepository<TravelAttendee, Long> {
    List<TravelAttendee> findAllByTravelSchedule_ScheduleId(Long scheduleId);
    boolean existsByTravelSchedule_ScheduleIdAndMember_UserId(Long scheduleId, String userId);
    Optional<TravelAttendee> findByTravelSchedule_ScheduleIdAndMember_UserId(Long scheduleId, String userId);
}
