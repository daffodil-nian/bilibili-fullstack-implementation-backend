package org.arrinna.bilibilimockbackground.domain.entity.articles;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章和tagId关联
 */
@Data
@TableName("article_tag_relation")
public class ArticleTagRelation implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;//关联主键

    @TableField("article_id")
    private Long articleId;//文章id

    @TableField("tag_id")
    private Long tagId;//tag的id

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
