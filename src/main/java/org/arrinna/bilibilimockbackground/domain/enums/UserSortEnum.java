package org.arrinna.bilibilimockbackground.domain.enums;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserSortEnum {
    DEFAULT(0,"fansCount","desc"),
    FANS_DESC(1,"fansCount","desc"),
    FANS_ASC(2,"fansCount","asc"),
    LEVEL_DESC(3,"level","desc"),
    LEVEL_ASC(4,"level","asc"),
    ;
    private Integer code;

    private String column;

    private String order;

    private static final Map<Integer,UserSortEnum> cache = Arrays.stream(UserSortEnum.values())
    .collect(Collectors.toMap(UserSortEnum::getCode, Function.identity()));

    public static UserSortEnum of(Integer code) {
        return cache.getOrDefault(code, DEFAULT);
    }
}
