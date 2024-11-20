package com.triptune.global.config;

import com.triptune.global.converter.KSTConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Collections;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions(KSTConverter kstConverter){
        return new MongoCustomConversions(Collections.singletonList(kstConverter));
    }
}
