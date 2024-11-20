package com.triptune.global.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@ReadingConverter
public class KSTConverter implements Converter<Date, LocalDateTime> {

    private static final String TIME_DEFAULT = "Asia/Seoul";

    @Override
    public LocalDateTime convert(Date source) {
        return source.toInstant()
                .atZone(ZoneId.of(TIME_DEFAULT))
                .toLocalDateTime();
    }
}
