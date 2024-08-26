package com.triptune.domain.common.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Files {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "s3_object_url")
    private String s3ObjectUrl;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Double fileSize;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_thumbnail")
    private Integer isThumbnail;

    @Column(name = "api_file_url")
    private String apiFileUrl;

    @Builder
    public Files(Long fileId, String s3ObjectUrl, String originalName, String fileName, String fileType, Double fileSize, LocalDateTime createdAt, Integer isThumbnail, String apiFileUrl) {
        this.fileId = fileId;
        this.s3ObjectUrl = s3ObjectUrl;
        this.originalName = originalName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
        this.isThumbnail = isThumbnail;
        this.apiFileUrl = apiFileUrl;
    }
}
