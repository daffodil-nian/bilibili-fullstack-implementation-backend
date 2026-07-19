package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ActiveStatusEnum {
    ONLINE(1,"在线"),
    OFFLINE(2,"离线"),
    ;
    private final Integer status;
    private final String desc;
    private static Map<Integer,ActiveStatusEnum> cache;

    static {
        cache = Arrays.stream(ActiveStatusEnum.values())
                .collect(Collectors.toMap(ActiveStatusEnum::getStatus, Function.identity()));
    }
    public static ActiveStatusEnum of(Integer status){
        return cache.get(status);
    }
}
