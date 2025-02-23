package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelScheduleRepository extends JpaRepository<TravelSchedule, Long>, TravelScheduleRepositoryCustom {
    Optional<TravelSchedule> findByScheduleId(Long scheduleId);
}
