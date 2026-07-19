package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum SexEnum {
    PROTECTED_SEX(0, "保密"),
    MALE(1, "男"),
    FEMALE(2, "女");
    private final Integer status;
    private final String desc;
    private static Map<Integer,SexEnum> cache;

    static {
        cache = Arrays.stream(SexEnum.values())
                .collect(Collectors.toMap(SexEnum::getStatus, Function.identity()));
    }
    public static SexEnum of(Integer status) {
        return cache.get(status);
    }

}
