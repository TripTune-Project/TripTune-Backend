package com.triptune.global.aop;

import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@RequiredArgsConstructor
@Component
public class ScheduleCheckAspect {

    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final HttpServletRequest httpServletRequest;

    @Around("@annotation(ScheduleCheck)")
    public Object authorCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        Long scheduleId = extractScheduleIdFromPath(httpServletRequest.getRequestURI());

        if(!isExistSchedule(scheduleId)){
            throw new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        return joinPoint.proceed();
    }


    private Long extractScheduleIdFromPath(String path){
        return Long.parseLong(path.split("/")[3]);
    }

    private boolean isExistSchedule(Long scheduleId){
        return travelScheduleRepository.existsById(scheduleId);
    }

}
