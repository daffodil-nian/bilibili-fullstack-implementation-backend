package org.arrinna.bilibilimockbackground.common.constant;

import java.time.LocalDate;

/**
 * 常量默认值
 */
public interface DefaultConstant {
    int DEFAULT_SEX_NUM = 0;

    LocalDate DEFAULT_BIRTH_DAY=LocalDate.of(2000,1,1);
    //默认签名
    String DEFAULT_SIGNATURE = "这个人很调皮，什么也不留下！22娘都看不下去了！";

    String NICKNAME_EMPTY_MSG="用户昵称不能为空";

    Integer DEFAULT_FOLLOW_STATUS = 2;
//    int NICKNAME_MIN_LENGTH=3;
//
//    int NICKNAME_MAX_LENGTH=18;
//
//    int SIGNATURE_MAX_LENGTH=100;


}
