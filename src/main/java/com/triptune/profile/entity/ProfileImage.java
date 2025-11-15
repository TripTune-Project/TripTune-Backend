package com.triptune.profile.entity;

import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.member.entity.Member;
import com.triptune.profile.properties.DefaultProfileImageProperties;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ProfileImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id")
    private Long profileImageId;

    @OneToOne(mappedBy = "profileImage", fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "s3_object_url")
    private String s3ObjectUrl;

    @Column(name = "s3_object_key")
    private String s3ObjectKey;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private double fileSize;


    @Builder
    public ProfileImage(Long profileImageId, Member member, String s3ObjectUrl, String s3ObjectKey, String originalName, String fileName, String fileType, double fileSize) {
        this.profileImageId = profileImageId;
        this.member = member;
        this.s3ObjectUrl = s3ObjectUrl;
        this.s3ObjectKey = s3ObjectKey;
        this.originalName = originalName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public static ProfileImage from(DefaultProfileImageProperties imageProperties){
        return ProfileImage.builder()
                .s3ObjectUrl(imageProperties.getS3ObjectUrl())
                .s3ObjectKey(imageProperties.getS3ObjectKey())
                .originalName(imageProperties.getOriginalName())
                .fileName(imageProperties.getFileName())
                .fileType(imageProperties.getExtension())
                .fileSize(imageProperties.getSize())
                .build();
    }

    public void updateProfileImage(MultipartFile profileImageFile, String s3ObjectUrl, String s3FileKey, String savedFileName, String extension){
        this.s3ObjectUrl = s3ObjectUrl;
        this.s3ObjectKey = s3FileKey;
        this.originalName = profileImageFile.getOriginalFilename();
        this.fileName = savedFileName;
        this.fileType = extension;
        this.fileSize = profileImageFile.getSize();
    }

    public void updateDefaultProfileImage(DefaultProfileImageProperties imageProperties) {
        this.s3ObjectUrl = imageProperties.getS3ObjectUrl();
        this.s3ObjectKey = imageProperties.getS3ObjectKey();
        this.originalName = imageProperties.getOriginalName();
        this.fileName = imageProperties.getFileName();
        this.fileType = imageProperties.getExtension();
        this.fileSize = imageProperties.getSize();
    }

}
