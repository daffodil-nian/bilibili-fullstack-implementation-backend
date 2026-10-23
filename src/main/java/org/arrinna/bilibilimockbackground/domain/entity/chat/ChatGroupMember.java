package org.arrinna.bilibilimockbackground.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_group_member")
public class ChatGroupMember {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * chat_room_group.id
     */
    private Long groupId;
    /**
     * 用户uid
     */
    private Long uid;
    /**
     * 1群主 2管理 3成员
     */
    private Integer role;
    /**
     * 1.在群里，2.被禁言，3被移出去
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}