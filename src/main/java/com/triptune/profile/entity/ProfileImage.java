package com.triptune.profile.entity;

import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.member.entity.Member;
import com.triptune.profile.properties.DefaultProfileImageProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id")
    private Long profileImageId;

    @OneToOne(mappedBy = "profileImage", fetch = FetchType.LAZY)
    private Member member;

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


    private ProfileImage(String s3ObjectKey, String originalName, String fileName, String fileType, double fileSize) {
        this.s3ObjectKey = s3ObjectKey;
        this.originalName = originalName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }


    public static ProfileImage createProfileImage(String s3ObjectKey, String originalName, String fileName, String fileType, double fileSize) {
        return new ProfileImage(
                s3ObjectKey,
                originalName,
                fileName,
                fileType,
                fileSize
        );
    }

    public void updateProfileImage(MultipartFile profileImageFile, String s3FileKey, String savedFileName, String extension){
        this.s3ObjectKey = s3FileKey;
        this.originalName = profileImageFile.getOriginalFilename();
        this.fileName = savedFileName;
        this.fileType = extension;
        this.fileSize = profileImageFile.getSize();
    }

    public void updateDefaultProfileImage(DefaultProfileImageProperties imageProperties) {
        this.s3ObjectKey = imageProperties.s3ObjectKey();
        this.originalName = imageProperties.originalName();
        this.fileName = imageProperties.fileName();
        this.fileType = imageProperties.extension();
        this.fileSize = imageProperties.size();
    }

    public void assignMember(Member member){
        this.member = member;
    }

    public boolean isDefaultImage(String s3ObjectKey) {
        return this.s3ObjectKey.equals(s3ObjectKey);
    }
}
