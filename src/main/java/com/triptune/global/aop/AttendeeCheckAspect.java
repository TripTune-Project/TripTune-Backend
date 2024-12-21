package com.triptune.global.aop;

import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Aspect
@RequiredArgsConstructor
@Component
public class AttendeeCheckAspect {

    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final HttpServletRequest httpServletRequest;

    @Around("@annotation(AttendeeCheck)")
    public Object attendeeCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long scheduleId = extractScheduleIdFromPath(httpServletRequest.getRequestURI());

        if (!isExistSchedule(scheduleId)){
            throw new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        if (!isAttendee(scheduleId, userId)){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE);
        }

        return joinPoint.proceed();
    }


    private Long extractScheduleIdFromPath(String path) {
        return  Long.parseLong(path.split("/")[3]);
    }

    private boolean isExistSchedule(Long scheduleId){
        return travelScheduleRepository.existsById(scheduleId);
    }

    private boolean isAttendee(Long scheduleId, String userId){
        return travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId);
    }
}
