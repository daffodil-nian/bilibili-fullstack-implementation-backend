package org.arrinna.bilibilimockbackground.common.util;

import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FileUtil {

    static final ArrayList<String> suffixList = List.of("jpg", "png", "jpeg")
            .stream().map(String::toLowerCase).collect(Collectors.toCollection(ArrayList::new));


    /**
     * 这个是判断图片文件是否合规的一个方法
     * @param file
     * @param maxSize
     */
    public static void checkImageFile(MultipartFile file, long maxSize){

        //1.首先图片不能为空
        AssertUtil.isFalse(file.isEmpty(), ErrorCodeEnum.PICTURE_EMPTY);

        //2.图片大小不能超过多少
        AssertUtil.isFalse(file.getSize()> DefaultConstant.MAX_AVATAR_SIZE,ErrorCodeEnum.PICTURE_SIZE_EXCEED);

        //3.然后判断后缀
        String PicType=file.getContentType();
        log.info("图片类型是:{}",PicType);
        String suffix=PicType.split("/")[1];
        log.info("图片后缀是:{}",suffix);
        AssertUtil.isFalse(!suffixList.contains(suffix),ErrorCodeEnum.PICTURE_TYPE_ERROR);

    }

    public String buildFilePath(String fileName,String prefix,Long roomId){
        return String.format(prefix,roomId,fileName);
    }

}
