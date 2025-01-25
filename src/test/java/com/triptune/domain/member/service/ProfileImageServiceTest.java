package com.triptune.domain.member.service;

import com.triptune.domain.common.service.S3Service;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.ProfileImageRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ProfileImageServiceTest {
    private static final long DEFAULT_PROFILE_IMAGE_SIZE = 14914;
    private static final String DEFAULT_PROFILE_IMAGE_NAME = "defaultProfileImage.png";

    @Autowired
    private ProfileImageService profileImageService;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private ProfileImageRepository profileImageRepository;


    @Test
    @DisplayName("프로필 이미지 업로드")
    void saveDefaultProfileImage(){
        // given, when
        ProfileImage response = profileImageService.saveDefaultProfileImage();

        // then
        assertThat(response.getOriginalName()).isEqualTo(DEFAULT_PROFILE_IMAGE_NAME);
        assertThat(response.getFileSize()).isEqualTo(DEFAULT_PROFILE_IMAGE_SIZE);
    }

    @Test
    @DisplayName("기본 프로필 이미지 파일 가져오기")
    void getDefaultProfileImageFile(){
        // given, when
        File response = profileImageService.getDefaultProfileImageFile();

        // then
        assertThat(response.length()).isEqualTo(DEFAULT_PROFILE_IMAGE_SIZE);
        assertThat(response.getName()).isEqualTo(DEFAULT_PROFILE_IMAGE_NAME);
    }


}