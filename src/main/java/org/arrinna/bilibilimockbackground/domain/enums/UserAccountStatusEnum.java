package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum UserAccountStatusEnum {
    NORMAL(1,"正常"),
    BLACK_HOUSE(2,"纳入小黑屋"),
    LOGIN_OUT(3,"注销"),
    ;
    private final Integer status;
    private final String desc;
    private static Map<Integer,UserAccountStatusEnum> cache;

    static{
        cache= Arrays.stream(UserAccountStatusEnum.values())
                .collect(Collectors.toMap(UserAccountStatusEnum::getStatus, Function.identity()));

    }

    public static UserAccountStatusEnum of(Integer status){
        return cache.get(status);
    }
}
