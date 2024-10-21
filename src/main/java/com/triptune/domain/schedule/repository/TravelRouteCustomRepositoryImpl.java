package com.triptune.domain.schedule.repository;

import com.querydsl.jpa.JPQLQueryFactory;
import com.triptune.domain.schedule.entity.QTravelRoute;
import com.triptune.domain.travel.entity.QTravelImage;
import com.triptune.domain.travel.entity.QTravelPlace;
import com.triptune.domain.travel.entity.TravelImage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TravelRouteCustomRepositoryImpl implements TravelRouteCustomRepository{
    private final JPQLQueryFactory jpqlQueryFactory;
    private final QTravelRoute travelRoute;
    private final QTravelPlace travelPlace;
    private final QTravelImage travelImage;

    public TravelRouteCustomRepositoryImpl(JPQLQueryFactory jpqlQueryFactory) {
        this.jpqlQueryFactory = jpqlQueryFactory;
        this.travelImage = QTravelImage.travelImage;
        this.travelRoute = QTravelRoute.travelRoute;
        this.travelPlace = QTravelPlace.travelPlace;
    }


    @Override
    public List<TravelImage> findPlaceImagesOfFirstRoute(Long scheduleId) {
        return jpqlQueryFactory
                .select(travelImage)
                .from(travelRoute)
                .join(travelRoute.travelPlace, travelPlace)
                .join(travelPlace.travelImageList, travelImage)
                .where(travelRoute.travelSchedule.scheduleId.eq(scheduleId)
                        .and(travelRoute.routeOrder.eq(1)))
                .fetch();

    }
}
