package com.triptune.schedule.dto.request;

import com.triptune.global.validation.ValidScheduleDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@ValidScheduleDate
public class ScheduleUpdateRequest implements ScheduleDate{

    @NotBlank(message = "일정 이름은 필수 입력 값입니다.")
    private String scheduleName;

    @NotNull(message = "일정 시작 날짜는 필수 입력 값입니다.")
    private LocalDate startDate;

    @NotNull(message = "일정 종료 날짜는 필수 입력 값입니다.")
    private LocalDate endDate;

    @Valid
    private List<RouteRequest> travelRoutes = new ArrayList<>();

    @Builder
    public ScheduleUpdateRequest(String scheduleName, LocalDate startDate, LocalDate endDate, List<RouteRequest> travelRoutes) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.travelRoutes = (travelRoutes != null) ? travelRoutes : new ArrayList<>();
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }
}
