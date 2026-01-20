package com.triptune.profile.service;

import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.exception.FileBadRequestException;
import com.triptune.profile.properties.DefaultProfileImageProperties;
import com.triptune.global.s3.S3Service;
import com.triptune.global.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@EnableConfigurationProperties(DefaultProfileImageProperties.class)
public class ProfileImageService {
    private static final String FILE_TAG = "profileImage";

    private final DefaultProfileImageProperties profileImageProperties;
    private final ProfileImageRepository profileImageRepository;
    private final S3Service s3Service;

    @Transactional
    public ProfileImage saveDefaultProfileImage() {
        ProfileImage profileImage = ProfileImage.createProfileImage(
                profileImageProperties.getS3ObjectUrl(),
                profileImageProperties.getS3ObjectKey(),
                profileImageProperties.getOriginalName(),
                profileImageProperties.getFileName(),
                profileImageProperties.getExtension(),
                profileImageProperties.getSize()
        );

        return profileImageRepository.save(profileImage);
    }

    @Transactional
    public void updateProfileImage(Long memberId, MultipartFile profileImageFile) {
        validateFileExtension(profileImageFile);

        ProfileImage profileImage = getProfileImageByMemberId(memberId);
        deleteS3File(profileImage);

        String extension = FileUtils.getExtension(profileImageFile.getOriginalFilename());
        String savedFileName = s3Service.generateS3FileName(FILE_TAG, extension);
        String s3FileKey = s3Service.generateS3FileKey(savedFileName);
        String s3ObjectUrl = s3Service.uploadToS3(profileImageFile, s3FileKey);

        profileImage.updateProfileImage(profileImageFile, s3ObjectUrl, s3FileKey, savedFileName, extension);
    }

    private void validateFileExtension(MultipartFile profileImageFile){
        if(!FileUtils.isValidExtension(profileImageFile)){
            throw new FileBadRequestException(ErrorCode.INVALID_EXTENSION);
        }
    }

    private ProfileImage getProfileImageByMemberId(Long memberId){
        return profileImageRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PROFILE_IMAGE_NOT_FOUND));
    }

    @Transactional
    public void updateDefaultProfileImage(Member member) {
        ProfileImage profileImage = member.getProfileImage();

        deleteS3File(profileImage);
        profileImage.updateDefaultProfileImage(profileImageProperties);
    }

    public void deleteS3File(ProfileImage profileImage){
        if (!profileImage.isDefaultImage(profileImageProperties.getS3ObjectKey())){
            s3Service.deleteS3File(profileImage.getS3ObjectKey());
        }
    }
}
