package org.arrinna.bilibilimockbackground.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserFollow;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.arrinna.bilibilimockbackground.mapper.UserFollowMapper;
import org.springframework.stereotype.Service;

@Service
public class UserFollowDao extends ServiceImpl<UserFollowMapper, UserFollow> {

    public UserInfoResp.UserFollowResp getFollowInfo(Long uid){
        UserInfoResp.UserFollowResp userFollowResp=UserInfoResp.UserFollowResp
                .builder()
                .followCount(getFollowCount(uid))
                .fansCount(getFansCount(uid))
                .build();
        return userFollowResp;
    }

    public Integer getFollowCount(Long uid){
        return lambdaQuery().
                eq(UserFollow::getUserId,uid)
                .eq(UserFollow::getStatus,1)
                .count().intValue();
        //这里返回的是用户关注了多少人
    }
    public Integer getFansCount(Long uid){
        return lambdaQuery().
                eq(UserFollow::getFollowId,uid) //follow_id是被关注的人的id
                .eq(UserFollow::getStatus,1)
                .count()
                .intValue();
    }


}
