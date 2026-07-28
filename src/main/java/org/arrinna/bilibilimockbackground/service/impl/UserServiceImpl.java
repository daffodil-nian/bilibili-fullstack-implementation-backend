package org.arrinna.bilibilimockbackground.service.impl;

import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.AssertUtil;
import org.arrinna.bilibilimockbackground.dao.UserDao;
import org.arrinna.bilibilimockbackground.dao.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserFollow;
import org.arrinna.bilibilimockbackground.domain.enums.UserRuleEnum;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserFollowReq;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author Arrinna
 * @Version 1.0.0
 * 用户登录注册
 */
@Service
public class UserServiceImpl implements IUserService {


    /**
     * 没想好在哪里写拦截器部分
     */

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserFollowDao userFollowDao;
    @Override
    public Boolean updateSignature(Long uid, String signature){
        //首先要判断是否为空，如果为空就必须返回，还有签名字数有限制，如果字数不够也是不可以更新


        //1.如果签名为空就默认是默认签名，这样signature也不会为空
        if(signature==null||signature.isBlank()){
            signature= DefaultConstant.DEFAULT_SIGNATURE;
        }
        //2.判断异常
        AssertUtil.isFalse(signature.length()< UserRuleEnum.SIGNATURE.getMinLength()||signature.length()>UserRuleEnum.SIGNATURE.getMaxLength(),UserRuleEnum.SIGNATURE.getErrMsg());
        //3.然后就返回这个结果
        return userDao.updateSignatureByUId(uid,signature);

    }

    @Override
    public Boolean updateNickname(Long uid, String nickname){

        if(nickname==null||nickname.isBlank()){
            AssertUtil.isTrue(false,DefaultConstant.NICKNAME_EMPTY_MSG);
        }
        AssertUtil.isFalse(nickname.length()< UserRuleEnum.NICKNAME.getMinLength()||nickname.length()>UserRuleEnum.NICKNAME.getMaxLength(),UserRuleEnum.NICKNAME.getErrMsg());
        return userDao.updateNicknameByUId(uid,nickname);
    }

    @Override
    public Boolean updateAvatar(Long uid, String avatar){

        //1.接下来就要完善上传头像的代码了
        return userDao.updateAvatarByUId(uid,avatar);
    }

    @Override
    public Boolean followUser(Long uid, UserFollowReq req){
        Long followUid=req.getFollowId();
        Integer status=req.getStatus();

        //1.校验是否为空
        AssertUtil.isNotEmpty(followUid, ErrorCodeEnum.FOLLOW_USER_ID_EMPTY);
        AssertUtil.isNotEmpty(status, ErrorCodeEnum.FOLLOW_ACTION_EMPTY);

        // 2. 不能关注自己
        AssertUtil.isFalse(uid.equals(followUid), ErrorCodeEnum.CANNOT_FOLLOW_SELF);

        //3.然后就是先查看数据库中是否有follow用户的记录，用一个参数去接受用户关注的人数
//       Integer userFollowCount=userFollowDao.getFollowCount(uid);

       //4.接下来就是判断用户是否关注了这个用户，没有关注就记录没有关注，关注了就记录关注
      UserFollow record= userFollowDao.getFollowByUidAndFollowId(uid,followUid);

        return record!=null&&record.getStatus()!=DefaultConstant.DEFAULT_FOLLOW_STATUS;
    }
}
