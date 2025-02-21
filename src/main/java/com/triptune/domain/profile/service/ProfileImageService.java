package com.triptune.domain.profile.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.profile.entity.ProfileImage;
import com.triptune.domain.profile.repository.ProfileImageRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.exception.FileBadRequestException;
import com.triptune.global.properties.DefaultProfileImageProperties;
import com.triptune.global.service.S3Service;
import com.triptune.global.util.FileUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@EnableConfigurationProperties(DefaultProfileImageProperties.class)
public class ProfileImageService {
    private static final String FILE_TAG = "profileImage";

    private final DefaultProfileImageProperties profileImageProperties;
    private final ProfileImageRepository profileImageRepository;
    private final S3Service s3Service;

    public ProfileImage saveDefaultProfileImage(Member member) {
        ProfileImage profileImage = ProfileImage.from(member, profileImageProperties);
        return profileImageRepository.save(profileImage);
    }

    public void updateProfileImage(String userId, MultipartFile profileImageFile) {
        validateFileExtension(profileImageFile);

        ProfileImage profileImage = getProfileImageByUserId(userId);
        deleteS3File(profileImage);

        String extension = FileUtils.getExtension(profileImageFile.getOriginalFilename());
        String savedFileName = s3Service.generateS3FileName(FILE_TAG, extension);
        String s3FileKey = s3Service.generateS3FileKey(savedFileName);
        String s3ObjectUrl = s3Service.uploadToS3(profileImageFile, s3FileKey);

        profileImage.updateProfileImage(profileImageFile, s3ObjectUrl, s3FileKey, savedFileName, extension);
        profileImage.getMember().updateUpdatedAt();
    }

    private void validateFileExtension(MultipartFile profileImageFile){
        if(!FileUtils.isValidExtension(profileImageFile)){
            throw new FileBadRequestException(ErrorCode.INVALID_EXTENSION);
        }
    }

    private ProfileImage getProfileImageByUserId(String userId){
        return profileImageRepository.findByMember_UserId(userId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PROFILE_IMAGE_NOT_FOUND));
    }

    public void updateDefaultProfileImage(Member member) {
        ProfileImage profileImage = member.getProfileImage();

        deleteS3File(profileImage);
        profileImage.updateDefaultProfileImage(profileImageProperties);
    }

    public void deleteS3File(ProfileImage profileImage){
        if(!profileImage.getS3FileKey().equals(profileImageProperties.getS3FileKey())){
            s3Service.deleteS3File(profileImage.getS3FileKey());
        }
    }
}
