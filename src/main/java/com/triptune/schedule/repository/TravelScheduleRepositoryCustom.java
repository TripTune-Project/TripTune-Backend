package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelScheduleRepositoryCustom {
    Page<TravelSchedule> findTravelSchedulesByUserId(Pageable pageable, String userId);
    Page<TravelSchedule> findSharedTravelSchedulesByUserId(Pageable pageable, String userId);
    Integer countTravelSchedulesByUserId(String userId);
    Integer countSharedTravelSchedulesByUserId(String userId);
    Page<TravelSchedule> searchTravelSchedulesByUserIdAndKeyword(Pageable pageable, String keyword, String userId);
    Integer countTravelSchedulesByUserIdAndKeyword(String keyword, String userId);
    Page<TravelSchedule> searchSharedTravelSchedulesByUserIdAndKeyword(Pageable pageable, String keyword, String userId);
    Integer countSharedTravelSchedulesByUserIdAndKeyword(String keyword, String userId);
    Page<TravelSchedule> findEnableEditTravelSchedulesByUserId(Pageable pageable, String userId);
    Integer countEnableEditTravelSchedulesByUserId(String userId);
}
