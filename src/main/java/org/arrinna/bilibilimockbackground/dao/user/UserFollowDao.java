package org.arrinna.bilibilimockbackground.dao.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserFollow;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.arrinna.bilibilimockbackground.mapper.user.UserFollowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    //status=1是关注中，status=2是取消关注，没有记录就说明没有关注
    /**todo
     **
     * 四大现象一句话极简记忆：
     1. **脏读**：读到别人草稿（没提交的数据），对方最后撤销，你看到的东西根本不存在。
     2. **不可重复读**：同一个人，两次查**同一条记录**，内容变了（高考学生分数被别人改掉）
     3. **幻读**：同一个人，两次**查一类记录（范围查询）**，相当于学生一眨眼的功夫老师就从第三题已经讲到第十题的感觉
     4. **更新丢失**：两个人同时拿同一条记录修改，后写的覆盖前面的修改，改动直接消失（两个人同时扣粉丝数，只扣一次）
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean Follow(Long uid,Long followUid){
        //1.校验是否存在关注者是uid被关注着的id是followUid，如果存在就看status否则插入一条数据
        UserFollow userFollowRecord = getFollowByUidAndFollowId(uid, followUid);
        if(userFollowRecord==null){
            //insert一条数据
            UserFollow userFollow = UserFollow.builder()
                    .userId(uid)
                    .followId(followUid)
                    .status(DefaultConstant.FOLLOWING)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            boolean res=save(userFollow);
            return res;
        }
        //然后判断状态
      else if(userFollowRecord.getStatus().equals(DefaultConstant.FOLLOWING)){
            userFollowRecord.setStatus(DefaultConstant.UNFOLLOWED);
        }
      else {
            userFollowRecord.setStatus(DefaultConstant.FOLLOWING);
        }
        return updateById(userFollowRecord);
    }

    /**
     * 默认分页方式
     * 这里的page用mybatis里面的类，不要用spring framework提供的抽象接口
     * @param targetUid
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<UserFollow> pageFollow(long targetUid, int pageNum, int pageSize){
        //1.然后就分页查询
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId,targetUid)
                .eq(UserFollow::getStatus,DefaultConstant.FOLLOWING)
                .orderByDesc(UserFollow::getCreateTime);
        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        return page(page,wrapper);
    }

    /**
     *
     * @param targetUid
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<UserFollow> pageFans(long targetUid, int pageNum, int pageSize){
        //1.然后就分页查询
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowId,targetUid)
                .eq(UserFollow::getStatus,DefaultConstant.FOLLOWING)
                .orderByDesc(UserFollow::getCreateTime);
        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        return page(page,wrapper);

    }

    /**
     * 判断是否两个人是互相关注,是就返回true，不是就返回false
     * @param uid1
     * @param uid2
     * @return
     */
    public Boolean isMutualFollow(Long uid1,Long uid2){
        if(uid1==null||uid2==null||uid1.equals(uid2)){
            return false;
        }
        return isFollowing(uid1, uid2)&&isFollowing(uid2, uid1);
    }

    public boolean isFollowing(Long uid,Long targetUid){
        return lambdaQuery()
                .eq(UserFollow::getUserId,uid)
                .eq(UserFollow::getFollowId,targetUid)
                .eq(UserFollow::getStatus,DefaultConstant.FOLLOWING)
                .count()>0;
    }

}
