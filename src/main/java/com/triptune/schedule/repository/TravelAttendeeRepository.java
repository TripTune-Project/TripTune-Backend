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
    boolean existsByTravelSchedule_ScheduleIdAndMember_Email(@Param("scheduleId") Long scheduleId, @Param("email") String email);
    Optional<TravelAttendee> findByTravelSchedule_ScheduleIdAndMember_Email(@Param("scheduleId") Long scheduleId, @Param("email") String email);
    boolean existsByTravelSchedule_ScheduleIdAndMember_EmailAndRole(@Param("scheduleId") Long scheduleId, @Param("email") String email, @Param("role") AttendeeRole role);
    int countByTravelSchedule_ScheduleId(@Param("scheduleId") Long scheduleId);
    Optional<TravelAttendee> findByTravelSchedule_ScheduleIdAndAttendeeId(@Param("scheduleId") Long scheduleId, @Param("attendeeId") Long attendeeId);
    List<TravelAttendee> findAllByMember_Email(@Param("email") String email);
}
