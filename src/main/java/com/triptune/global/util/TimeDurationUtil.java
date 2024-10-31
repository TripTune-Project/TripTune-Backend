package com.triptune.global.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeDurationUtil {
    private static final int MAX_DAYS = 30;
    private static final String DATE_PATTERN = "yyyy년 M월 d일";

    public static String timeDuration(LocalDateTime updateTime){
        LocalDateTime currentTime = LocalDateTime.now();
        Duration duration = Duration.between(updateTime, currentTime);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if(days > MAX_DAYS){
            return formatFullDate(updateTime);
        } else if (days > 0){
            return formatDaysAgo(days);
        } else if (hours > 0){
            return formatHoursAgo(hours);
        } else if (minutes > 0){
            return formatMinutesAgo(minutes);
        }

        return "지금";
    }

    private static String formatFullDate(LocalDateTime date){
        return date.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    private static String formatDaysAgo(long days){
        return days + "일 전";
    }

    private static String formatHoursAgo(long hours){
        return hours + "시간 전";
    }

    private static String formatMinutesAgo(long minutes){
        return minutes + "분 전";
    }
}
