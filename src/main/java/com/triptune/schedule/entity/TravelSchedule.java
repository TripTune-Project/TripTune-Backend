package com.triptune.schedule.entity;

import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TravelSchedule {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "travelSchedule", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<TravelAttendee> travelAttendees = new ArrayList<>();

    @OneToMany(mappedBy = "travelSchedule", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<TravelRoute> travelRoutes = new ArrayList<>();

    @Builder
    public TravelSchedule(Long scheduleId, String scheduleName, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, LocalDateTime updatedAt, List<TravelAttendee> travelAttendees, List<TravelRoute> travelRoutes) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.travelAttendees = (travelAttendees != null) ? travelAttendees : new ArrayList<>();
        this.travelRoutes = (travelRoutes != null) ? travelRoutes : new ArrayList<>();
    }

    public static TravelSchedule from(ScheduleCreateRequest request){
        return TravelSchedule.builder()
                .scheduleName(request.getScheduleName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void set(ScheduleUpdateRequest request) {
        this.scheduleName = request.getScheduleName();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTravelRouteList(List<TravelRoute> travelRouteList){
        this.travelRoutes = travelRouteList;
    }
}
