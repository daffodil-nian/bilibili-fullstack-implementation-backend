package org.arrinna.bilibilimockbackground.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class PasswordUtilTest {

    @Test
    void encryptWithSalt() {
    }

    @Test
    void generateSalt() {
    }

    /**
     * 测试加密后的密码长啥样子，是否能看懂，测试结果表明看不出原密码
     */
    @Test
    void testPassword(){
        String password= "写自己的密码";

        String encryptPassword=PasswordUtil.encryptWithStaticSalt(password);
        System.out.println(encryptPassword);
        log.info("加密后的密码是:{}",encryptPassword);
        if(PasswordUtil.verifyWithStaticSalt(password,encryptPassword)){
            //如果是真的就输出你干嘛
            System.out.println("你干嘛");
        }
        System.out.println("小11");

    }
}