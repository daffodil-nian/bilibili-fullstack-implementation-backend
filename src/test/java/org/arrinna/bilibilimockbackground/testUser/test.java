package org.arrinna.bilibilimockbackground.testUser;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserPrivacyReq;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSimpleVO;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSpaceVO;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class test {
    @Resource
    private UserFollowDao userFollowDao;
    @Resource
    private IUserService userService;

    /**
     * 关注接口测试完毕
     */
    @Test
    public void test(){
        //关注接口测试完毕
        for (long i = 2; i < 19; i++) {
            if(i!=6){
                userFollowDao.Follow(i,6l);
            }

        }
    }

    /**
     * 用户隐私接口测试完毕
     */
    @Test
    public void test_userPrivacy(){
        UserPrivacyReq req = userService.getMyPrivacy(6L);
        log.info("daffodil-nian");
        log.info(req.toString());
    }

    /**
     * 测试查看粉丝数，测试结果没问题
     */
    @Test
    public void test_ListFans(){
        List<UserSimpleVO> fans = userService.listFans(1L,6L,1,20);
        log.info(fans.toString());
    }

    /**
     * 测试查看关注数，测试结果没问题
     */
    @Test
    public void test_ListFollow(){
        List<UserSimpleVO> fans = userService.listFollows(1L,6L,1,20);
        log.info(fans.toString());
    }

    /**
     * 测试完成，没有问题~
     */
    @Test
    public void test_GetUserSpaceVO(){
        UserSpaceVO userSpaceVO = userService.getUserInfo(1L,6L);

        log.info(userSpaceVO.toString());
    }
}
