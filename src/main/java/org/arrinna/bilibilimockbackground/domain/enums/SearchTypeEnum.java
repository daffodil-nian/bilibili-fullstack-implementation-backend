package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
/**
 * 搜索类型枚举
 */
public enum SearchTypeEnum {
    VIDEO(1,"视频","video"),
    USER(2,"用户","user"),
    POST(3,"专栏帖子","post"),
    ANIME(4,"番剧","anime"),
    LIVE(5,"直播","live"),
    ARTICLE(6,"文章","article");

    private final int no;

    private final String text;

    private final String type;

    private static final Map<Integer, SearchTypeEnum> cache = new HashMap<>();

    static {
        for (SearchTypeEnum e : values()) {
            cache.put(e.getNo(), e);
        }
    }

    public static SearchTypeEnum of(Integer status) {
        return cache.get(status);
    }

}
