package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OSS图片类型枚举类
 * 用于定义系统中支持的图片类型
 */
@Getter
@AllArgsConstructor
public enum OssImageTypeEnum {
    AVATAR("avatar/","用户头像"),
    COVER("cover/","视频封面"),
    UPLOAD("upload/","用户上传的普通图片");

    private String imagePathPrefix;

    private String desc;

    /**
     * 生成的图片
     * @param userId
     * @param fileName
     * @return
     */
    public String generateObjectKey(Long userId,String fileName){
        return imagePathPrefix+ userId + "/" +System.currentTimeMillis()+"_"+fileName;
    }
}
