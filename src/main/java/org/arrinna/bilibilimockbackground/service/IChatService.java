package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.response.ChatMessageVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.ContactItemVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IChatService {

    Long createFriendSession(Long viewerUid, Long targetUid, int type);

    @Transactional(rollbackFor = Exception.class)
    void disableFriendSession(Long viewerUid, Long targetUid, int type);

    Long sendText(Long fromUid, Long targetUid, String text);

    List<ContactItemVO> listContacts(Long uid);

    List<ChatMessageVO> listMessages(Long uid, Long roomId, int limit);

    void markRead(Long uid, Long roomId);
}
