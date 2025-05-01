package com.triptune.global.service;

import com.triptune.global.s3.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @InjectMocks
    private S3Service s3Service;


    @Test
    @DisplayName("s3 저장용 이미지 이름 생성")
    void generateS3FileName(){
        // given
        String fileTag = "TEST";
        String extension = "jpg";

        // when
        String response = s3Service.generateS3FileName(fileTag, extension);

        // then
        System.out.println(response);
        assertThat(response.contains("_"+ fileTag + "_")).isTrue();
        assertThat(response.contains("." + extension)).isTrue();
    }

}