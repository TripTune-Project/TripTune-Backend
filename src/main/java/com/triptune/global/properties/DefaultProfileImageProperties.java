package com.triptune.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "default-profile")
public class DefaultProfileImageProperties {
    private String s3ObjectUrl;
    private String originalName;
    private String fileName;
    private String extension;
    private long size;


}
