package org.arrinna.bilibilimockbackground.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ArticleSortEnum {
    /**
     * 0：综合排序，默认，按发布时间倒序
     */
    // column 必须与 ColumnEsDoc / ES 映射字段名一致（驼峰），不是 MySQL 列名
    DEFAULT(0, "publishTime", "desc"),
    /**
     * 1：最新发布
     */
    NEW_PUBLISH(1, "publishTime", "desc"),
    /**
     * 2：最多点击
     */
    CLICK_DESC(2, "clickCount", "desc"),
    /**
     * 3：最多喜欢
     */
    LIKE_DESC(3, "likeCount", "desc"),
    /**
     * 4：最多评论
     */
    COMMENT_DESC(4, "commentCount", "desc");

    /**
     * 前端传入编码
     */
    private Integer code;
    /**
     * ES 排序字段名（与 ColumnEsDoc 属性名一致）
     */
    private String column;
    /**
     * 排序方向：asc / desc
     */
    private String order;

    private static final Map<Integer, ArticleSortEnum> CACHE = Arrays.stream(ArticleSortEnum.values())
            .collect(Collectors.toMap(ArticleSortEnum::getCode, Function.identity()));

    /**
     * 根据code获取枚举，找不到返回默认综合排序
     * @param code 前端传的排序编码
     * @return ArticleSortEnum
     */
    public static ArticleSortEnum of(Integer code) {
        return CACHE.getOrDefault(code, DEFAULT);
    }
}
