package com.triptune.route.repository;

import com.triptune.route.entity.TravelRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelRouteRepository extends JpaRepository<TravelRoute, Long>, TravelRouteRepositoryCustom{

    List<TravelRoute> findAllByTravelSchedule_ScheduleId(@Param("scheduleId") Long scheduleId);
    Optional<TravelRoute> findByRouteIdAndTravelSchedule_ScheduleId(Long routeId, Long scheduleId);

    @Modifying
    @Query("delete from TravelRoute r where r.travelSchedule.scheduleId = :scheduleId")
    void deleteAllByScheduleId(@Param("scheduleId") Long scheduleId);

    @Query("""
        select coalesce(max(r.routeOrder), 0)
        from TravelRoute r
        where r.travelSchedule.scheduleId = :scheduleId
    """)
    int findLastRouteOrder(@Param("scheduleId") Long scheduleId);
}
