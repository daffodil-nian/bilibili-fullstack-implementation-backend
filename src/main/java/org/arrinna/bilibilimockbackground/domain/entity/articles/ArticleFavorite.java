package org.arrinna.bilibilimockbackground.domain.entity.articles;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 读者收藏专栏（不是作者文集 articles.collection_id）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_favorite")
public class ArticleFavorite implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("article_id")
    private Long articleId;

    @TableField("user_id")
    private Long userId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
