
package org.arrinna.bilibilimockbackground.domain.vo.response;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 这个实际上是某个人的消息VO，有消息ID，关联的房间ID，消息，内容，类型和创建时间
 */
@Data
public class ChatMessageVO {
    private Long msgId;//消息ID
    private Long roomId;//房间ID
    private Long fromUid;//

    private String content;//内容
    private Integer type;//类型
    private LocalDateTime createTime;//消息的 创建时间
//    private String fromNickname;
//    private String fromAvatar;

}
