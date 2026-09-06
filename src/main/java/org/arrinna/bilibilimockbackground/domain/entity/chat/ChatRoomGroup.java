package org.arrinna.bilibilimockbackground.domain.entity.chat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_room_group")
public class ChatRoomGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * chat_room.id
     */
    private Long roomId;

    /**
     * 群名称
     */
    private String name;

    /**
     * 群头像
     */
    private String avatar;

    /**
     * 群主 uid
     */
    private Long ownerUid;

    /**
     * 0正常 1解散/删除
     */
    private Integer deleteStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}