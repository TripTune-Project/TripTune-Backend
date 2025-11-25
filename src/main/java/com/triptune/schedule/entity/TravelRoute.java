package com.triptune.schedule.entity;

import com.triptune.common.entity.BaseCreatedEntity;
import com.triptune.travel.entity.TravelPlace;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelRoute extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long routeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private TravelSchedule travelSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private TravelPlace travelPlace;

    @Column(name = "route_order")
    private int routeOrder;

    @Builder
    public TravelRoute(Long routeId, TravelSchedule travelSchedule, TravelPlace travelPlace, int routeOrder) {
        this.routeId = routeId;
        this.travelSchedule = travelSchedule;
        this.travelPlace = travelPlace;
        this.routeOrder = routeOrder;
    }

    public TravelRoute(TravelSchedule travelSchedule, TravelPlace travelPlace, int routeOrder) {
        this.travelSchedule = travelSchedule;
        this.travelPlace = travelPlace;
        this.routeOrder = routeOrder;
    }


    public static TravelRoute of(TravelSchedule travelSchedule, TravelPlace travelPlace, int routeOrder){
        return TravelRoute.builder()
                .travelSchedule(travelSchedule)
                .travelPlace(travelPlace)
                .routeOrder(routeOrder)
                .build();
    }

    public void setTravelSchedule(TravelSchedule travelSchedule){
        this.travelSchedule = travelSchedule;
    }
}
