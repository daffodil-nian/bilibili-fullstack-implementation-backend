package org.arrinna.bilibilimockbackground.domain.dto.article;

import lombok.Data;

@Data
public class ArticleListQuery {
    private Long userId;
    private Long categoryId;
    //专栏分类
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
