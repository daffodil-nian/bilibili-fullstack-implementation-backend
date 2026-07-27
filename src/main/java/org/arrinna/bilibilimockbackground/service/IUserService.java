package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.request.UserFollowReq;

public interface IUserService {

    Boolean updateSignature(Long uid, String signature);

    Boolean updateNickname(Long uid, String nickname);

    Boolean updateAvatar(Long uid, String avatar);

    Boolean followUser(Long uid, UserFollowReq req);
}
