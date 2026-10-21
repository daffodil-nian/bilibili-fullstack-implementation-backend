package org.arrinna.bilibilimockbackground.im.handler;


import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * 这个处理器是专门用于处理聊天的消息而设定的一个处理器
 */
public class ChatHandler {


    private static IChatService ChatService;

    public static void send(){

    }

    public static void withdraw(){
        //
        log.info("收到撤回请求，roomId={}，from={}，messageId={}");

    }

}
