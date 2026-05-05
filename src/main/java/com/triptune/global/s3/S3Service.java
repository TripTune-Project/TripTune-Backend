package com.triptune.global.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.triptune.global.exception.FileDeleteException;
import com.triptune.global.exception.FileUploadException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3Client amazonS3Client;
    private final S3Properties s3Properties;

    public void uploadToS3(MultipartFile uploadFile, String s3ObjectKey){
        ObjectMetadata metadata = FileUtils.generateMetadata(uploadFile);

        try{
            amazonS3Client.putObject(new PutObjectRequest(
                    s3Properties.bucket(),
                    s3ObjectKey,
                    uploadFile.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead));

            log.info("[s3 이미지 업로드 성공] : {}", s3ObjectKey);

        } catch (Exception e){
            log.error("[S3 이미지 업로드 실패] : {}", s3ObjectKey, e);
            throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }


    public void deleteS3File(String s3ObjectKey) {
        try{
            amazonS3Client.deleteObject(s3Properties.bucket(), s3ObjectKey);
            log.info("[S3 이미지 삭제 성공] : {}", s3ObjectKey);
        } catch(Exception e){
            log.error("[S3 이미지 삭제 실패] : {}", s3ObjectKey, e);
            throw new FileDeleteException(ErrorCode.FILE_DELETE_FAIL);
        }

    }

}
