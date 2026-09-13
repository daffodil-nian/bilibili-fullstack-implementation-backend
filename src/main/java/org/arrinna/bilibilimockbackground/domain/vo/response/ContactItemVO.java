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
}
