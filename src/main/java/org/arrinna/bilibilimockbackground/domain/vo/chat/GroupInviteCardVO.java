package org.arrinna.bilibilimockbackground.domain.vo.chat;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupInviteCardVO {

    private Long roomId;

    //群名
    private String groupName;

    private String groupAvatar;//群头像的URL？？

    private Long inviterUid;

    /** 邀请人昵称 */
    private String inviterName;

    private Long targetUid;

    private Integer memberCount;

    private String result;//群状态


}
