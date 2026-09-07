package org.arrinna.bilibilimockbackground.chat;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class testChat {

    @Resource
    private IChatService chatService;

    @Test
    public void testFriendChat(){

//        Long roomId=chatService.createFriendSession(1L, 6L, 2);
//        log.info("roomId:{}",roomId);

        for (long i = 2; i <19 ; i++) {
            if(i!=6){
                Long roomId_temp=chatService.createFriendSession(i, 6L, 2);
                log.info("roomId:{}",roomId_temp);
            }
        }
        for (long j = 1; j <6 ; j++) {
            if(j!=6){
                Long roomId_temp=chatService.createFriendSession(6L, j, 2);
                log.info("roomId:{}",roomId_temp);
            }
        }

    }

}
