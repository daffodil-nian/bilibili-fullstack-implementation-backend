package org.arrinna.bilibilimockbackground.domain.enums.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GroupRoleEnum {
    OWNER(1,"群主"),
    ADMIN(2,"管理员"),
    MEMBER(3,"普通成员")
    ;
    private Integer code;
    private String desc;

//    然后写一个kv键
    private static final Map<Integer,GroupRoleEnum> CACHE;

    static {
        CACHE = Arrays.stream(values())
                .collect(Collectors.toMap(
                        GroupRoleEnum::getCode, Function.identity()
                ));
    }

    public static GroupRoleEnum of(Integer code){
        return CACHE.get(code);
    }

    //全体禁言时，仍可以发言的用户
    public boolean canSpeakWhenMuteAll(){
        return this==OWNER||this==ADMIN;
    }

}
