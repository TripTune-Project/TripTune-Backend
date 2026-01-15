package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelRouteRepository extends JpaRepository<TravelRoute, Long> {
    Page<TravelRoute> findAllByTravelSchedule_ScheduleId(Pageable pageable, @Param("scheduleId") Long scheduleId);
    void deleteAllByTravelSchedule_ScheduleId(@Param("scheduleId") Long scheduleId);
}
