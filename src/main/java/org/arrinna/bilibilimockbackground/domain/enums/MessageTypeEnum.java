package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum MessageTypeEnum {
    TEXT(1, "文本"),
    EMOJI(2, "表情包"),
    IMAGE(3, "图片"),


    SYSTEM(4, "系统消息"),

    GROUP_INVITE(101,"群邀请卡片")
//    todo 其他等待
    ;
    private Integer code;
    private String desc;

    private static Map<Integer, MessageTypeEnum> cache;

    static {
        cache=Arrays.stream(MessageTypeEnum.values())
                .collect(Collectors.toMap(MessageTypeEnum::getCode,Function.identity()))
                ;
    }

    //然后还有一个of方法
    public static MessageTypeEnum of(Integer code){
        return cache.get(code);
    }


    //还要专门写一个区分系统消息与普通消息的方法,普通气泡和系统气泡有区别的！
    public static boolean isSystem(Integer code){
        return code!=null&&code.equals(SYSTEM.getCode());
    }


}
