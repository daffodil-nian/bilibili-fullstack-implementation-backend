package org.arrinna.bilibilimockbackground.common.util;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class GenerateUserDefaultNicknameTest {

    @Resource
    private GenerateUserDefaultNickname generateUserDefaultNickname;
    @Test
    void getInstance() {
    }

    @Test
    void nextUniqueName() {
        //我们在这里进行单元测试，看看是否能够达到我们的预期效果吧

        String name = generateUserDefaultNickname.nextUniqueName();
        System.out.println(name);
    }
}