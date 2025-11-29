package com.triptune.schedule.entity;

import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
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

    @Builder
    public TravelSchedule(Long scheduleId, String scheduleName, LocalDate startDate, LocalDate endDate, List<TravelAttendee> travelAttendees, List<TravelRoute> travelRoutes) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.travelAttendees = (travelAttendees != null) ? travelAttendees : new ArrayList<>();
        this.travelRoutes = (travelRoutes != null) ? travelRoutes : new ArrayList<>();
    }

    public static TravelSchedule from(ScheduleCreateRequest request){
        return TravelSchedule.builder()
                .scheduleName(request.getScheduleName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }

    public void updateSchedule(ScheduleUpdateRequest request) {
        this.scheduleName = request.getScheduleName();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
    }

    public void addTravelRoutes(TravelRoute travelRoute) {
        this.travelRoutes.add(travelRoute);
        travelRoute.setTravelSchedule(this);
    }

    public void addTravelAttendee(TravelAttendee travelAttendee) {
        this.travelAttendees.add(travelAttendee);
        travelAttendee.setTravelSchedule(this);
    }
}
