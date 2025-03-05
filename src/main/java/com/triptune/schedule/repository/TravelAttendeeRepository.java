package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.enumclass.AttendeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelAttendeeRepository extends JpaRepository<TravelAttendee, Long>, TravelAttendeeRepositoryCustom {
    List<TravelAttendee> findAllByTravelSchedule_ScheduleId(@Param("scheduleId") Long scheduleId);
    boolean existsByTravelSchedule_ScheduleIdAndMember_UserId(@Param("scheduleId") Long scheduleId, @Param("userId") String userId);
    Optional<TravelAttendee> findByTravelSchedule_ScheduleIdAndMember_UserId(@Param("scheduleId") Long scheduleId, @Param("userId") String userId);
    boolean existsByTravelSchedule_ScheduleIdAndMember_UserIdAndRole(@Param("scheduleId") Long scheduleId, @Param("userId") String userId, @Param("role") AttendeeRole role);
    int countByTravelSchedule_ScheduleId(@Param("scheduleId") Long scheduleId);
    Optional<TravelAttendee> findByTravelSchedule_ScheduleIdAndAttendeeId(@Param("scheduleId") Long scheduleId, @Param("attendeeId") Long attendeeId);
    List<TravelAttendee> findAllByMember_UserId(@Param("userId") String userId);
}
