package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.response.ChatMessageVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IChatMessageService {
    Long sendText(Long fromUid, Long targetUid, String text);

    List<ChatMessageVO> listMessages(Long uid, Long roomId, int limit);

    //修改状态，看看要不要判断是否是群聊
    @Transactional(rollbackFor = Exception.class)
    Boolean withdrawMessage(Long uid, Long messageId, Long roomId);
}
