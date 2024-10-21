package com.triptune.domain.schedule.repository;

import com.triptune.domain.travel.entity.TravelImage;

import java.util.List;

public interface TravelRouteCustomRepository {
    List<TravelImage> findPlaceImagesOfFirstRoute(Long scheduleId);
}
