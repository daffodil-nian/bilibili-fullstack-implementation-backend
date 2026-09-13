package org.arrinna.bilibilimockbackground.im.mq;


import cn.hutool.core.lang.UUID;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "bili.im.push-via-mq", havingValue = "true")
public class ImMqConfig {
    @Value("${spring.application.name}")
    private String appName;

    /**
     * 消息推送交换机，实际上就是广播，把收到的消息无条件的复制一份，然后就投递给所有绑定的队列，
     * 如果MQ重启，消息还在
     * 而且不会自动清除的
     * @return
     */
    @Bean
    public FanoutExchange imPushExchange(){
        return new FanoutExchange(ImMqConstant.IM_PUSH_EXCHANGE,true,false);
    }
    /** 每台实例自己的队列，才能广播收到 */
    @Bean
    public Queue imPushQueue() {
        String name = ImMqConstant.IM_PUSH_QUEUE + "." + appName + "." + UUID.randomUUID();
        return new Queue(name, false, true, true);
    }

    /**
     * 这个是队列和交换机的绑定
     * @param imPushQueue
     * @param imPushExchange
     * @return
     */
    @Bean
    public Binding imPushBinding(Queue imPushQueue, FanoutExchange imPushExchange) {
        return BindingBuilder.bind(imPushQueue).to(imPushExchange);
    }
}
