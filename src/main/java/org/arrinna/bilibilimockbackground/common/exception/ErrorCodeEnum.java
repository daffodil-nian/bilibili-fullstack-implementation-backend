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

    // 通用业务失败
    BUSINESS_FAIL(40000, "业务校验失败"),
    // 系统异常
    SYSTEM_ERROR(50000, "服务器异常");

    private final Integer code;
    private final String msg;

}
