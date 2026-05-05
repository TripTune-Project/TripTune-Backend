package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelScheduleRepositoryCustom {
    Page<ScheduleInfoQueryDto> findTravelSchedules(Pageable pageable, Long memberId);
    Page<ScheduleInfoQueryDto> findSharedTravelSchedules(Pageable pageable, Long memberId);
    Integer countTravelSchedules(Long memberId);
    Integer countSharedTravelSchedules(Long memberId);
    Page<ScheduleInfoQueryDto> searchTravelSchedules(Pageable pageable, String keyword, Long memberId);
    Page<ScheduleInfoQueryDto> searchSharedTravelSchedules(Pageable pageable, String keyword, Long memberId);
    Page<TravelSchedule> findEnableEditTravelSchedules(Pageable pageable, Long memberId);
}
