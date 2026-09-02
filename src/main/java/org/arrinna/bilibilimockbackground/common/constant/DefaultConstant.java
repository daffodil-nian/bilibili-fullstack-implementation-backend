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
    Long MAX_AVATAR_SIZE=2*1024*1024L;

    String REGISTER_SUCCESS_MSG="注册成功";

    String REGISTER_FAIL_MSG="登录成功";

    String SEARCH_SUCCESS_MSG = "搜索成功";

    int FOLLOWING=1;//关注中
    int UNFOLLOWED=2;//取关

//    int NICKNAME_MIN_LENGTH=3;
//
//    int NICKNAME_MAX_LENGTH=18;
//
//    int SIGNATURE_MAX_LENGTH=100;


}
