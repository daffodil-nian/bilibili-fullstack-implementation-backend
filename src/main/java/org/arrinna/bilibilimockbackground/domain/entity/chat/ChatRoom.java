package org.arrinna.bilibilimockbackground.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("chat_room")
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 房间ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 1=群聊 2=单聊
     */
    private Integer type;

    /**
     * 0=正常 1=禁用/关闭
     */
    private Integer status;

    /**
     * 最后聊天活跃时间(列表排序用)
     */
    private LocalDateTime activeTime;

    /**
     * 最后一条消息ID
     */
    private Long lastMsgId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}