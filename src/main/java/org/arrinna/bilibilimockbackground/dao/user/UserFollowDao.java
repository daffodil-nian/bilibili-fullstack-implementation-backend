package org.arrinna.bilibilimockbackground.dao.user;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserFollow;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.arrinna.bilibilimockbackground.mapper.user.UserFollowMapper;
import org.springframework.stereotype.Service;

@Service
public class UserFollowDao extends ServiceImpl<UserFollowMapper, UserFollow> {
//    public int getUserFollowCount(Long uid){
//        //1.uid是用户的uid
//        return lambdaQuery().eq(UserFollow::getUserId,uid).count().intValue();
//    }

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

    public UserFollow getFollowByUidAndFollowId(Long uid,Long followId){
     //这个要判断followId和uid是否是一个关注与被关注的关系
        return lambdaQuery()
                .eq(UserFollow::getUserId,uid)
                .eq(UserFollow::getFollowId,followId)
                .one();
    }

}
