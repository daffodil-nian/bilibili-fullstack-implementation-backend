package org.arrinna.bilibilimockbackground.im.mq;

import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.im.OnlineWsMap;
import org.arrinna.bilibilimockbackground.im.domain.dto.ImPushMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "bili.im.push-via-mq", havingValue = "true")
public class ImPushMessageConsumer {

    //这个是Spring AMQP的注解，位于spring-rabbit包中，只需要关注消息怎么处理即可

    /**
     * 发消息 → 先入库（永远）→ 再 MQ 尝试实时推
     * 上线   → 从 DB 拉未读/最近消息 → 再经 WS 推给前端（或 HTTP 返回给前端渲染）
     * @param message
     */
    @RabbitListener(queues = "#{imPushQueue.name}")
    public void receive(ImPushMessage message) {
        if (message == null || message.getUidList() == null) {
            return;
        }
        //mq接受到消息之后，就判断指定用户是否在线，如果在线就把消息push过去。
        //如果不在就不推送？？？
        for (Long uid : message.getUidList()) {
            if (uid == null) {
                continue;
            }
            if (OnlineWsMap.isOnline(uid)) {
                OnlineWsMap.push(uid, message.getPayload());
                log.info("MQ 消费后已推 WS, uid={}", uid);
            } else {
                log.info("uid={} 本机无连接，跳过（消息已在 DB）", uid);
            }
        }
    }
}
