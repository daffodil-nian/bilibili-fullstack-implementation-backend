package org.arrinna.bilibilimockbackground.dao.user;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserPrivacy;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserPrivacyReq;
import org.arrinna.bilibilimockbackground.mapper.user.UserPrivacyMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static cn.hutool.core.util.BooleanUtil.toInt;

@Service
public class UserPrivacyDao extends ServiceImpl<UserPrivacyMapper, UserPrivacy> {
    public UserPrivacy getByUid(Long uid) {
        return lambdaQuery().
                eq(UserPrivacy::getUId, uid)
                .one();
    }
    public Boolean updateUserPrivacyByUId(Long uid, UserPrivacy req){

        return lambdaUpdate()
                .eq(UserPrivacy::getUId, uid)
                .set(req.getShowCollect() != null, UserPrivacy::getShowCollect, req.getShowCollect())
                .set(req.getShowBirthdayAndTag() != null, UserPrivacy::getShowBirthdayAndTag, req.getShowBirthdayAndTag())
                .set(req.getShowComic() != null, UserPrivacy::getShowComic, req.getShowComic())
                .set(req.getShowBangumi() != null, UserPrivacy::getShowBangumi, req.getShowBangumi())
                .set(req.getShowSchoolInfo() != null, UserPrivacy::getShowSchoolInfo, req.getShowSchoolInfo())
                .set(req.getShowFansDecorate() != null, UserPrivacy::getShowFansDecorate, req.getShowFansDecorate())
                .set(req.getShowCoinVideo() != null, UserPrivacy::getShowCoinVideo, req.getShowCoinVideo())
                .set(req.getShowGame() != null, UserPrivacy::getShowGame, req.getShowGame())
                .set(req.getShowLiveReplay() != null, UserPrivacy::getShowLiveReplay, req.getShowLiveReplay())
                .set(req.getShowLikeVideo() != null, UserPrivacy::getShowLikeVideo, req.getShowLikeVideo())
                .set(req.getShowFansMedal() != null, UserPrivacy::getShowFansMedal, req.getShowFansMedal())
                .set(req.getShowClassVideo() != null, UserPrivacy::getShowClassVideo, req.getShowClassVideo())
                .set(req.getShowFollowList() != null, UserPrivacy::getShowFollowList, req.getShowFollowList())
                .set(req.getShowFansList() != null, UserPrivacy::getShowFansList, req.getShowFansList())
                .set(req.getShowChargeVideo() != null, UserPrivacy::getShowChargeVideo, req.getShowChargeVideo())
                .set(UserPrivacy::getUpdateTime, LocalDateTime.now())
                .update();
    }
}
