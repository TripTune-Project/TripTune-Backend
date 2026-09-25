package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelRouteRepository extends JpaRepository<TravelRoute, Long>, TravelRouteRepositoryCustom{

    @Modifying
    @Query("delete from TravelRoute r where r.travelSchedule.scheduleId = :scheduleId")
    void deleteAllByScheduleId(@Param("scheduleId") Long scheduleId);
}
