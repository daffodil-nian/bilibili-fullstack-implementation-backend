package org.arrinna.bilibilimockbackground.dao.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserLvInfo;
import org.arrinna.bilibilimockbackground.domain.enums.BiliLVEnum;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.arrinna.bilibilimockbackground.mapper.user.UserMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserDao extends ServiceImpl<UserMapper, User> {



    public boolean updateNicknameById(Long id,String nickname){
        return lambdaUpdate()
                .eq(User::getId,id)
                .set(nickname!=null,User::getNickname,nickname)
                .update();
    }
    public boolean updateAvatarById(Long id,String avatar){
        return lambdaUpdate()
                .eq(User::getId,id)
                .set(avatar!=null,User::getAvatar,avatar)
                .update();
    }
    public boolean updateSignatureById(Long id,String signature){
        return lambdaUpdate()
                .eq(User::getId,id)
                .set(signature!=null,User::getSignature,signature)
                .update();
    }
    public boolean updateNicknameByUId(Long uid,String nickname){
        return lambdaUpdate()
                .eq(User::getUId,uid)
                .set(nickname!=null,User::getNickname,nickname)
                .update();
    }
    public boolean updateAvatarByUId(Long uid,String avatar){
        return lambdaUpdate()
                .eq(User::getUId,uid)
                .set(avatar!=null,User::getAvatar,avatar)
                .update();
    }
    public boolean updateSignatureByUId(Long uid,String signature){
        return lambdaUpdate()
                .eq(User::getUId,uid)
                .set(signature!=null,User::getSignature,signature)
                .update();
    }

    public boolean updateBirthDayByUId(Long uid, Date birthday){
        return lambdaUpdate()
                .eq(User::getUId,uid)
                .set(birthday!=null,User::getBirthDay,birthday)
                .update();
    }

    // 0 性别保密， 1 男 2 女
    public boolean updateSexByUId(Long uid,Integer sex){
        //TODO
        return lambdaUpdate()
                .eq(User::getUId,uid)
                .set(sex!=null,User::getSex,sex)
                .update();
    }

    public UserLvInfo getUserLevelInfoByUId(Long uid){
        User user= lambdaQuery()
                .eq(User::getUId,uid).one();
        Integer level = user.getLevel();
        Integer needAddExp = user.getNeedAddExp();
        UserLvInfo userLvInfo = UserLvInfo
                .builder()
                .level(level)
                .needAddExp(needAddExp)
                .levelName(BiliLVEnum.of(level).getDesc())
                .totalExp(BiliLVEnum.of(level).getTotalExp())
                .build();
        return userLvInfo;
    }

    public UserLvInfo getUserLevelInfoById(Long id){
        User user= lambdaQuery()
                .eq(User::getId,id).one();
        Integer level = user.getLevel();
        Integer needAddExp = user.getNeedAddExp();
        UserLvInfo userLvInfo = UserLvInfo
                .builder()
                .level(level)
                .needAddExp(needAddExp)
                .levelName(BiliLVEnum.of(level).getDesc())
                .totalExp(BiliLVEnum.of(level).getTotalExp())
                .build();
        return userLvInfo;
    }

    public Long getUserIdByUID(Long uid){
        User user= lambdaQuery()
                .eq(User::getUId,uid).one();
        return user.getId();
    }

    /**
     * 根据uid查询用户信息
     * @param uid
     * @return
     */
    public UserInfoResp.UserBaseInfo showUserInfo(Long uid){
        //根据uid查询用户信息，然后返回
        User user=lambdaQuery()
                .eq(User::getUId,uid)
                .one();
        //然后用上copy的一个函数
        UserInfoResp.UserBaseInfo userBaseInfo= BeanUtil.copyProperties(user,UserInfoResp.UserBaseInfo.class);
        userBaseInfo.setUID(uid);//
        return userBaseInfo;
    }


    /**
     * 根据用户账号密码来查询
     * @param username
     * @return
     */
    public Long findUserUIDByUsername(String username){
        return lambdaQuery()
                .eq(User::getUsername,username)
                .one().getUId()
                ;
    }

    /**
     * 判断用户是否存在
     * 如果存在这个用户就返回true
     * @param username
     * @return
     */
    public boolean isUserExist(String username){
        return lambdaQuery()
                .eq(User::getUsername,username)
                .exists()
                ;
    }

    /**
     * 判断用户是否存在
     * @param username
     * @param password
     * @return
     */
    public boolean isUserExist(String username,String password){
        return lambdaQuery()
                .eq(User::getUsername,username)
                .eq(User::getPassword,password)
                .exists()
                ;
    }

    @Override
    public boolean save(User entity) {
        return super.save(entity);
    }
}
