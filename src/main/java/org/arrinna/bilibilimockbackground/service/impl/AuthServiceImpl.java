package org.arrinna.bilibilimockbackground.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.algorithm.SnowAlgorithm;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.constant.RedisKey;
import org.arrinna.bilibilimockbackground.common.constant.ValidationConstant;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.*;
import org.arrinna.bilibilimockbackground.dao.user.UserDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.dao.user.UserPrivacyDao;
import org.arrinna.bilibilimockbackground.dao.user.UserWalletDao;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserPrivacy;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserWallet;
import org.arrinna.bilibilimockbackground.domain.enums.ActiveStatusEnum;
import org.arrinna.bilibilimockbackground.domain.enums.BiliLVEnum;
import org.arrinna.bilibilimockbackground.domain.enums.SexEnum;
import org.arrinna.bilibilimockbackground.domain.enums.UserAccountStatusEnum;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.arrinna.bilibilimockbackground.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @Author Arrinna
 * 用户身份校验的service层
 */
@Service
@Slf4j
public class AuthServiceImpl implements IAuthService {

    @Resource
    private SnowAlgorithm snowAlgorithm;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserFollowDao userFollowDao;
    @Autowired
    private UserWalletDao userWalletDao;
    @Autowired
    private CosUtil cosUtil;
    @Resource
    private UserPrivacyDao userPrivacyDao;

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public UserInfoResp.UserBaseInfo login(String username, String password){
        //1.检查账号和密码是否有效
        AssertUtil.isFalse(username.isBlank(), ErrorCodeEnum.ACCOUNT_EMPTY);    // 校验账号不能为空
        AssertUtil.isFalse(password.isBlank(),ErrorCodeEnum.PASSWORD_EMPTY);    // 校验密码不能为空

        //2.查询数据库，检查用户是否存在，其中密码需要进行解密
        AssertUtil.isTrue(userDao.isUserExist(username,PasswordUtil.encryptWithStaticSalt(password)),ErrorCodeEnum.PASSWORD_ERROR);

        System.out.println("用户名和密码校验成功,您即将登录成功~"); //验证成功，接下来继续往后面写吧~
        //3.找到该用户对应的UID,我们要调用userDao

        Long uid= userDao.findUserUIDByUsername(username);
        String redisKey=RedisUtils.getKey(RedisKey.USER_TOKEN,uid);
        System.out.println("redisKey:love you partner"+redisKey);

        //4.接下来就去redis中根据prefix在redis中找,找到就续期
        String token = RedisUtils.get(redisKey);
        if(token!=null){
            log.info("用户已经登录，token续期7天");
            RedisUtils.expire(redisKey,7, TimeUnit.DAYS);
        }
        //5.否则就是重新生成jwt并且存在redis中
        else{
            log.info("生成token");
            String temp_token= jwtUtils.createToken(uid);
            RedisUtils.set(redisKey,temp_token,7,TimeUnit.DAYS);

        }
        //6.返回给前端，
        log.info("用户登录成功");

        //7.最后用一个变量存储这些值昂

        UserInfoResp.UserBaseInfo userBaseInfo= userDao.showUserInfo(uid);

        String avatar=userBaseInfo.getAvatar();
        if(avatar==null||avatar.isBlank()){

        }
        else{
            String avatar_url=cosUtil.toFullUrl(avatar);
            userBaseInfo.setAvatar(avatar_url);
        }


        //8.完善一下用户的关注情况和钱包信息,根据用户的UID获取就可以了，反正也是公开数据，等后续骨架搭建好后再完善

        Long user_long_id= userDao.getUserIdByUID(uid);
        //等后续再把关注列表完善一下，加上一点功能，有的UP主不让别人看他的关注列表，就跟抖音一样


        //todo 已经改了这个BUG，要传uid而不是user_long_id,
        // 接下来要同步更新es数据库
        userBaseInfo.setUserFollowInfo(userFollowDao.getFollowInfo(uid));

        //9.接着根据用户的u_id来获取用户的等级情况
        // & 根据用户的id根据硬币情况
        userBaseInfo.setUserLvInfo(userDao.getUserLevelInfoByUId(uid));
        userBaseInfo.setUserWalletResp(userWalletDao.getUserWalletByULongId(user_long_id));

        //10.接着把用户的等级情况完善一下一并返回给用户

        //11.用户登录状态记得改
        //userBaseInfo.setActiveStatus(ActiveStatusEnum.ONLINE.getStatus());
       System.out.println("syc"+userBaseInfo);
        log.info("我表示理解{}",userBaseInfo);
        return userBaseInfo;
    }

