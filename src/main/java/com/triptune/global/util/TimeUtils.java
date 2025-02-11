package com.triptune.global.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static final int MAX_DURATION_DAYS = 30;
    private static final int HOURS_IN_A_DAY = 24;
    private static final int MINUTES_IN_AN_HOUR = 60;
    private static final String DATE_PATTERN = "yyyy년 M월 d일";
    private static final ZoneId TIMEZONE_UTC = ZoneId.of("UTC");
    private static final ZoneId TIMEZONE_KST = ZoneId.of("Asia/Seoul");

    public static String timeDuration(LocalDateTime updateTime){
        LocalDateTime currentTime = LocalDateTime.now();
        Duration duration = Duration.between(updateTime, currentTime);

        long days = duration.toDays();
        long hours = duration.toHours() % HOURS_IN_A_DAY;
        long minutes = duration.toMinutes() % MINUTES_IN_AN_HOUR;

        if(days > MAX_DURATION_DAYS){
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


    public static LocalDateTime convertToKST(LocalDateTime source){
        if(source == null){
            return null;
        }

        return source.atZone(TIMEZONE_UTC)
                .withZoneSameInstant(TIMEZONE_KST)
                .toLocalDateTime();
    }
}
