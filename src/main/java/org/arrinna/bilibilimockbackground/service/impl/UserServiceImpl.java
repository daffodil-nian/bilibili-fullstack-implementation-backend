package org.arrinna.bilibilimockbackground.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.COSFilePrefix;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.AssertUtil;
import org.arrinna.bilibilimockbackground.dao.user.UserDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserFollow;
import org.arrinna.bilibilimockbackground.domain.enums.SexEnum;
import org.arrinna.bilibilimockbackground.domain.enums.UserRuleEnum;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserFollowReq;
import org.arrinna.bilibilimockbackground.manager.CosManager;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Arrinna
 * @Version 1.0.0
 * 用户登录注册
 */
@Service
@Slf4j
public class UserServiceImpl implements IUserService {


    /**
     * 没想好在哪里写拦截器部分
     */

    @Value("${cos.client.host}")
    private String host;
    @Autowired
    private UserDao userDao;
    @Resource
    private CosManager cosManager;
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

        //todo 修改了这个还要同步修改es中的数据！！！

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

    // todo 完善这个方法
    public Boolean updateFollowInfo(){

        return true;
    }

    @Override
    public String updateAvatar(Long uid, MultipartFile avatar){
        //1.接下来就要完善上传头像的代码了

        AssertUtil.isFalse(avatar.isEmpty(),ErrorCodeEnum.AVATAR_EMPTY);
        //2.要获取头像的大小，首先看看文件后缀是否符合要求，如果不符合要求也不行
        final ArrayList<String> suffixList = List.of("jpg", "png", "jpeg").stream().map(String::toLowerCase).collect(Collectors.toCollection(ArrayList::new));

        AssertUtil.isFalse(avatar.getSize()>DefaultConstant.MAX_AVATAR_SIZE,ErrorCodeEnum.AVATAR_SIZE_ERROR);

        //3.判断后缀
        String PicType=avatar.getContentType();
        log.info("图片类型是:{}",PicType);
        String suffix=PicType.split("/")[1];
        log.info("图片后缀是:{}",suffix);
        AssertUtil.isFalse(!suffixList.contains(suffix),ErrorCodeEnum.AVATAR_SIZE_ERROR);
        //如果没有符合的就说明图片类型不支持


        //4.接下来就通过拼接生成url,先上传到COS中再存储到数据库中

        String picture_name= avatar.getOriginalFilename();
        String filepath=String.format(COSFilePrefix.USER_AVATAR_PREFIX,uid,picture_name);
        log.info("可爱可爱的你"+picture_name);
        File file=null;

        try{
            file=File.createTempFile("avatar_", "");
            avatar.transferTo(file);
            cosManager.putObject(filepath,file);
            log.info("上传成功"+host+filepath);
            String url=host+filepath;
            //并且要把值写入到数据库中
            boolean result=userDao.updateAvatarByUId(uid,filepath);
            AssertUtil.isFalse(result==false,ErrorCodeEnum.UPLOAD_AVATAR_ERROR);
            return url;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //5.最后返回结果

    }

    @Override
    public Boolean updateBirthDay(Long uid, Date birthday){
         AssertUtil.isFalse(birthday==null,ErrorCodeEnum.BIRTHDAY_EMPTY);
         //不会存在拿不到ID的情况，因为没有登录根本进不去。。
        return userDao.updateBirthDayByUId(uid,birthday);
    }


    @Override
    public Boolean updateUserSex(Long uid, Integer sex){

            //就说明传入的数据不正确
            //如果expression满足就触发下面的条件
        AssertUtil.isFalse(sex!=null &&(!SexEnum.isValid(sex)),ErrorCodeEnum.PARAM_ERROR);

        return userDao.updateSexByUId(uid,sex);
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