    /**
     * * 用户注册方法
     * * 先用账户名密码注册，然后再想短信注册方式吧，毕竟这个不是很科学
     * * @param username 用户名
     * * @param password 密码
     * * @param checkPassword 确认密码
     *
     * @return 注册结果字符串
     */
    @Override
    public boolean register(String username, String password, String checkPassword){
        //1.检查账号和密码是否有效
        AssertUtil.isFalse(username.isBlank(),ErrorCodeEnum.ACCOUNT_EMPTY);    // 校验账号不能为空
        AssertUtil.isFalse(password.isBlank(),ErrorCodeEnum.PASSWORD_EMPTY);    // 校验密码不能为空
        AssertUtil.isFalse(checkPassword.isBlank(),ErrorCodeEnum.CHECK_PWD_EMPTY); // 校验确认密码不能为空
        AssertUtil.isTrue(password.equals(checkPassword),ErrorCodeEnum.PWD_NOT_SAME); // 校验两次输入的密码是否一致

        //2.检查账号和密码的长度规范，这里我们规定用户名长度在8-16位之间
        AssertUtil.isTrue(username.length()>=8 && username.length()<=16,ErrorCodeEnum.ACCOUNT_LENGTH_ERROR);
        AssertUtil.isTrue(password.length()>=8 && password.length()<=16,ErrorCodeEnum.PWD_LENGTH_ERROR);

        //3.检查密码的复杂程度
        AssertUtil.isTrue(password.matches(ValidationConstant.PasswordRegex),ErrorCodeEnum.PWD_COMPLEX_ERROR);

        //4.密码加盐，提高用户数据安全性
        String encryptedPassword = PasswordUtil.encryptWithStaticSalt(password);

        //5.我们用上雪花算法来生成id，保证这个ID的唯一性

        Long id = snowAlgorithm.nextIdInstance();
        //bilibili注册后昵称默认是bili前缀+后面的一些字母
        String nickname= GenerateUserDefaultNickname.getInstance().nextUniqueName();

        //6.接下来就是sex，我们用上builder构造器，这样就不会暴露一些写法了。

        //7.接着判断用户是否注册了，如果注册了就不能够注册，这时需要我们调用dao层中的一个方法

        AssertUtil.isFalse(userDao.isUserExist(username),ErrorCodeEnum.ACCOUNT_ALREADY_EXIST);

        //8.最后就是注册，然后把注册成功的结果返回
        User user = User.builder()
                .id(id)
                .username(username)
                .password(encryptedPassword)
                .nickname(nickname)
                .signature(DefaultConstant.DEFAULT_SIGNATURE)
                .sex(SexEnum.PROTECTED_SEX.getStatus()) //默认是保密的性别
                .birthDay(DefaultConstant.DEFAULT_BIRTH_DAY)
                .activeStatus(ActiveStatusEnum.OFFLINE.getStatus()) //默认离线，除非登录了才是在线的
                .status(UserAccountStatusEnum.NORMAL.getStatus())
                .level(BiliLVEnum.LV0.getLevel())   //刚刚注册的用户的lv等级是0
                .needAddExp(BiliLVEnum.LV0.getNeedAddExp())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        //9.需要给每个用户分配一个硬币包包
        UserWallet userWallet= UserWallet
                .builder()
                .user_id(id) // 以后统一改成u_long_id，而不是UID
                .bCoin(0)
                .coin(0)
                .build();
        userWalletDao.save(userWallet);

        //10.还需要给每个注册的用户分配一个包包，待开发。。。



        //11.最后还有用户登录注册时所在的IP地址，然后要返回给前端，待开发。。。


        //12.then 调用dao层中的方法，把注册结果返回回去
        boolean isRegister=userDao.save(user);

        //13.注册完毕还要调用userPrivacyDao，给用户分配一个隐私设置
        UserPrivacy userPrivacy=UserPrivacy
                .builder()
                .uId(user.getUId())
                .build();

        return isRegister;
    }
}
