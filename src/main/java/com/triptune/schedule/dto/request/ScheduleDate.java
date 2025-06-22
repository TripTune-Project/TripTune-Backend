package com.triptune.schedule.dto.request;

import java.time.LocalDate;

public interface ScheduleDate {
    LocalDate getStartDate();
    LocalDate getEndDate();
}
