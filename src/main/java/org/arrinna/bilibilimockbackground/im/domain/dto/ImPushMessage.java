package org.arrinna.bilibilimockbackground.im.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * 消息推送
 */
public class ImPushMessage implements Serializable {
    /** 要尝试推送的用户 */
    private List<Long> uidList;
    /** 已经拼好的 WS JSON，例如 CHAT_MSG 那一串 */
    private String payload;
}
