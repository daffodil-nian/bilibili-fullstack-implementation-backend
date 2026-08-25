package org.arrinna.bilibilimockbackground.controller;

import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.domain.dto.LoginDto;
import org.arrinna.bilibilimockbackground.domain.dto.RegisterDto;
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
    @PostMapping ("/login")
    public Result<UserInfoResp.UserBaseInfo> login(@RequestBody LoginDto loginDto){
        //然后直接调用service层，在service层中进行逻辑处理
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();

        return Result.Success(authService.login(username,password),DefaultConstant.REGISTER_FAIL_MSG);
    }
    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody RegisterDto registerDto){
        String username = registerDto.getUsername();
        String password = registerDto.getPassword();
        String checkPassword = registerDto.getCheckPassword();
        return Result.Success(authService.register(username,password,checkPassword), DefaultConstant.REGISTER_SUCCESS_MSG);
    }
    @PostMapping("/logout")
    public Result<Boolean> logout(){
        return Result.Success();
    }


}
