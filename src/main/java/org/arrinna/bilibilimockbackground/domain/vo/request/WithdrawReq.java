package org.arrinna.bilibilimockbackground.domain.vo.request;

import lombok.Data;

@Data
public class WithdrawReq {

    //跟据群消息ID撤回这一条消息
    private Long messageId;

    private Long roomId;
}
