package org.arrinna.bilibilimockbackground.controller;

import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/user")
@RestController
public class UserController {

    @Autowired
    private IUserService userService;
    /**
     * 用户修改签名
     */
    @PutMapping("/update/signature")
    public Result<Boolean> updateSignature(@RequestAttribute("uid") Long uid
    , @RequestBody Map<String,String> body){
        String signature=body.get("signature");
        return Result.Success(userService.updateSignature(uid,signature));
    }

    /**
     * 用户修改头像
     *
     */
    @PutMapping("/update/avatar")
    public Result<Boolean> updateAvatar(@RequestAttribute("uid") Long uid
    ,@RequestBody Map<String,String> body){
        String avatar=body.get("avatar");
        return Result.Success(userService.updateAvatar(uid,avatar));
    }

    /**
     * 用户修改昵称
     */
    @PutMapping("/update/nickname")
    public Result<Boolean> updateNickname(@RequestAttribute("uid") Long uid
            ,@RequestBody Map<String,String> body){
        String nickname=body.get("nickname");
        return Result.Success(userService.updateNickname(uid,nickname));
    }

    /**
     * 用户关注其他用户，注意，不能关注自己
     */

    public Result<Boolean> followUser(){
        return null;
    }


}
