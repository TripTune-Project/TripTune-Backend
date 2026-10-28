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

    @Modifying
    @Query(value = """
        insert into travel_route (schedule_id, place_id, route_order, created_at)
        select :scheduleId, :placeId, coalesce(max(route_order), 0) + 1, now()
        from travel_route
        where schedule_id = :scheduleId
    """, nativeQuery = true)
    void insertTravelRoute(@Param("scheduleId") Long scheduleId, @Param("placeId") Long placeId);


}
