package org.arrinna.bilibilimockbackground.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 房间id
     */
    private Long roomId;

    /**
     * 发送者uid
     */
    private Long fromUid;

    /**
     * 文本/表情code/图片URL
     */
    private String content;

    /**
     * 消息类型：1文本 2表情 3图片
     */
    private Integer type;

    /**
     * 0正常 1已撤回
     */
    private Integer status;

    /**
     * @用户列表 JSON数组 [1,2]，单聊为空
     */
    private String atUids;

    /**
     * 回复消息id
     */
    private Long replyMsgId;
    /**
     * 发送方IP
     */
    private String clientIp;
    /**
     * IP属地
     */
    private String ipRegion;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}