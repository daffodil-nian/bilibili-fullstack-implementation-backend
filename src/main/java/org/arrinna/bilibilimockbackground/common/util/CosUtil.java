package org.arrinna.bilibilimockbackground.common.util;

import lombok.Data;
import org.arrinna.bilibilimockbackground.domain.enums.OssImageTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Data
@Component
public class CosUtil {

    @Value("${cos.client.host}")
    private String host;

    @Value("${cos.client.region}")
    private String region;

    @Value("${cos.client.bucket}")
    private String bucket;

    @Value("${cos.client.secret-id}")
    private String secretId;

    @Value("${cos.client.secret-key}")
    private String SecretKey;


    public String toFullUrl(String path) {
        if (path == null || path.isBlank()) {
            return null; // 或默认头像相对路径再拼
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (path.startsWith("/")) {
            return host + path;
        }
        return host + "/" + path;
    }



}
