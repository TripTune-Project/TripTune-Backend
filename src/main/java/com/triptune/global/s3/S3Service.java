package com.triptune.global.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.FileUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private static final String PROFILE_DIR = "img/profile/";

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public String uploadToS3(MultipartFile uploadFile, String s3FileKey){
        ObjectMetadata metadata = FileUtils.generateMetadata(uploadFile);

        try{
            amazonS3Client.putObject(new PutObjectRequest(
                    bucket,
                    s3FileKey,
                    uploadFile.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead));

            log.info("s3 이미지 업로드 성공: {}", s3FileKey);
            return amazonS3Client.getUrl(bucket, s3FileKey).toString();
        } catch (Exception e){
            log.error("S3 이미지 업로드 실패: {}", s3FileKey, e);
            throw new AmazonS3Exception("s3 이미지 업로드 실패");
        }
    }


    public String generateS3FileName(String fileTag, String extension){
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + "_" + fileTag + "_" + uuid + "." + extension;
    }

    public void deleteS3File(String s3FileKey) {
        if(!isObjectExist(s3FileKey)){
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

    public boolean isObjectExist(String s3FileKey){
        return amazonS3Client.doesObjectExist(bucket, s3FileKey);
    }

    public String generateS3FileKey(String savedFileName){
        return PROFILE_DIR + savedFileName;
    }
}
