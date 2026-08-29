package org.arrinna.bilibilimockbackground.domain.dto.article;

import lombok.Data;

@Data
public class ArticleCommentAddDto {
    private String content;
    private Long parentId;

}
