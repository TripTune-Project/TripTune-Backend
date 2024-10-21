package com.triptune.domain.schedule.repository;

import com.triptune.domain.schedule.entity.TravelSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelScheduleCustomRepository {
    Page<TravelSchedule> findTravelSchedulesByAttendee(Pageable pageable, Long memberId);
    Integer getTotalElementByTravelSchedules(Long memberId);
}
