package org.arrinna.bilibilimockbackground.domain.vo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 这个我参考其他网站怎么写的
 */
@Data
public class ContactItemVO {
    private Long roomId;
    private Long peerUid;
    private Long lastMsgId;
    private String lastContent;
    private LocalDateTime activeTime;

    //加了三个属性，到时候一起打包返回给前端去
    private String peerNickname;//对方昵称！
    private String peerAvatar;//对方头像！
    private Integer unreadCount;//未读消息数量！
}
