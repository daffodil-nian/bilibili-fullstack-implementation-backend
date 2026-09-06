package org.arrinna.bilibilimockbackground.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_room_friend")
public class ChatRoomFriend {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * chat_room.id
     */
    private Long roomId;

    /**
     * 较小用户id
     */
    private Long uid1;

    /**
     * 较大用户id
     */
    private Long uid2;

    /**
     * 0正常 1禁用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}