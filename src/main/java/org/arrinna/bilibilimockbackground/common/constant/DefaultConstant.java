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

    String CANNOT_FIND_MSG = "未找到相关内容";

    String FOLLOW_SUCCESS_MSG = "关注成功";
    String UNFOLLOW_SUCCESS_MSG = "取关成功";
    String FOLLOW_FAIL_MSG = "自己不能关注自己哦~";

    int FOLLOWING=1;//关注中
    int UNFOLLOWED=2;//取关

    int ACCOUNT_FORBIDDEN=2;
    int LOG_OUT = 3;

    int ROOM_STATUS_NORMAL = 0;//房间默认状态

    int ROOM_STATUS_FORBIDDEN = 1;//房间被禁用

    int ROOM_FRIEND_STATUS_NORMAL= 0;//房间好友默认状态

    int ROOM_FRIEND_STATUS_FORBIDDEN= 1;//房间好友被禁用

    int ROOM_GROUP_STATUS_NORMAL= 0;//房间群组默认状态

    int ROOM_GROUP_STATUS_DELETED= 1;//房间群组被删除

    String DEFAULT_AUTO_RESPONSE="我们已互相关注，开始聊天吧~";

    String DEFAULT_SINGLE_RESPONSE="我关注了你~";

    int MESSAGE_STATUS_NORMAL=0;

    //消息状态撤回为1
    int MESSAGE_STATUS_RECALL = 1;

    String DEFAULT_AVATAR = "/user/default/default.png";


    /* 发送后多少分钟内可以撤回 */
    int MESSAGE_RECALL_LIMIT_MINUTES=2;//撤回时间的限制ing

    long MAX_EMOTICON_SIZE=1*1024*1024l;


    int GROUP_MAX_CREATE_PER_USER = 10;

    int GROUP_MAX_MEMBERS_FREE = 100;

    //一次最多邀请9个人进群

    int GROUP_MAX_INVITE_PER_USER = 9;

    int ROOM_ROLE_OWNER=1;

    int ROOM_ROLE_MANAGER=2;

    int ROOM_ROLE_MEMBER=3;

//    int NICKNAME_MIN_LENGTH=3;
//
//    int NICKNAME_MAX_LENGTH=18;
//
//    int SIGNATURE_MAX_LENGTH=100;


    /**
     * 群成员的在群里的一个类型
     */

    int MEMBER_STATUS_IN = 1;//已经进群

    int MEMBER_STATUS_MUTED = 2;//在群里but被禁言

    int MEMBER_STATUS_OUT=3;//被移出群

    int MEMBER_STATUS_PENDING=4;//等待对方进群通过

    int MEMBER_STATUS_REFUSE=5;//用户收到群邀请卡片，主动点了拒绝


}
