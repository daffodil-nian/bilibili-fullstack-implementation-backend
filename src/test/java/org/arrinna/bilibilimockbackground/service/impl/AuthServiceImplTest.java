package org.arrinna.bilibilimockbackground.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.arrinna.bilibilimockbackground.service.IAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class AuthServiceImplTest {


    @Autowired
    private IAuthService authService;

    @Test
    void login() {
        String username="testtest";
        String password="Aa@12345678";//这是密码
        UserInfoResp.UserBaseInfo res=authService.login(username,password);
        log.info("用户登录成功，返回信息为:{}",res);
    }

    @Test
    void register() {
        String username="ceshizhanghao1";
        String password="Aa@12345678";
        String checkPassword="Aa@12345678";
      authService.register(username,password,checkPassword);
      System.out.println("只因你太美");
    }

    @Test
    void testLogin() {
    }

    @Test
    void testRegister() {
    }

    @Test
    void testLogin1() {
    }

    @Test
    void testRegister1() {
    }
}