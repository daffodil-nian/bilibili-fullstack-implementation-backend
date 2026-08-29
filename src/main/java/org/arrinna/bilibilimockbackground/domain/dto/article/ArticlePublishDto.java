package org.arrinna.bilibilimockbackground.domain.dto.article;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ArticlePublishDto {

    private String title;

    private String content;

    private String cover;

    private String summary;

    private Integer status;//状态

    private Long collection;

    private Long categoryId;

    private List<String> tagNames;//标签名称
}
