package com.triptune.global.util;

import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
public class FileUtil {

    public static String getExtension(String fileName){
        int dotIndex = fileName.lastIndexOf(".");

        if(dotIndex != -1 && dotIndex < fileName.length() - 1){
            return fileName.substring(dotIndex + 1);
        } else {
            throw new DataNotFoundException(ErrorCode.EXTENSION_NOT_FOUND);
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
}
