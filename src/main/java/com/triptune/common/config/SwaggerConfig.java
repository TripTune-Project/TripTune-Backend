package com.triptune.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("TripTune 프로젝트 API")
                        .description("같이 여행 계획을 작성하는 웹 프로젝트")
                        .version("1.0.0"));

    }
}
