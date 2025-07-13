package com.triptune.schedule.dto.request;

import com.triptune.global.validation.ValidScheduleDate;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@ValidScheduleDate
public class ScheduleCreateRequest implements ScheduleDate{
    @NotBlank(message = "일정 이름은 필수 입력 값입니다.")
    private String scheduleName;

    @NotNull(message = "일정 시작 날짜는 필수 입력 값입니다.")
    @FutureOrPresent(message = "오늘 이후 날짜만 입력 가능합니다.")
    private LocalDate startDate;

    @NotNull(message = "일정 종료 날짜는 필수 입력 값입니다.")
    @FutureOrPresent(message = "오늘 이후 날짜만 입력 가능합니다.")
    private LocalDate endDate;

    @Builder
    public ScheduleCreateRequest(String scheduleName, LocalDate startDate, LocalDate endDate) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
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
