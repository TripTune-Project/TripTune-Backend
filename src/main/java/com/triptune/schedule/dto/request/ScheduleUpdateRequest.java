package com.triptune.schedule.dto.request;

import com.triptune.global.validation.ValidScheduleDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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


    @NotNull(message = "일정 버전은 필수 입력 값입니다.")
    @Min(value = 0, message = "일정 버전은 0 이상이어야 합니다.")
    private Integer scheduleVersion;


    @Builder
    public ScheduleUpdateRequest(String scheduleName, LocalDate startDate, LocalDate endDate, Integer scheduleVersion) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.scheduleVersion = scheduleVersion;
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
