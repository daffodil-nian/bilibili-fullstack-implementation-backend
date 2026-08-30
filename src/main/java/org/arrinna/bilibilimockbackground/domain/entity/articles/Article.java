package org.arrinna.bilibilimockbackground.domain.entity.articles;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@TableName(value = "articles")
@Data
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "article_id", type = IdType.AUTO)
    private Long articleId;

    @TableField("user_id")
    private Long userId;

    private String title;
    private String cover;
    private String summary;
    private String content;

    @TableField("category_id")
    private Long categoryId;

    @TableField("collection_id")
    private Long collectionId;

    private Integer status;

    private Integer visibility;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField("click_count")
    private Integer clickCount;

    @TableField("share_count")
    private Integer shareCount;

    @TableField("coin_count")
    private Integer coinCount;

    @TableField("favorite_count")
    private Integer favoriteCount;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
