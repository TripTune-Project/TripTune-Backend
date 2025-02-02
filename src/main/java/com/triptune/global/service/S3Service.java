package com.triptune.global.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
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


    public String uploadToS3(MultipartFile uploadFile, String fileKey){
        try{
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(uploadFile.getSize());
            metadata.setContentType(uploadFile.getContentType());

            amazonS3Client.putObject(new PutObjectRequest(
                    bucket,
                    fileKey,
                    uploadFile.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead));

            log.info("s3 이미지 업로드 성공: {}", fileKey);
            return amazonS3Client.getUrl(bucket, fileKey).toString();
        } catch (Exception e){
            log.error("S3 이미지 업로드 실패: {}", fileKey, e);
            throw new AmazonS3Exception("s3 이미지 업로드 실패");
        }
    }

    public String generateS3FileName(String fileTag, String extension){
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + "_" + fileTag + "_" + uuid + "." + extension;
    }

    public void deleteS3File(String s3FileKey) {
        boolean isObjectExist = amazonS3Client.doesObjectExist(bucket, s3FileKey);

        if(!isObjectExist){
            throw new DataNotFoundException(ErrorCode.PROFILE_IMAGE_NOT_FOUND);
        }

        try{
            amazonS3Client.deleteObject(bucket, s3FileKey);
            log.info("S3 이미지 삭제 성공 : {}", s3FileKey);
        } catch(Exception e){
            log.error("S3 이미지 삭제 실패 : {}", e.getMessage());
            throw new AmazonS3Exception("s3 이미지 업로드 실패");
        }

    }
}
