package com.triptune.global.util;

import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

    public static String getExtension(String fileName){
        int dotIndex = fileName.lastIndexOf(".");

        if(dotIndex != -1 && dotIndex < fileName.length() - 1){
            return fileName.substring(dotIndex + 1);
        } else {
            log.error("");
            throw new DataNotFoundException(ErrorCode.EXTENSION_NOT_FOUND);
        }
    }
}
