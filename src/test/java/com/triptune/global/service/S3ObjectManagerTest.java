package com.triptune.global.service;

import com.triptune.global.s3.S3ObjectManager;
import com.triptune.global.s3.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class S3ObjectManagerTest {

    @InjectMocks
    private S3ObjectManager s3ObjectManager;


    @Test
    @DisplayName("s3 저장용 이미지 이름 생성")
    void generateS3FileName(){
        // given
        String fileTag = "TEST";
        String extension = "jpg";

        // when
        String response = s3ObjectManager.generateS3FileName(fileTag, extension);

        // then
        log.info("response={}", response);
        assertThat(response.contains("_"+ fileTag + "_")).isTrue();
        assertThat(response.contains("." + extension)).isTrue();
    }

}