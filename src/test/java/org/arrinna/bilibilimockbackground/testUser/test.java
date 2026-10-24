package org.arrinna.bilibilimockbackground.testUser;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.vo.request.UserPrivacyReq;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSimpleVO;
import org.arrinna.bilibilimockbackground.domain.vo.user.UserSpaceVO;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
public class test {
    @Resource
    private UserFollowDao userFollowDao;
    @Resource
    private IUserService userService;
    @Resource
    private IChatService chatService;

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

    /**
     * 测试完毕，创建房间功能没问题，那看看能不能反复创建这个群呢？可以反复创建这个群
     */
    @Test
    public void test_chatService(){
        //1.首先
        List<Long> uidList = List.of(4L,5L);
        Long roomId=chatService.createGroupSession(6L,"测试群",uidList);
        log.info("您好~您的房间号为{}",roomId);

    }


    /**
     * 测试退群功能，测试结果没问题
     */
    @Test
    public void test_user_quitGroup(){
        boolean res=chatService.quitGroup(4L,23L);
        log.info("退群成功！ res={}",res);
    }

    @Test
    public void test_dismiss_group(){
        boolean res=chatService.dismissGroup(6L,20L);
        log.info("解散群聊成功！ res={}",res);
    }


}
