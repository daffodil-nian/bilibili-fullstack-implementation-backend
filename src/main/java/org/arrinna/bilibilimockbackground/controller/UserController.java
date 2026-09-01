package org.arrinna.bilibilimockbackground.controller;

import jakarta.annotation.Resource;
import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.common.exception.BusinessException;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.EsUtil;
import org.arrinna.bilibilimockbackground.manager.CosManager;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    /**
     * 用户修改头像
     *
     */
//    @PutMapping("/update/avatar")
//    public Result<Boolean> updateAvatar(@RequestAttribute("uid") Long uid
//    ,@RequestBody Map<String,String> body){
//        String avatar=body.get("avatar");
//        //图片要小于2M的图片
//        return Result.Success(userService.updateAvatar(uid,avatar));
//    }


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
        return Result.Success(ok);
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
     * 用户关注其他用户，注意，不能关注自己
     */

    public Result<Boolean> followUser(){
        return null;
    }


}
