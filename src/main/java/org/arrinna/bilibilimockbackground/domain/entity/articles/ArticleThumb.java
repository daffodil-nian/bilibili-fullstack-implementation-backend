package org.arrinna.bilibilimockbackground.domain.entity.articles;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("article_thumb")
public class ArticleThumb implements Serializable {

        @TableId(type = IdType.AUTO)
        private Long id;

        /**
         * 专栏id，关联 articles 表主键
         */
        @TableField("article_id")
        private Long articleId;

        /**
         * 点赞用户ID
         */
        @TableField("user_id")
        private Long userId;

        /**
         * 点赞时间，数据库 DEFAULT CURRENT_TIMESTAMP
         */
        @TableField(value = "create_time", fill = FieldFill.INSERT)
        private LocalDateTime createTime;


}
