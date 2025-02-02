package com.triptune.domain.profile.entity;

import com.triptune.domain.member.entity.Member;
import com.triptune.global.properties.DefaultProfileImageProperties;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id")
    private Long profileImageId;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "s3_object_url")
    private String s3ObjectUrl;

    @Column(name = "s3_file_key")
    private String s3FileKey;

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

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Builder
    public ProfileImage(Long profileImageId, Member member, String s3ObjectUrl, String s3FileKey, String originalName, String fileName, String fileType, double fileSize, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.profileImageId = profileImageId;
        this.member = member;
        this.s3ObjectUrl = s3ObjectUrl;
        this.s3FileKey = s3FileKey;
        this.originalName = originalName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProfileImage from(DefaultProfileImageProperties imageProperties){
        return ProfileImage.builder()
                .s3ObjectUrl(imageProperties.getS3ObjectUrl())
                .s3FileKey(imageProperties.getS3FileKey())
                .originalName(imageProperties.getOriginalName())
                .fileName(imageProperties.getFileName())
                .fileType(imageProperties.getExtension())
                .fileSize(imageProperties.getSize())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void update(MultipartFile profileImageFile, String s3ObjectUrl, String s3FileKey, String savedFileName, String extension){
        this.s3ObjectUrl = s3ObjectUrl;
        this.s3FileKey = s3FileKey;
        this.originalName = profileImageFile.getOriginalFilename();
        this.fileName = savedFileName;
        this.fileType = extension;
        this.fileSize = profileImageFile.getSize();
        this.updatedAt = LocalDateTime.now();
    }


}
