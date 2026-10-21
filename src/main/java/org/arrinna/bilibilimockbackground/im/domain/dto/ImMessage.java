package org.arrinna.bilibilimockbackground.im.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.arrinna.bilibilimockbackground.im.enums.WsPushTypeEnum;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImMessage implements Serializable {

    private WsPushTypeEnum type;//消息类型

    private Long msgId;

    private Long roomId;

    private Long fromUid;

    private String content;//内容

    private Long timestamp;//时间戳

}
