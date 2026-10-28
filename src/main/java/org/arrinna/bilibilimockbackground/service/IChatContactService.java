package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.response.ContactItemVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IChatContactService {
    @Transactional(rollbackFor = Exception.class)
    Long createFriendSession(Long viewerUid, Long targetUid, int type);

    @Transactional(rollbackFor = Exception.class)
    void disableFriendSession(Long viewerUid, Long targetUid, int type);

    List<ContactItemVO> listContacts(Long uid);

    void markRead(Long uid, Long roomId);
}
