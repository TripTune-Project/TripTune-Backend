package com.triptune.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleRequest {
    @NotBlank(message = "여행지 이름은 필수 입력 값입니다.")
    private String scheduleName;

    @NotNull(message = "여행지 시작 날짜는 필수 입력 값입니다.")
    private LocalDate startDate;

    @NotNull(message = "여행지 종료 날짜는 필수 입력 값입니다.")
    private LocalDate endDate;

    @Builder
    public ScheduleRequest(String scheduleName, LocalDate startDate, LocalDate endDate) {
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
