package com.triptune.domain.schedule.repository;

import com.triptune.domain.schedule.entity.TravelSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelScheduleCustomRepository {
    Page<TravelSchedule> findTravelSchedulesByUserId(Pageable pageable, String userId);
    Page<TravelSchedule> findSharedTravelSchedulesByUserId(Pageable pageable, String userId);
    Integer countTravelSchedulesByUserId(String userId);
    Integer countSharedTravelSchedulesByUserId(String userId);
    Page<TravelSchedule> searchTravelSchedulesByUserIdAndKeyword(Pageable pageable, String keyword, String userId);
    Integer countTravelSchedulesByUserIdAndKeyword(String keyword, String userId);
    Page<TravelSchedule> searchSharedTravelSchedulesByUserIdAndKeyword(Pageable pageable, String keyword, String userId);
    Integer countSharedTravelSchedulesByUserIdAndKeyword(String keyword, String userId);
}
