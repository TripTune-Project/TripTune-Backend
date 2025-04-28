package com.triptune.schedule.repository;

import com.triptune.schedule.entity.TravelSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelScheduleRepositoryCustom {
    Page<TravelSchedule> findTravelSchedulesByMemberId(Pageable pageable, Long memberId);
    Page<TravelSchedule> findSharedTravelSchedulesByMemberId(Pageable pageable, Long memberId);
    Integer countTravelSchedulesByMemberId(Long memberId);
    Integer countSharedTravelSchedulesByMemberId(Long memberId);
    Page<TravelSchedule> searchTravelSchedulesByMemberIdAndKeyword(Pageable pageable, String keyword, Long memberId);
    Integer countTravelSchedulesByMemberIdAndKeyword(String keyword, Long memberId);
    Page<TravelSchedule> searchSharedTravelSchedulesByMemberIdAndKeyword(Pageable pageable, String keyword, Long memberId);
    Integer countSharedTravelSchedulesByMemberIdAndKeyword(String keyword, Long memberId);
    Page<TravelSchedule> findEnableEditTravelSchedulesByMemberId(Pageable pageable, Long memberId);
    Integer countEnableEditTravelSchedulesByMemberId(Long memberId);
}
