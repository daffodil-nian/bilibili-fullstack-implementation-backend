package org.arrinna.bilibilimockbackground.testCOS;

import com.qcloud.cos.model.PutObjectRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.COSFilePrefix;
import org.arrinna.bilibilimockbackground.manager.CosManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@Slf4j
@SpringBootTest
public class test {

    @Resource
    private CosManager cosManager;

    /**
     * 测试完毕，这么操作能够把图片上传到对应的存储区域
     *
     */
    @Test
    public void test() {


// 本地文件路径
        String localFilePath = "C:\\Users\\lily\\Pictures\\绘画素材\\像素画\\角色图片.png";
        File localFile = new File(localFilePath);
        log.info("我我我:"+localFile.getName());
        String picture_name= localFile.getName();
        // 上传到COS的路径
        String filepath=String.format("/test/%s",picture_name);
        String filepath2=String.format(COSFilePrefix.USER_AVATAR_PREFIX,1,picture_name);
        try{
            cosManager.putObject(filepath2,localFile);
            //接下来是图片的URL
            log.info("上传成功1111"+filepath2);
        }
        catch (Exception e) {

        }
    }
}
