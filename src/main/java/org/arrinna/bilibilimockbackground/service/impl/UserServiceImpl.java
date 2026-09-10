package org.arrinna.bilibilimockbackground.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.COSFilePrefix;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.AssertUtil;
import org.arrinna.bilibilimockbackground.common.util.CosUtil;
import org.arrinna.bilibilimockbackground.common.util.EsUtil;
import org.arrinna.bilibilimockbackground.dao.user.UserDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.dao.user.UserPrivacyDao;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserFollow;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserPrivacy;
import org.arrinna.bilibilimockbackground.domain.enums.SexEnum;
import org.arrinna.bilibilimockbackground.domain.enums.UserRuleEnum;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSimpleVO;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSpaceVO;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserFollowReq;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserPrivacyReq;
import org.arrinna.bilibilimockbackground.manager.CosManager;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.arrinna.bilibilimockbackground.common.constant.DefaultConstant.FOLLOWING;
import static org.arrinna.bilibilimockbackground.common.constant.DefaultConstant.LOG_OUT;

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
    @Resource
    private UserPrivacyDao userPrivacyDao;
    @Autowired
    private CosUtil cosUtil;
//    @Resource
//    private EsUtil esUtil;

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
        //1.校验是否为空
        AssertUtil.isNotEmpty(followUid, ErrorCodeEnum.FOLLOW_USER_ID_EMPTY);

      //2.然后直接调用dao层的follow，直接能解决关注问题
        return userFollowDao.Follow(uid,followUid);
    }
    // todo 完善根据选项公开隐私的接口

    @Override
    public Boolean updateUserPrivacySetting(Long uid, UserPrivacyReq req){
        //1.
        AssertUtil.isFalse(uid==null||req==null,ErrorCodeEnum.PARAM_ERROR);
        //2.以防数据库没有这条消息写一下
        UserPrivacy privacy = userPrivacyDao.getById(uid); // 你自己封装：eq u_id
        if (privacy == null) {
            // 没有就建一条默认，再改
            privacy = UserPrivacy.builder().uId(uid).build(); // 其它靠 DB 默认或你手动 set 默认
            userPrivacyDao.save(privacy);
            privacy = userPrivacyDao.getByUid(uid);
        }
        //3.
        if(req.getShowFollowList()!=null){
            privacy.setShowFollowList(req.getShowFollowList()==false?1:0);//true变false
        }
        if(req.getShowFansList()!=null){
            privacy.setShowFansList(req.getShowFansList()==false?1:0);
        }
        privacy.setUpdateTime(LocalDateTime.now());

        //4.接下来调用dao层
        return userPrivacyDao.updateUserPrivacyByUId(uid,privacy);
    }

    @Override
    public UserSpaceVO getUserInfo(Long viewerUId, Long targetUId){
        AssertUtil.isFalse(viewerUId==null||targetUId==null,ErrorCodeEnum.PARAM_ERROR);
        //1.查询用户信息,如果是注销状态
//        UserInfoResp.UserBaseInfo userBaseInfo=userDao.showUserInfo(targetUId);
        //2.然后看看用户状态是否注销或进小黑屋，如果是就看不到消息，但是要把情况汇报回去！

        //。。。2是被关进小黑屋，3是注销
        User user=userDao.getUserByUID(targetUId);
        AssertUtil.isFalse(user.getStatus()==LOG_OUT,ErrorCodeEnum.UPINFO_ERROR);

        boolean isSelf = viewerUId != null && viewerUId.equals(targetUId);
        //如果是自己的号码就可以查看

        UserPrivacy privacy = getOrInitPrivacy(targetUId);
        //3.根据隐私设置，查询用户信息，先查以下信息
        boolean showBirthday = isPublic(privacy.getShowBirthdayAndTag());
        boolean showFollowList = isSelf || isPublic(privacy.getShowFollowList());
        boolean showFansList = isSelf || isPublic(privacy.getShowFansList());

        Boolean followed = false;
        if (viewerUId != null && !isSelf) {
            UserFollow rel = userFollowDao.getFollowByUidAndFollowId(viewerUId, targetUId);
            followed = rel != null && Objects.equals(rel.getStatus(), FOLLOWING);
        }

        //注意，这里的avatar要变动！！！！
        return UserSpaceVO
                .builder()
                .uid(targetUId)
                .nickname(user.getNickname())
                .avatar(cosUtil.toFullUrl(user.getAvatar()))
                .birthday(user.getBirthDay())
                .level(user.getLevel())
                .signature(user.getSignature())
                .followCount(userFollowDao.getFollowCount(targetUId))
                .fansCount(userFollowDao.getFansCount(targetUId))
                .likeCount(0L)//这个暂时替代一下
                .playCount(0L)
                .showFollowList(showFollowList)
                .showFansList(showFansList)
                .build();
    }

    @Override
    public List<UserSimpleVO> listFans(Long viewerId, Long targetUId, int page, int size){
        //1.首先判断用户的viewerUId是否等于targetUId
        checkFansListVisible(viewerId, targetUId);
        //2.如果可以看就来拼接
        Page<UserFollow> userFansList=userFollowDao.pageFans(targetUId,page,size);
        List<UserSimpleVO> list=buildSimpleList(viewerId,userFansList,true);
        log.info(list+"你好");
        return list;
    }
    @Override
    public List<UserSimpleVO> listFollows(Long viewerId, Long targetUId, int page, int size) {
        //1.
        checkFollowListVisible(viewerId, targetUId);
        //2.如果可以就来拼接
        Page<UserFollow> userFollowList=userFollowDao.pageFollow(targetUId,page,size);
        List<UserSimpleVO> list=buildSimpleList(viewerId,userFollowList,false);

        return list;
    }

    @Override
    public UserPrivacyReq getMyPrivacy(Long uid) {
        AssertUtil.isFalse(uid==null,ErrorCodeEnum.USER_NOT_LOGIN);
        UserPrivacy p = getOrInitPrivacy(uid);
        UserPrivacyReq resp = new UserPrivacyReq();
        resp.setShowFollowList(Objects.equals(p.getShowFollowList(), 1));
        resp.setShowFansList(Objects.equals(p.getShowFansList(), 1));
        resp.setShowBirthdayAndTag(Objects.equals(p.getShowBirthdayAndTag(), 1));
        resp.setShowCollect(Objects.equals(p.getShowCollect(), 1));
        resp.setShowBangumi(Objects.equals(p.getShowBangumi(), 1));
        resp.setShowGame(Objects.equals(p.getShowGame(), 1));
        resp.setShowChargeVideo(Objects.equals(p.getShowChargeVideo(), 1));
        resp.setShowComic(Objects.equals(p.getShowComic(), 1));
        resp.setShowSchoolInfo(Objects.equals(p.getShowSchoolInfo(), 1));
        resp.setShowFansDecorate(Objects.equals(p.getShowFansDecorate(), 1));
        resp.setShowCoinVideo(Objects.equals(p.getShowCoinVideo(), 1));
        resp.setShowGame(Objects.equals(p.getShowGame(), 1));
        resp.setShowLikeVideo(Objects.equals(p.getShowLikeVideo(), 1));
        resp.setShowFansMedal(Objects.equals(p.getShowFansMedal(), 1));
        resp.setShowClassVideo(Objects.equals(p.getShowClassVideo(), 1));
        resp.setShowFollowList(Objects.equals(p.getShowFollowList(), 1));
        resp.setShowFansList(Objects.equals(p.getShowFansList(), 1));
        resp.setShowChargeVideo(Objects.equals(p.getShowChargeVideo(), 1));

        return resp;
    }
    /**
     * 查看某个用户的隐私情况
     * @param uid
     * @return
     */
    private UserPrivacy getOrInitPrivacy(Long uid) {
        UserPrivacy p = userPrivacyDao.getByUid(uid);
        if (p == null) {
            userPrivacyDao.save(UserPrivacy.builder().uId(uid).build());
            p = userPrivacyDao.getByUid(uid);
        }
        return p;
    }
    private boolean isPublic(Integer flag) {
        return flag == null || Objects.equals(flag, 1);
    }

    private void checkFansListVisible(Long viewerId,Long targetUId){
        //1.如果是自己就可以看
        if(viewerId.equals(targetUId)){
            return;
        }
        //2.如果不是自己，则判断隐私设置,有数据就获取，没数据就直接插入数据即可
        UserPrivacy p = getOrInitPrivacy(targetUId);
        AssertUtil.isTrue(isPublic(p.getShowFansList()),ErrorCodeEnum.FANS_LIST_VISIBLE_ERROR);


    }
    /**
     * 判断用户是否能够查看关注列表
     * @param viewerId
     * @param targetUId
     */
    private void checkFollowListVisible(Long viewerId,Long targetUId){
        if (viewerId.equals(targetUId)) {
            return;
        }
        UserPrivacy p = getOrInitPrivacy(targetUId);
        AssertUtil.isTrue(isPublic(p.getShowFansList()),ErrorCodeEnum.FOLLOW_LIST_VISIBLE_ERROR);

    }

    /**
     * 如果可以查看用户隐私，就点开
     * 这个方法是用来构建用户的粉丝列表或者是关注用户列表
     * @param viewerUId
     * @param page
     * @param isFansPage ,true表示是粉丝列表，false表示是关注列表
     * @return
     */
    private List<UserSimpleVO> buildSimpleList(Long viewerUId, Page<UserFollow> page,boolean isFansPage){
        List<UserFollow> records = page.getRecords();
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        // 粉丝列表：谁关注了 target → userId
        // 关注列表：target 关注了谁 → followId
        //这个获取的是粉丝或者关注的人的ID
        List<Long> uids = records.stream()
                .map(r -> isFansPage ? r.getUserId() : r.getFollowId())
                .toList();
        // 批量查用户（没有 listByUids 就用 lambdaQuery().in）
        List<User> users = userDao.lambdaQuery()
                .in(User::getUId, uids)
                .list();

        Map<Long, User> userMap = users
                .stream()
                .collect(Collectors.toMap
                        (User::getUId, u -> u,
                                (a, b) -> a));
        //这个写法实际上是告诉我们key怎么取，value怎么取，然后用上冲突合并函数


        // 我是否已关注这些人,如果关注了就是回关
        Set<Long> followedByMe = Set.of();
        if (viewerUId != null && !uids.isEmpty()) {
            followedByMe = userFollowDao
                    .lambdaQuery()
                    .eq(UserFollow::getUserId, viewerUId)
                    .in(UserFollow::getFollowId, uids)
                    .eq(UserFollow::getStatus, FOLLOWING)
                    .list()
                    .stream()
                    .map(UserFollow::getFollowId)
                    .collect(Collectors.toSet());
        }
        Set<Long> finalFollowed = followedByMe;
        return uids.stream().map(id -> {
            User u = userMap.get(id);
            /**
             * 这是 lambda 里的返回：每处理一个 id，产出一个 UserSimpleVO。
             * map 会对每个 uid 调一次这个函数，把结果收集成列表。
             */
            return UserSimpleVO.builder()
                    .uid(id)
                    .nickname(u == null ? null : u.getNickname())
                    .avatar(u == null ? null : cosUtil.toFullUrl(u.getAvatar()))
                    .signature(u == null ? null : u.getSignature())
                    .level(u == null ? null : u.getLevel())
                    .followed(finalFollowed.contains(id))
                    .build();
        }).toList();
    }

}
