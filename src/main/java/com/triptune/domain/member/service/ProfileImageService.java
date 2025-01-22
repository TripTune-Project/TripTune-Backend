package com.triptune.domain.member.service;

import com.triptune.domain.common.service.S3Service;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.ProfileImageRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.parameters.P;
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
    private static final String PROFILE_IMAGE_NAME = "defaultProfileImage.png";
    private static final String PROFILE_IMAGE_PATH = "src/main/resources/static/images/" + PROFILE_IMAGE_NAME;

    private final S3Service s3Service;
    private final ProfileImageRepository profileImageRepository;

    public ProfileImage saveDefaultProfileImage() {
        File uploadFile;

        try{
            uploadFile = new ClassPathResource(PROFILE_IMAGE_PATH).getFile();
        } catch (IOException e){
            log.error("기본 프로필 이미지 파일을 읽는데 실패: {}", PROFILE_IMAGE_PATH, e);
            throw new DataNotFoundException(ErrorCode.PROFILE_IMAGE_NOT_FOUND);
        }

        String fileName = LocalDateTime.now() + "_" + PROFILE_IMAGE_TAG + "_" + UUID.randomUUID();
        String savedFileName = PROFILE_S3_DIR + fileName;

        int dotIndex = fileName.lastIndexOf(".");

        String extension = "";

        if(dotIndex != -1 && dotIndex < fileName.length() - 1){
            extension = fileName.substring(dotIndex + 1);
        } else {
            // TODO : 예외 처리 필요
            System.out.println("확장자 없습니다.");
        }

        String s3ObjectUrl = s3Service.putS3(uploadFile, savedFileName);
        ProfileImage profileImage = ProfileImage.of(s3ObjectUrl, PROFILE_IMAGE_NAME, fileName, extension, uploadFile.length());

        return profileImageRepository.save(profileImage);
    }
}
