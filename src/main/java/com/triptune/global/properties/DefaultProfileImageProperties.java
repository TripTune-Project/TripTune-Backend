package com.triptune.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Getter
@ConfigurationProperties(prefix = "app.backend.default-profile")
public class DefaultProfileImageProperties {
    private String s3ObjectUrl;
    private String s3FileKey;
    private String originalName;
    private String fileName;
    private String extension;
    private long size;

    public DefaultProfileImageProperties(String s3ObjectUrl, String s3FileKey, String originalName, String fileName, String extension, long size) {
        this.s3ObjectUrl = s3ObjectUrl;
        this.s3FileKey = s3FileKey;
        this.originalName = originalName;
        this.fileName = fileName;
        this.extension = extension;
        this.size = size;
    }
}
