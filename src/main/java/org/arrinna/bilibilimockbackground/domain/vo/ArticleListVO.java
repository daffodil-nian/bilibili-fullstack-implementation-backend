package org.arrinna.bilibilimockbackground.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleListVO {
    private Long articleId;
    private String title;
    private String cover;
    private String summary;
    private Long userId;
    private String authorNickname;
    private String authorAvatar;
    private Integer status;
    private Integer likeCount;
    private Integer commentCount;
    private Integer clickCount;
    private LocalDateTime publishTime;
    private String tagNames;
}
