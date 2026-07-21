package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum BiliLVEnum {
    //lv 0 60
    //lv1 200
    //lv2 1500
    //lv3 4500
    //lv4 11000
    //lv5 28880
    //lv6 35000
    LV0(0,60,0,"初来乍到"),
    LV1(1,200,140,"萌新小白"),
    LV2(2,1500,1300,"常驻观众"),
    LV3(3,4500,3000,"日日追更"),
    LV4(4,11000,6500,"资深看官"),
    LV5(5,28880,17880,"忠粉常驻"),
    LV6(6, 35000, 6120, "殿堂元老");
    private final Integer level;
    /** 达到该等级需要的【累计总经验】 */
    private final Integer totalExp;
    /** 从上一级升到本级需要新增的经验 */
    private final Integer needAddExp;
    /** 四字等级称号 */
    private final String desc;

    private static final Map<Integer,BiliLVEnum> cache;

    static {
        cache = Arrays.stream(BiliLVEnum.values())
                .collect(Collectors.toMap(BiliLVEnum::getLevel, Function.identity()))
        ;
    }
    public static BiliLVEnum of(Integer level){
        return cache.get(level);//根据key获得value
    }
}
