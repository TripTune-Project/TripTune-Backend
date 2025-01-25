package com.triptune.domain.common.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.swing.text.DateFormatter;
import java.io.File;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public String uploadToS3(File uploadFile, String savedFileName){
        try{
            amazonS3Client.putObject(new PutObjectRequest(bucket, savedFileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));

            log.info("s3 이미지 업로드 성공: {}", savedFileName);
            return amazonS3Client.getUrl(bucket, savedFileName).toString();
        } catch (Exception e){
            log.error("S3 이미지 업로드 실패: {}", savedFileName, e);
            throw new AmazonS3Exception("s3 이미지 업로드 실패");
        }
    }

    public String generateS3FileName(String fileTag, String extension){
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + "_" + fileTag + "_" + uuid + "." + extension;
    }
}
