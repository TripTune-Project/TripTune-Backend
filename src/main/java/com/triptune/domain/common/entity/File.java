package com.triptune.domain.common.entity;

import com.triptune.domain.travel.entity.TravelImage;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class File {

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
    private double fileSize;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_thumbnail")
    private boolean isThumbnail;

    @Column(name = "api_file_url")
    private String apiFileUrl;

    @OneToMany(mappedBy = "file", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TravelImage> travelImageFileList;

    @Builder
    public File(Long fileId, String s3ObjectUrl, String originalName, String fileName, String fileType, double fileSize, LocalDateTime createdAt, boolean isThumbnail, String apiFileUrl, List<TravelImage> travelImageFileList) {
        this.fileId = fileId;
        this.s3ObjectUrl = s3ObjectUrl;
        this.originalName = originalName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
        this.isThumbnail = isThumbnail;
        this.apiFileUrl = apiFileUrl;
        this.travelImageFileList = travelImageFileList;
    }

    public static String getThumbnailUrl(List<TravelImage> imageFile){
        return imageFile.stream()
                .map(TravelImage::getFile)
                .filter(File::isThumbnail)
                .map(File::getS3ObjectUrl)
                .findFirst()
                .orElse(null);

    }
}
