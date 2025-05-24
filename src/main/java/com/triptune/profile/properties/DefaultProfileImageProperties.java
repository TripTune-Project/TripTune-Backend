package com.triptune.profile.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@ConfigurationProperties(prefix = "app.backend.default-profile")
public class DefaultProfileImageProperties {
    private String s3ObjectUrl;
    private String s3ObjectKey;
    private String originalName;
    private String fileName;
    private String extension;
    private long size;

    public DefaultProfileImageProperties(String s3ObjectUrl, String s3ObjectKey, String originalName, String fileName, String extension, long size) {
        this.s3ObjectUrl = s3ObjectUrl;
        this.s3ObjectKey = s3ObjectKey;
        this.originalName = originalName;
        this.fileName = fileName;
        this.extension = extension;
        this.size = size;
    }
}
