package com.triptune.domain.member.service;

import com.triptune.domain.common.service.S3Service;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.ProfileImageRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.FileUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {
    private static final String PROFILE_S3_DIR = "img/profile/";
    private static final String PROFILE_IMAGE_TAG = "profileImage";

    @Value("${default-profile.image-name}")
    private String profileImageName;

    @Value("${default-profile.image-path}")
    private String profileImagePath;

    private final S3Service s3Service;
    private final ProfileImageRepository profileImageRepository;

    public ProfileImage saveDefaultProfileImage() {
        File uploadFile = getDefaultProfileImageFile();
        String extension = FileUtil.getExtension(profileImageName);

        String saveS3FileName = s3Service.generateS3FileName(PROFILE_IMAGE_TAG, extension);
        String saveS3FilePath = PROFILE_S3_DIR + saveS3FileName;

        String s3ObjectUrl = s3Service.uploadToS3(uploadFile, saveS3FilePath);
        ProfileImage profileImage = ProfileImage.of(s3ObjectUrl, profileImageName, saveS3FileName, extension, uploadFile.length());

        return profileImageRepository.save(profileImage);
    }

    public File getDefaultProfileImageFile(){
        try{
            return new ClassPathResource(profileImagePath + profileImageName).getFile();
        } catch (IOException e){
            log.error("기본 프로필 이미지 파일을 읽는데 실패: {}", profileImagePath + profileImageName, e);
            throw new DataNotFoundException(ErrorCode.PROFILE_IMAGE_NOT_FOUND);
        }
    }
}
