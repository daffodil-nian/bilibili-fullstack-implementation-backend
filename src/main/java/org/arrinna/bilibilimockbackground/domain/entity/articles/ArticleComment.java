package org.arrinna.bilibilimockbackground.domain.entity.articles;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("article_comment")
public class ArticleComment implements Serializable {
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    @TableField("article_id")
    private Long articleId;

    @TableField("user_id")
    private Long userId;

    private String content;

    /** 一级=0，二级=根评id */
    @TableField("root_comment_id")
    private Long rootCommentId;

    /** 一级=null */
    @TableField("to_comment_id")
    private Long toCommentId;

    @TableField("reply_to_user_id")
    private Long replyToUserId;

    @TableField("comment_like_count")
    private Integer commentLikeCount;

    @TableField("reply_count")
    private Integer replyCount;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;
}