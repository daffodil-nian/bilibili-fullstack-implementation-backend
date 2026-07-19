package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;

public interface IAuthService {

    UserInfoResp.UserBaseInfo login(String username, String password);

    boolean register(String username, String password, String checkPassword);
}
