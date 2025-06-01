package com.triptune.global.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app.backend.cors")
public class CorsConfig {
    private List<String> allowedOrigins;

    public List<String> getAllowedOrigins(){
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins){
        this.allowedOrigins = allowedOrigins;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        log.info("[cors 설정 시작]");
        log.info("[allowedOrigins 갯수] : 총 " + allowedOrigins.size() + "개");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 적용
        return source;
    }
}
