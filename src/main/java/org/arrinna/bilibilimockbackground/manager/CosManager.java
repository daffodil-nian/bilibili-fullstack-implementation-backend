package org.arrinna.bilibilimockbackground.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import org.arrinna.bilibilimockbackground.common.config.CosClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;
    @Value("${cos.client.bucket}")
    private String bucketName;
    // 将本地文件上传到 COS
    public PutObjectResult putObject(String key, File file) {
        return cosClient.putObject(bucketName, key, file);
    }
}
