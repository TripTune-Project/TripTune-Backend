package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelScheduleRepositoryCustom {
    Page<TravelSchedule> findTravelSchedulesByEmail(Pageable pageable, String email);
    Page<TravelSchedule> findSharedTravelSchedulesByEmail(Pageable pageable, String email);
    Integer countTravelSchedulesByEmail(String email);
    Integer countSharedTravelSchedulesByEmail(String email);
    Page<TravelSchedule> searchTravelSchedulesByEmailAndKeyword(Pageable pageable, String keyword, String email);
    Integer countTravelSchedulesByEmailAndKeyword(String keyword, String email);
    Page<TravelSchedule> searchSharedTravelSchedulesByEmailAndKeyword(Pageable pageable, String keyword, String email);
    Integer countSharedTravelSchedulesByEmailAndKeyword(String keyword, String email);
    Page<TravelSchedule> findEnableEditTravelSchedulesByEmail(Pageable pageable, String email);
    Integer countEnableEditTravelSchedulesByEmail(String email);
}
