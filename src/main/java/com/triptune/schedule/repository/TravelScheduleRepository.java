package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelSchedule;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TravelScheduleRepository extends JpaRepository<TravelSchedule, Long>, TravelScheduleRepositoryCustom {

    @Query("select s.routeVersion from TravelSchedule s where s.scheduleId = :scheduleId")
    Optional<Integer> findRouteVersionById(@Param("scheduleId") Long scheduleId);

    @Modifying
    @Query("""
        update TravelSchedule s
        set s.scheduleName = :scheduleName,
            s.startDate = :startDate,
            s.endDate = :endDate,
            s.scheduleVersion = :scheduleVersion + 1
        where s.scheduleId = :scheduleId
            and s.scheduleVersion = :scheduleVersion
    """)
    int updateSchedule(@Param("scheduleId") Long scheduleId,
                        @Param("scheduleName") String scheduleName,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("scheduleVersion") int scheduleVersion);
}
