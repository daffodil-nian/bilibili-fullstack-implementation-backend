package org.arrinna.bilibilimockbackground.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * 群资料
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoVO {

    private Long roomId;//房间ID

//    private Long groupId;//群聊ID

    private String groupName;//群聊名称

    private String groupAvatar;//群头像

    private Long userId;//和用户关联

    private String notice;//群公告/描述

    private Integer memberCount;//群成员数

    private LocalDateTime createTime;//创建时间

    private LocalDateTime updateTime;//更新时间

}
