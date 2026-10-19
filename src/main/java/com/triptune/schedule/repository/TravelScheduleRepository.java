package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelScheduleRepository extends JpaRepository<TravelSchedule, Long>, TravelScheduleRepositoryCustom {

    @Query("select s.routeVersion from TravelSchedule s where s.scheduleId = :scheduleId")
    Optional<Integer> findRouteVersionById(@Param("scheduleId") Long scheduleId);
}
