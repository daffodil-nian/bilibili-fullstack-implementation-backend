package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ArticleStatusEnum {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    UNDER_REVIEW(2, "审核中"),
    OFFLINE(3, "已下架"),
    PRIVATE(4, "私密");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

}
