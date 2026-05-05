package com.triptune.profile.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.backend.default-profile")
public record DefaultProfileImageProperties (
    String s3ObjectKey,
    String originalName,
    String fileName,
    String extension,
    long size
){}
