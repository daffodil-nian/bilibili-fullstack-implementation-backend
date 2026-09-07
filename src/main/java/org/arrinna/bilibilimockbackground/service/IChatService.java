package org.arrinna.bilibilimockbackground.service;

import org.springframework.transaction.annotation.Transactional;

public interface IChatService {

    Long createFriendSession(Long viewerUid, Long targetUid, int type);

    @Transactional(rollbackFor = Exception.class)
    void disableFriendSession(Long viewerUid, Long targetUid, int type);
}
