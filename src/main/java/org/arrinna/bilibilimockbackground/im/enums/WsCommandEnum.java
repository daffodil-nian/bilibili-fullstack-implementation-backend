package org.arrinna.bilibilimockbackground.im.enums;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum WsCommandEnum {
    CHAT_SEND("CHAT_SEND");

    private final String code;

    private static Map<String,WsCommandEnum> cache;

    static {
        cache = Arrays.stream(WsCommandEnum.values())
                .collect(Collectors.toMap(WsCommandEnum::getCode, Function.identity()))
                ;
    }

    public static WsCommandEnum of(String code){
        return cache.get(code);
    }
}
