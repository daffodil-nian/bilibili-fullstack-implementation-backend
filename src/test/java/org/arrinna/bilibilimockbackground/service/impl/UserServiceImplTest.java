package org.arrinna.bilibilimockbackground.service.impl;

import org.arrinna.bilibilimockbackground.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private IUserService userService;
    @Test
    void updateSignature() {
        boolean result = userService.updateSignature(1L,"天官赐福，百无禁忌");
        System.out.println(result+"yam");
    }

    @Test
    void updateNickname() {
    }

    @Test
    void updateAvatar() {
    }
}