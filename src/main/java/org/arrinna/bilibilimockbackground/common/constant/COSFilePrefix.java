package org.arrinna.bilibilimockbackground.common.constant;


public interface COSFilePrefix {
    // 1.用户头像存储区
    String USER_AVATAR_PREFIX = "/user/%s/avatar/%s";

    //2.用户背景存储区


    //3.user storage place,for example: you can store your own emoticon here,
    // the first is pack_id, the second is code which is the description of emoji used in chinese
    //for example:if you want to store niulai emoticon,you can use this prefix,
    // the prefix maybe like this "/emoticon/1/niulai"
    String EMOTICON_PREFIX = "/emoticon/%s/%s";

}
