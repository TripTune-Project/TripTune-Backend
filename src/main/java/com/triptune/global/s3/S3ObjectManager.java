package com.triptune.global.s3;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3ObjectManager {

    private final S3Properties s3Properties;


    public String generateS3ObjectUrl(@Nullable String s3ObjectKey) {
        return s3ObjectKey == null
                ? null
                : s3Properties.baseUrl() + "/" + s3ObjectKey;
    }

    public String generateS3FileName(String fileTag, String extension){
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
                + "_" + fileTag
                + "_" + uuid
                + "." + extension;
    }


    public String generateS3ObjectKey(String dir, String savedFileName){
        return dir + "/" + savedFileName;
    }
}
