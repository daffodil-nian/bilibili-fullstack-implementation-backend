package org.arrinna.bilibilimockbackground.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {
    // ========== 登录模块 ==========
    ACCOUNT_NOT_EXIST(10001, "账号不存在"),
    PASSWORD_ERROR(10002, "用户名或密码错误"),
    ACCOUNT_EMPTY(10003, "用户名不能为空"),
    PASSWORD_EMPTY(10004, "密码不能为空"),

    // ========== 注册模块 ==========
    CHECK_PWD_EMPTY(10005, "确认密码不能为空"),
    PWD_NOT_SAME(10006, "两次密码输入不一致"),
    ACCOUNT_LENGTH_ERROR(10007, "账号长度在8-16位之间"),
    PWD_LENGTH_ERROR(10008, "密码长度在8-16位之间"),
    PWD_COMPLEX_ERROR(10009, "密码要包含大写字母小写字母等复杂符号"),
    ACCOUNT_ALREADY_EXIST(10010, "该账号已存在"),

    // ========= 关注模块 ===========
    FOLLOW_USER_ID_EMPTY(10011, "关注用户ID不能为空"),
    FOLLOW_ACTION_EMPTY(10012, "操作类型不能为空"),
    CANNOT_FOLLOW_SELF(10013, "不能关注你自己哦"),
    FOLLOW_USER_NOT_EXIST(10014, "关注的目标用户不存在"),
    FOLLOW_ACTION_ERROR(10015, "关注操作类型不合法"),

    // ========= 用户模块 ===========
    SEX_EMPTY(10016, "性别不能为空"),
    BIRTHDAY_EMPTY(10017, "生日不能为空"),
    AVATAR_EMPTY(10018, "头像不能为空"),
    AVATAR_SIZE_ERROR(10019, "头像大小不能超过2M"),
    AVATAR_TYPE_ERROR(10020, "头像类型不支持"),
    UPLOAD_AVATAR_ERROR(10021, "上传头像失败，请稍后再试哦~"),

    // ========== 专栏模块 ==========
    ARTICLE_TITLE_EMPTY(11022, "专栏标题不能为空"),
    ARTICLE_CONTENT_EMPTY(11023, "专栏内容不能为空"),
    ARTICLE_COVER_EMPTY(11024, "专栏封面不能为空"),
    ARTICLE_SUMMARY_EMPTY(11025, "专栏摘要不能为空"),
    ARTICLE_NOT_FOUND(11026, "专栏不存在"),
    ARTICLE_FORBIDDEN(11027, "专栏状态私密，您不可查看"),


    // 通用业务失败
    BUSINESS_FAIL(40000, "业务校验失败"),
    PARAM_ERROR(40001, "请求参数非法"),
    // 系统异常
    SYSTEM_ERROR(50000, "服务器异常");

    private final Integer code;
    private final String msg;

}
