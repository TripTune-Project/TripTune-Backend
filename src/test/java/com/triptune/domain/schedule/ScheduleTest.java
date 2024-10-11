package com.triptune.domain.schedule;

import com.triptune.domain.BaseTest;
import com.triptune.domain.schedule.dto.CreateScheduleRequest;

import java.time.LocalDate;

public abstract class ScheduleTest extends BaseTest {
    protected CreateScheduleRequest createScheduleRequest() {
        return CreateScheduleRequest.builder()
                .scheduleName("테스트")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .build();
    }
}
