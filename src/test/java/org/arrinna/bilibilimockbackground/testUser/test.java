package org.arrinna.bilibilimockbackground.testUser;

import jakarta.annotation.Resource;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class test {
    @Resource
    private UserFollowDao userFollowDao;

    @Test
    public void test(){
//        userFollowDao.Follow(1L,6l);
        //关注接口测试完毕
        userFollowDao.Follow(6L,1l);
        //关注接口测试完毕
    }

}
