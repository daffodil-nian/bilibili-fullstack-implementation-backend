package org.arrinna.bilibilimockbackground.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_contact")
public class ChatContact {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 当前用户uid
     */
    private Long uid;

    /**
     * chat_room.id
     */
    private Long roomId;

    /**
     * 读到的时间，用于未读统计
     */
    private LocalDateTime readTime;

    /**
     * 会话对我的活跃时间，会话列表排序
     */
    private LocalDateTime activeTime;

    /**
     * 最后消息id
     */
    private Long lastMsgId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}