package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum columnSortEnum {
    DEFAULT(0,"publish_time","默认","desc"),
    NEW_PUBLISH(1, "publish_time", "最新发布", "desc"),

    /** 最多点击 播放量 */
    MOST_CLICK(2, "click_count", "最多点击", "desc"),

    /** 最多喜欢 点赞数 */
    MOST_LIKE(3, "like_count", "最多喜欢", "desc"),

    /** 最多评论 评论数量 */
    MOST_COMMENT(4, "comment_count", "最多评论", "desc");
    private Integer code;
    private String column;
    private String desc;
    private String order;
}
