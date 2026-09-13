package org.arrinna.bilibilimockbackground.im.push;

import java.util.List;

public interface ImPushGateway {
    void pushToUser(List<Long> uidList,String payLoad);
}
