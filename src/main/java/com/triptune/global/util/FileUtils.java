package com.triptune.global.util;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.FileBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
public class FileUtils {

    public static String getExtension(String fileName){
        int dotIndex = fileName.lastIndexOf(".");
        System.out.println(dotIndex);

        if(dotIndex > 0 && dotIndex < fileName.length() - 1){
            return fileName.substring(dotIndex + 1);
        } else {
            throw new FileBadRequestException(ErrorCode.INVALID_EXTENSION);
        }
    }

    public static boolean isValidExtension(MultipartFile file){
        List<String> validTypes = List.of("image/jpeg", "image/jpg", "image/png");

        try {
            String mimeType = new Tika().detect(file.getInputStream());

            return validTypes.stream()
                    .anyMatch(type -> type.equalsIgnoreCase(mimeType));

        } catch (IOException e) {
            log.error("isValidException 파일 MIME 타입 검사 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    public static ObjectMetadata generateMetadata(MultipartFile uploadFile){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(uploadFile.getSize());
        metadata.setContentType(uploadFile.getContentType());

        return metadata;
    }
}
