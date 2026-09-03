package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.request.UserFollowReq;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserPrivacyReq;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

public interface IUserService {

    Boolean updateSignature(Long uid, String signature);

    Boolean updateNickname(Long uid, String nickname);

//    Boolean updateAvatar(Long uid, String avatar);

    String updateAvatar(Long uid, MultipartFile avatar);

    Boolean updateBirthDay(Long uid, Date birthday);

    Boolean updateUserSex(Long uid, Integer sex);

    Boolean followUser(Long uid, UserFollowReq req);

    Boolean updateUserPrivacySetting(Long uid, UserPrivacyReq req);
}
