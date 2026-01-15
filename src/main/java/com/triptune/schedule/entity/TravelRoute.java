package com.triptune.schedule.entity;

import com.triptune.common.entity.BaseCreatedEntity;
import com.triptune.travel.entity.TravelPlace;
import jakarta.persistence.*;
import lombok.AccessLevel;
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


    private TravelRoute(TravelPlace travelPlace, int routeOrder) {
        this.travelPlace = travelPlace;
        this.routeOrder = routeOrder;
    }

    public static TravelRoute createTravelRoute(TravelSchedule travelSchedule, TravelPlace travelPlace, int routeOrder){
        TravelRoute travelRoute = new TravelRoute(
                travelPlace,
                routeOrder
        );
        travelRoute.assignTravelSchedule(travelSchedule);
        return travelRoute;
    }

    public void assignTravelSchedule(TravelSchedule travelSchedule){
        this.travelSchedule = travelSchedule;
        travelSchedule.addTravelRoutes(this);
    }

    protected void detachTravelSchedule(){
        this.travelSchedule = null;
    }



}
