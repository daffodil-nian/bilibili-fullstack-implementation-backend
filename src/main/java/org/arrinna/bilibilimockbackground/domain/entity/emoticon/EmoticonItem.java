package org.arrinna.bilibilimockbackground.domain.entity.emoticon;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("emoticon_item")
public class EmoticonItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long packId;
    private String code;       // [微笑]
    private String name;
    private String url;        // 相对路径
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}