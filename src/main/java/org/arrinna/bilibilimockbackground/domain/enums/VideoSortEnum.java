package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VideoSortEnum {
    /** 综合排序 */
    DEFAULT(0, "play_count", "综合排序", "desc"),
    /** 最多播放 */
    MOST_PLAY(1, "play_count", "最多播放", "desc"),
    /** 最新发布 */
    NEW_PUBLISH(2, "publish_time", "最新发布", "desc"),
    /** 最多弹幕 */
    MOST_DANMAKU(3, "danmaku_count", "最多弹幕", "desc"),
    /** 最多收藏 */
    MOST_COLLECT(4, "collect_count", "最多收藏", "desc");

    private final Integer code;
    private final String column;
    private final String desc;
    private final String order;
}
