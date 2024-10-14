package com.triptune.domain.schedule.entity;

import com.triptune.domain.travel.entity.TravelPlace;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class TravelRoute {

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
}
