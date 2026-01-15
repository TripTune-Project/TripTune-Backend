package com.triptune.schedule.entity;

import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "travelSchedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelAttendee> travelAttendees = new ArrayList<>();

    @OneToMany(mappedBy = "travelSchedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelRoute> travelRoutes = new ArrayList<>();

    private TravelSchedule(String scheduleName, LocalDate startDate, LocalDate endDate) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static TravelSchedule createTravelSchedule(String scheduleName, LocalDate startDate, LocalDate endDate) {
        return new TravelSchedule(
                scheduleName,
                startDate,
                endDate
        );
    }

    public void updateSchedule(ScheduleUpdateRequest request) {
        this.scheduleName = request.getScheduleName();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
    }

    public void removeTravelRoutes(TravelRoute travelRoute){
        travelRoutes.remove(travelRoute);
        travelRoute.detachTravelSchedule();
    }

    public void clearTravelRoutes() {
        for (TravelRoute travelRoute : new ArrayList<>(travelRoutes)) {
            removeTravelRoutes(travelRoute);
        }
    }

    public void addTravelRoutes(TravelRoute travelRoute) {
        travelRoutes.add(travelRoute);
    }

    public void addTravelAttendees(TravelAttendee travelAttendee) {
        travelAttendees.add(travelAttendee);
    }
}
