package com.triptune.common.fixture;

public class S3Fixture {

    public static String createS3ObjectUrl(String s3ObjectKey){
        return "http://test.com/" + s3ObjectKey;
    }
}
