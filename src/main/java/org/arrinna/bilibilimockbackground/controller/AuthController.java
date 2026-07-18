package org.arrinna.bilibilimockbackground.controller;

import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.domain.dto.LoginDto;
import org.arrinna.bilibilimockbackground.domain.dto.RegisterDto;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.arrinna.bilibilimockbackground.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;
    /**
     * 登录接口
     * @param loginDto
     * @return
     */
    @GetMapping("/login")
    public Result<UserInfoResp> login(@RequestBody LoginDto loginDto){
        //然后直接调用service层，在service层中进行逻辑处理
        return Result.Success();
    }
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterDto registerDto){

        return Result.Success();
    }
    @PostMapping("/logout")
    public Result<Boolean> logout(){
        return Result.Success();
    }


}
