package com.triptune.domain.member.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id")
    private Long profileImageId;

    @Column(name = "s3_object_url")
    private String s3ObjectUrl;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private double fileSize;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Builder
    public ProfileImage(Long profileImageId, String s3ObjectUrl, String originalName, String fileName, String fileType, double fileSize, LocalDateTime createdAt) {
        this.profileImageId = profileImageId;
        this.s3ObjectUrl = s3ObjectUrl;
        this.originalName = originalName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }
}
