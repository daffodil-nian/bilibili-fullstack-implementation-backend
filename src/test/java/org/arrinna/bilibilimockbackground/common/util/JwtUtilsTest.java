package org.arrinna.bilibilimockbackground.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void createToken() {
        //我们看看token长啥样子吧
        String token = jwtUtils.createToken(1L);
        System.out.println("because you are so beautiful"+token);
        //这个token测试完后发现没问题
    }
}