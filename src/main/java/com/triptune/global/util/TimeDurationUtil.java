package com.triptune.global.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeDurationUtil {
    public static String timeDuration(LocalDateTime updateTime){
        String result = "지금";

        LocalDateTime currentTime = LocalDateTime.now();

        Duration duration = Duration.between(updateTime, currentTime);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if(days > 7){
            result = updateTime.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));
        } else if (days > 0){
            result = days + "일 전";
        } else if (hours > 0){
            result = hours + "시간 전";
        } else if (minutes > 0){
            result = minutes + "분 전";
        }

        return result;
    }
}
