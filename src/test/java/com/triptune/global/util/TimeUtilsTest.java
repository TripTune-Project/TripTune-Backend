package com.triptune.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeUtilsTest {

    @Test
    @DisplayName("마지막 업데이트가 3달 전인 경우")
    void timeDuration3Months(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusMonths(3);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo(updateTime.getYear() + "년 " + updateTime.getMonthValue() + "월 " + updateTime.getDayOfMonth() + "일");

    }

    @Test
    @DisplayName("마지막 업데이트가 200년 전인 경우")
    void timeDuration200Years(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusYears(200);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo(updateTime.getYear() + "년 " + updateTime.getMonthValue() + "월 " + updateTime.getDayOfMonth() + "일");

    }

    @Test
    @DisplayName("마지막 업데이트가 29일 전 경우")
    void timeDuration29Days(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusDays(29);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("29일 전");
    }


    @Test
    @DisplayName("마지막 업데이트가 31일 전인 경우")
    void timeDuration31Days(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusDays(31);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo(updateTime.getYear() + "년 " + updateTime.getMonthValue() + "월 " + updateTime.getDayOfMonth() + "일");

    }


    @Test
    @DisplayName("마지막 업데이트가 5시간 전인 경우")
    void timeDuration5Hours(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusHours(5);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("5시간 전");

    }

    @Test
    @DisplayName("마지막 업데이트가 4일 5시간 전인 경우")
    void timeDuration4Days5Hours(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusDays(4).minusHours(5);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("4일 전");

    }

    @Test
    @DisplayName("마지막 업데이트가 5분 전인 경우")
    void timeDuration5Minutes(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusMinutes(5);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("5분 전");

    }


    @Test
    @DisplayName("마지막 업데이트가 2시간 5분 전인 경우")
    void timeDuration2Hours5Minutes(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusHours(2).minusMinutes(5);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("2시간 전");

    }

    @Test
    @DisplayName("마지막 업데이트가 10일 2시간 5분 전인 경우")
    void timeDuration10Days2Hours5Minutes(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusDays(10).minusHours(2).minusMinutes(5);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("10일 전");

    }

    @Test
    @DisplayName("마지막 업데이트가 8초 전인 경우")
    void timeDuration8Seconds(){
        // given
        LocalDateTime updateTime = LocalDateTime.now().minusSeconds(8);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("지금");

    }

    @Test
    @DisplayName("마지막 업데이트가 3년 20일 53분 8초 전인 경우")
    void timeDuration3Years20Days53Minutes8Seconds(){
        // given
        LocalDateTime updateTime = LocalDateTime.now()
                .minusYears(3)
                .minusDays(20)
                .minusMinutes(53)
                .minusSeconds(8);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo(updateTime.getYear() + "년 " + updateTime.getMonthValue() + "월 " + updateTime.getDayOfMonth() + "일");

    }

    @Test
    @DisplayName("마지막 업데이트가 20일 53분 8초 전인 경우")
    void timeDuration20Days53Minutes8Seconds(){
        // given
        LocalDateTime updateTime = LocalDateTime.now()
                .minusDays(20)
                .minusMinutes(53)
                .minusSeconds(8);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("20일 전");
    }

    @Test
    @DisplayName("마지막 업데이트가 5일 9시간 53분 8초 전인 경우")
    void timeDuration5Days9Hours53Minutes8Seconds(){
        // given
        LocalDateTime updateTime = LocalDateTime.now()
                .minusDays(5)
                .minusHours(9)
                .minusMinutes(53)
                .minusSeconds(8);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("5일 전");

    }

    @Test
    @DisplayName("마지막 업데이트가 23시간 53분 8초 전인 경우")
    void timeDuration23Hours53Minutes8Seconds(){
        // given
        LocalDateTime updateTime = LocalDateTime.now()
                .minusHours(23)
                .minusMinutes(53)
                .minusSeconds(8);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("23시간 전");

    }

    @Test
    @DisplayName("마지막 업데이트가 1분 8초 전인 경우")
    void timeDuration1Minutes8Seconds(){
        // given
        LocalDateTime updateTime = LocalDateTime.now()
                .minusMinutes(1)
                .minusSeconds(8);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("1분 전");

    }

    @Test
    @DisplayName("마지막 업데이트가 24시간 전인 경우")
    void timeDuration24Hours(){
        // given
        LocalDateTime updateTime = LocalDateTime.now()
                .minusHours(24);

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("1일 전");

    }

    @Test
    @DisplayName("마지막 업데이트가 0초 전인 경우")
    void timeDuration1Minutes0Seconds(){
        // given
        LocalDateTime updateTime = LocalDateTime.now();

        // when
        String response = TimeUtils.timeDuration(updateTime);

        // then
        assertThat(response).isEqualTo("지금");

    }

    @Test
    @DisplayName("한국 시간으로 변경")
    void convertToKST(){
        // given
        LocalDateTime testTime = LocalDateTime.now().minusHours(9);

        // when
        LocalDateTime response = TimeUtils.convertToKST(testTime);

        // then
        assertThat(response).isEqualTo(testTime.plusHours(9));
    }

}
