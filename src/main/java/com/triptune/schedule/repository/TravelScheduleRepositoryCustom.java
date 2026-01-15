package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelScheduleRepositoryCustom {
    Page<TravelSchedule> findTravelSchedules(Pageable pageable, Long memberId);
    Page<TravelSchedule> findSharedTravelSchedules(Pageable pageable, Long memberId);
    Integer countTravelSchedules(Long memberId);
    Integer countSharedTravelSchedules(Long memberId);
    Page<TravelSchedule> searchTravelSchedules(Pageable pageable, String keyword, Long memberId);
    Page<TravelSchedule> searchSharedTravelSchedules(Pageable pageable, String keyword, Long memberId);
    Page<TravelSchedule> findEnableEditTravelSchedules(Pageable pageable, Long memberId);
}
