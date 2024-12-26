package com.triptune.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "cors")
public class WebConfig implements WebMvcConfigurer {
    private List<String> allowedOrigins;

    public List<String> getAllowedOrigins(){
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins){
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PATCH", "DELETE")
                .allowedHeaders("Authorization", "Content-Type")
                .exposedHeaders("Custom-Header")    // 클라이언트 측 응답에서 노출되는 해더 지정
                .allowCredentials(true);
    }
}
