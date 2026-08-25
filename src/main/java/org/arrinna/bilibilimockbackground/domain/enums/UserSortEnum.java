package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserSortEnum {
    DEFAULT(0,null,"desc"),
    FANS_DESC(1,"fans","desc"),
    FANS_ASC(2,"fans","asc"),
    LEVEL_DESC(3,"level","desc"),
    LEVEL_ASC(4,"level","asc"),
    ;
    private Integer code;

    private String column;

    private String order;
}
