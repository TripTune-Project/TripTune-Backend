package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.enumclass.AttendeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelAttendeeRepository extends JpaRepository<TravelAttendee, Long>, TravelAttendeeRepositoryCustom {
    List<TravelAttendee> findAllByTravelSchedule_ScheduleId(Long scheduleId);
    boolean existsByTravelSchedule_ScheduleIdAndMember_UserId(Long scheduleId, String userId);
    Optional<TravelAttendee> findByTravelSchedule_ScheduleIdAndMember_UserId(Long scheduleId, String userId);
    boolean existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(Long scheduleId, String userId, AttendeeRole role);
    int countByTravelSchedule_ScheduleId(Long scheduleId);
    Optional<TravelAttendee> findByTravelSchedule_ScheduleIdAndAttendeeId(Long scheduleId, Long attendeeId);
    List<TravelAttendee> findAllByMember_UserId(String userId);
}
