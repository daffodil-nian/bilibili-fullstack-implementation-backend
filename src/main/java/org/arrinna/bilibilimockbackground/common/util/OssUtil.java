package org.arrinna.bilibilimockbackground.common.util;

import lombok.Data;
import org.arrinna.bilibilimockbackground.domain.enums.OssImageTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Data
@Component
public class OssUtil {

    @Value("${spring.oss.endpoint}")
    private String endpoint;

    @Value("${spring.oss.domain}")
    private String region;

    @Value("${spring.oss.bucketName}")
    private String bucket;

    @Value("${spring.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${spring.oss.accessKeySecret}")
    private String accessSecret;

    public String upLoadImage(OssImageTypeEnum imageType, Long userId, String fileName
    , InputStream inputStream
                              ){
        String objectKey =imageType.generateObjectKey(userId,fileName);
        return objectKey;
    }


}
