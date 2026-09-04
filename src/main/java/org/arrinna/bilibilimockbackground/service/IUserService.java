package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.request.UserFollowReq;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserPrivacyReq;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSimpleVO;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSpaceVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public interface IUserService {

    Boolean updateSignature(Long uid, String signature);

    Boolean updateNickname(Long uid, String nickname);

//    Boolean updateAvatar(Long uid, String avatar);

    String updateAvatar(Long uid, MultipartFile avatar);

    Boolean updateBirthDay(Long uid, Date birthday);

    Boolean updateUserSex(Long uid, Integer sex);

    Boolean followUser(Long uid, UserFollowReq req);

    Boolean updateUserPrivacySetting(Long uid, UserPrivacyReq req);

    UserSpaceVO getUserInfo(Long viewerUId, Long targetUId);

    List<UserSimpleVO> listFans(Long viewerId, Long targetUId, int page, int size);

    List<UserSimpleVO> listFollows(Long viewerId, Long targetUId, int page, int size);

    UserPrivacyReq getMyPrivacy(Long uid);
}
