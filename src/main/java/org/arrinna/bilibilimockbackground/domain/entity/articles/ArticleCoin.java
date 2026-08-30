package org.arrinna.bilibilimockbackground.domain.entity.articles;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专栏投币关系：谁给哪篇投了几枚。余额在 user_wallet，总数在 articles.coin_count。
 * 本项目一人一篇一次，默认 1 币。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_coin")
public class ArticleCoin implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("article_id")
    private Long articleId;

    /** 投币者 u_id */
    @TableField("user_id")
    private Long userId;

    /** 本次投币数，固定 1 */
    @TableField("coin_num")
    private Integer coinNum;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
