package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.enums.AttendeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelAttendeeRepository extends JpaRepository<TravelAttendee, Long>, TravelAttendeeRepositoryCustom {
    boolean existsByTravelSchedule_ScheduleIdAndMember_MemberId(@Param("scheduleId") Long scheduleId, @Param("memberId") Long memberId);
    Optional<TravelAttendee> findByTravelSchedule_ScheduleIdAndMember_MemberId(@Param("scheduleId") Long scheduleId, @Param("memberId") Long memberId);
    boolean existsByTravelSchedule_ScheduleIdAndMember_MemberIdAndRole(@Param("scheduleId") Long scheduleId, @Param("memberId") Long memberId, @Param("role") AttendeeRole role);
    int countByTravelSchedule_ScheduleId(@Param("scheduleId") Long scheduleId);
    Optional<TravelAttendee> findByTravelSchedule_ScheduleIdAndAttendeeId(@Param("scheduleId") Long scheduleId, @Param("attendeeId") Long attendeeId);


    @Modifying
    @Query("select a from TravelAttendee a where a.travelSchedule.scheduleId = :scheduleId")
    List<TravelAttendee> findAllByScheduleId(@Param("scheduleId") Long scheduleId);

    @Modifying
    @Query("select a from TravelAttendee a where a.member.memberId = :memberId")
    List<TravelAttendee> findAllByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("delete from TravelAttendee a where a.travelSchedule.scheduleId = :scheduleId")
    void deleteAllByScheduleId(@Param("scheduleId") Long scheduleId);
}
