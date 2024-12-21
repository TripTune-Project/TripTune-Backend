package com.triptune.domain.schedule.repository;

import com.triptune.domain.schedule.entity.TravelSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelScheduleRepository extends JpaRepository<TravelSchedule, Long>, TravelScheduleCustomRepository {
    Optional<TravelSchedule> findByScheduleId(Long scheduleId);
}
