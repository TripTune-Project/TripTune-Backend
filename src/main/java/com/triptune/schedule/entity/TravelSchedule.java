package com.triptune.schedule.entity;

import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.route.entity.TravelRoute;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "schedule_name")
    private String scheduleName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "schedule_version")
    private Integer scheduleVersion;

    @Column(name = "route_version")
    private Integer routeVersion;

    @OneToMany(mappedBy = "travelSchedule", fetch = FetchType.LAZY)
    private List<TravelAttendee> travelAttendees = new ArrayList<>();

    @OneToMany(mappedBy = "travelSchedule", fetch = FetchType.LAZY)
    private List<TravelRoute> travelRoutes = new ArrayList<>();

    private TravelSchedule(String scheduleName, LocalDate startDate, LocalDate endDate, Integer scheduleVersion, Integer routeVersion) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.scheduleVersion = scheduleVersion;
        this.routeVersion = routeVersion;
    }

    public static TravelSchedule createTravelSchedule(String scheduleName, LocalDate startDate, LocalDate endDate) {
        return new TravelSchedule(
                scheduleName,
                startDate,
                endDate,
                0,
                0
        );
    }

    public void updateSchedule(String scheduleName, LocalDate startDate, LocalDate endDate) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.scheduleVersion++;
    }

    public void addTravelRoutes(TravelRoute travelRoute) {
        travelRoutes.add(travelRoute);
    }

    public void addTravelAttendees(TravelAttendee travelAttendee) {
        travelAttendees.add(travelAttendee);
    }


    public void increaseRouteVersion() {
        this.routeVersion++;
    }

    public boolean isValidScheduleVersion (Integer scheduleVersion) {
        return this.scheduleVersion.equals(scheduleVersion);
    }
}
