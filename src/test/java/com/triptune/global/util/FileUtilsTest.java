package com.triptune.global.util;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.FileBadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockMultipartFile;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUtilsTest {

    @ParameterizedTest
    @CsvSource({"test.jpg", "/test.jpg", "/test/image.jpg"})
    @DisplayName("파일 확장자 추출")
    void getExtension(String fileName){
        // given, when
        String response = FileUtils.getExtension(fileName);

        // then
        assertThat(response).isEqualTo("jpg");
    }

    @ParameterizedTest
    @CsvSource({".jpg", "/test.", "./testjpg"})
    @DisplayName("파일 확장자 추출 실패로 예외 발생")
    void getExtensionException(String fileName){
        // given, when
        FileBadRequestException fail = assertThrows(FileBadRequestException.class,
                () -> FileUtils.getExtension(fileName));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INVALID_EXTENSION.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INVALID_EXTENSION.getMessage());
    }

    @ParameterizedTest
    @MethodSource("createCorrectMultipartFiles")
    @DisplayName("파일 메타데이터 생성")
    void generateMetadata(MockMultipartFile file){
        // given, when
        ObjectMetadata response = FileUtils.generateMetadata(file);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContentLength()).isEqualTo(file.getSize());
    }

    static Stream<Arguments> createCorrectMultipartFiles(){
        return Stream.of(
                Arguments.of(new MockMultipartFile("file1", "test1.jpg", "image/jpg", new byte[0])),
                Arguments.of(new MockMultipartFile("file2", "test2.jpeg", "image/jpeg", new byte[0])),
                Arguments.of(new MockMultipartFile("file3", "test3.png", "image/png", new byte[0]))
        );
    }

}