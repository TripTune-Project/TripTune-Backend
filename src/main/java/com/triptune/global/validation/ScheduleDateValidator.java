package com.triptune.global.validation;

import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ScheduleDateValidator implements ConstraintValidator<ValidScheduleDate, ScheduleDate>  {
    @Override
    public boolean isValid(ScheduleDate value, ConstraintValidatorContext constraintValidatorContext) {
        if (value.getStartDate() == null || value.getEndDate() == null){
            return true;
        }
        return !value.getStartDate().isAfter(value.getEndDate());
    }
}
