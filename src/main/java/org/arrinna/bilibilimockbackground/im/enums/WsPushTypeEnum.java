package org.arrinna.bilibilimockbackground.im.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum WsPushTypeEnum {
    ACK("ACK"),
    ERROR("ERROR"),
    CHAT_MSG("CHAT_MSG");
    private final String code;

    private static Map<String, WsPushTypeEnum> cache;

    static {
        cache= Arrays.stream(WsPushTypeEnum.values())
                .collect(Collectors.toMap(WsPushTypeEnum::getCode
                        , Function.identity()))
                ;
    }

    public static WsPushTypeEnum of(String code){
        return cache.get(code);
    }

}
