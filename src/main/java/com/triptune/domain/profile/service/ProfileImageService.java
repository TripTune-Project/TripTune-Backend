package com.triptune.domain.profile.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.profile.entity.ProfileImage;
import com.triptune.domain.profile.repository.ProfileImageRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.exception.FileBadRequestException;
import com.triptune.global.properties.DefaultProfileImageProperties;
import com.triptune.global.service.S3Service;
import com.triptune.global.util.FileUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.authentication.UserServiceBeanDefinitionParser;
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

        ProfileImage profileImage = findByUserId(userId);
        s3Service.deleteS3File(profileImage.getS3FileKey());

        String extension = FileUtil.getExtension(profileImageFile.getOriginalFilename());
        String savedFileName = s3Service.generateS3FileName(FILE_TAG, extension);
        String s3FileKey = s3Service.generateS3FileKey(savedFileName);
        String s3ObjectUrl = s3Service.uploadToS3(profileImageFile, s3FileKey);

        profileImage.update(profileImageFile, s3ObjectUrl, s3FileKey, savedFileName, extension);
        profileImage.getMember().updateUpdatedAt();
    }

    private void validateFileExtension(MultipartFile profileImageFile){
        if(!FileUtil.isValidExtension(profileImageFile)){
            throw new FileBadRequestException(ErrorCode.INVALID_EXTENSION);
        }
    }

    private ProfileImage findByUserId(String userId){
        return profileImageRepository.findByMember_UserId(userId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PROFILE_IMAGE_NOT_FOUND));
    }
}
