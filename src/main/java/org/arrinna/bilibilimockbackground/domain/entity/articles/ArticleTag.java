package org.arrinna.bilibilimockbackground.domain.entity.articles;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("article_tag")
public class ArticleTag implements Serializable {
        @TableId(type = IdType.AUTO)
        private Long id;//tag的id

        private String name;//tag的名称
        @TableField(value = "create_time", fill = FieldFill.INSERT)
        private LocalDateTime createTime;
        @TableLogic
        @TableField("is_delete")
        private Integer isDelete;


}
