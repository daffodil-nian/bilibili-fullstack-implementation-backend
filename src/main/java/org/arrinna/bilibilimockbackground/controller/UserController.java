package org.arrinna.bilibilimockbackground.controller;

import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.exception.BusinessException;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.EsUtil;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserFollow;
import org.arrinna.bilibilimockbackground.domain.enums.MessageTypeEnum;
import org.arrinna.bilibilimockbackground.domain.enums.RoomTypeEnum;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserFollowReq;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserPrivacyReq;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSimpleVO;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSpaceVO;
import org.arrinna.bilibilimockbackground.manager.CosManager;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/user")
@RestController
public class UserController {

    @Autowired
    private IUserService userService;
    @Resource
    private CosManager cosManager;
    @Resource
    private EsUtil esUtil;
    @Resource
    private IChatService chatService;
    @Autowired
    private UserFollowDao userFollowDao;

    /**
     * 用户修改签名
     */
    @PutMapping("/update/signature")
    public Result<Boolean> updateSignature(@RequestAttribute("uid") Long uid
    , @RequestBody Map<String,String> body){
        String signature=body.get("signature");
        Boolean ok=userService.updateSignature(uid,signature);
                if(ok){
                    esUtil.syncUserByUid(uid);
                }
        return Result.Success(ok);
    }

    @PutMapping("/update/birthday")
    public Result<Boolean> updateBirthDay(@RequestAttribute("uid") Long uid
    ,@RequestBody Map<String,String> body){
        String birthdayStr=body.get("birthday");
        //然后更新生日
        Date birthday = null;
        //不为空才解析，null代表本次不更新生日
        if(birthdayStr != null && !birthdayStr.isBlank()){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try{
                birthday=sdf.parse(birthdayStr);
            }
            catch (ParseException e){
                throw new BusinessException(ErrorCodeEnum.PARAM_ERROR);
            }
            //最后来试试看看能否更新成功
        }
        Boolean ok=userService.updateBirthDay(uid,birthday);
        if(ok){
            esUtil.syncUserByUid(uid);
        }
        return Result.Success(userService.updateBirthDay(uid,birthday));
    }

    @PostMapping("/update/avatar")
    public Result<String> updateAvatar(@RequestAttribute("uid") Long uid,
                                        @RequestParam("file") MultipartFile file){
        String ok=userService.updateAvatar(uid,file);
        if(ok!=null){
            esUtil.syncUserByUid(uid);
        }
        return Result.Success(ok,"修改头像成功！"); //没想到这里也有BUG，因为string 参数优先级高，T data优先级低，所以才会出BUG，为了避免这个问题我们再传递一个msg这样就可以避开BUG了！
    }

    @PutMapping("/update/sex")
    public Result<Boolean> updateSex(@RequestAttribute("uid") Long uid,
    @RequestBody Map<String,String> body) {
        String sexStr = body.get("sex");
        Integer sex = null;
        if(sex==null){
            sex=Integer.parseInt(sexStr);
        }
        Boolean ok= userService.updateUserSex(uid,sex);
        if(ok){
            esUtil.syncUserByUid(uid);
        }
        //然后修改性别
        return Result.Success(userService.updateUserSex(uid,sex));
    }
    /**
     * 用户修改昵称
     */
    @PutMapping("/update/nickname")
    public Result<Boolean> updateNickname(@RequestAttribute("uid") Long uid
            ,@RequestBody Map<String,String> body){
        String nickname=body.get("nickname");
        Boolean ok = userService.updateNickname(uid,nickname);
        if(ok){
            esUtil.syncUserByUid(uid);
        }
        return Result.Success(userService.updateNickname(uid,nickname));
    }

    /**
     * 用户关注/取消关注（toggle）
     * Body: { "followId": 被关注者u_id }
     */
    @PostMapping("/follow")
    public Result<Boolean> followUser(@RequestAttribute("uid") Long uid,
                                      @RequestBody UserFollowReq req) {
        Long targetUid = req == null ? null : req.getFollowId();
        if (targetUid == null) {
            return Result.fail(ErrorCodeEnum.FOLLOW_USER_ID_EMPTY.getCode(),
                    ErrorCodeEnum.FOLLOW_USER_ID_EMPTY.getMsg());
        }
        if (uid.equals(targetUid)) {
            return Result.fail(DefaultConstant.FOLLOW_FAIL_MSG);
        }
        //判断两个人是否是互相关注的，不同结果调用不同方法
        UserFollow old=userFollowDao.getFollowByUidAndFollowId(uid,targetUid);
        boolean wasFollowing=old!=null&&old.getStatus().equals(DefaultConstant.FOLLOWING);

        boolean res = userService.followUser(uid, req);
        // 同步被关注者（粉丝数在其文档上）；toDoc 里 fansCount 仍可能是 0，后续再完善


        //关注完就要把数据信息同步到elasticSearch中
        // 同时要调用chatService中的方法

        if(!res){
            return Result.Success(false);
        }

        esUtil.syncUserByUid(targetUid);
        esUtil.syncUserByUid(uid);

        if(wasFollowing){
            //调用取消关注的方法
            chatService.disableFriendSession(uid,targetUid, RoomTypeEnum.FRIEND.getType());
        }
        else{
            //调用关注的方法
            chatService.createFriendSession(uid,targetUid, RoomTypeEnum.FRIEND.getType());
        }

        return Result.Success(res);
    }


    //   /** 3. 个人主页 */
    //    @GetMapping("/{targetUid}/space")
    //    public Result<UserSpaceVO> getUserSpace(@RequestAttribute("uid") Long viewerUid,
    //                                            @PathVariable Long targetUid) {
    //        return Result.Success(userService.getUserInfo(viewerUid, targetUid));
    //    }
    //实现用户点击个人首页能够看到用户信息
    // 如果设为隐私是看不到的除了自己
    @GetMapping("/{targetUid}/space")
    public Result<UserSpaceVO> ShowUserInfo(@RequestAttribute("uid") Long uid, @PathVariable("targetUid") Long targetUid) {
        //1.首先是用户点击user的uid，这里排除了自己看自己的情况所以不要紧，我们直接

         return Result.Success(userService.getUserInfo(uid, targetUid));
    }

    //4.如果用户不想让人看到自己的粉丝列表，可以选择设置，这样就看不到了
    //5.反之，就可以查看关注数和粉丝数
    @PutMapping("/update/privacy")
    public Result<Boolean> updateUserPrivacySetting(@RequestAttribute("uid") Long uid,@RequestBody UserPrivacyReq req){

        return Result.Success(userService.updateUserPrivacySetting(uid, req));

    }

    /** 粉丝列表 */
    @GetMapping("/{targetUid}/fans")
    public Result<List<UserSimpleVO>> fans(@RequestAttribute("uid") Long viewerUid,
                          @PathVariable Long targetUid,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "24") int size) {

        return Result.Success(userService.listFans(viewerUid,targetUid, page, size));
    }

    /** 关注列表 */
    @GetMapping("/{targetUid}/follows")
    public Result<List<UserSimpleVO>> follows(@RequestAttribute("uid") Long viewerUid,
                                              @PathVariable Long targetUid,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "24") int size) {
        return Result.Success(userService.listFollows(viewerUid,targetUid, page, size));
    }


//    /** 6. 查自己隐私（设置页） */
    @GetMapping("/search/privacy")
    public Result<UserPrivacyReq> getPrivacy(@RequestAttribute("uid") Long uid) {

        return Result.Success(userService.getMyPrivacy(uid));
    }

}
