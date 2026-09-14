package org.arrinna.bilibilimockbackground.im.mq;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.im.domain.dto.ImPushMessage;
import org.arrinna.bilibilimockbackground.im.push.ImPushGateway;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
//如果配置文件这里是true就自动启动。
@ConditionalOnProperty(name = "bili.im.push-via-mq", havingValue = "true")
public class ImPushMessageProducer implements ImPushGateway {

    @Resource
    private RabbitTemplate rabbitTemplate;
    //fanout泛出不用key，所以你可以直接传入一个空的字符串就行，然后后面的就是msg消息体。

    @Override
    public void pushToUser(List<Long> uidList, String payLoad) {
        ImPushMessage msg = new ImPushMessage(uidList,payLoad);
        //这个就是把消息发送到rabbitmq中去
        rabbitTemplate.convertAndSend(ImMqConstant.IM_PUSH_EXCHANGE,"",msg);
        log.info("已投递 IM 推送消息到 MQ ,uids={}",uidList);
    }
}
