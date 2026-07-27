package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public enum UserRuleEnum {
    NICKNAME(3,18,"昵称长度必须在3-18位之间"),
    AVATAR(0,512,"头像地址格式不正确"),
    SIGNATURE(0,100,"个性签名长度不能超过100个字符");

    private final Integer minLength;
    private final Integer maxLength;
    private final String errMsg;

}
