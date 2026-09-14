# Rabbitmq解决什么问题

首先是普通调用，A调用B，B必须在线而且要立刻处理

如果选 **MQ**，A可以把消息丢入到Broker中，然后B有空就从Broker中取。



# MQ核心名词

这里就以rabbitmq为例子吧！

| 名词     | 邮局类比 | 一句话                                                 |
| -------- | -------- | ------------------------------------------------------ |
| Producer |          | 这个是生产者，负责往Broker发消息应用代码               |
| Broker   |          | rabbitMq服务端，是消息暂存与路由的地方                 |
| Consumer |          | 从Broker中取消息的应用代码                             |
| Queue    |          | Broker中的信箱，可以理解为菜鸟驿站中存放包裹的柜子     |
| Binding  |          | Broker内的Exchange-Queue的规则，也可以理解为投递规则！ |
| Exchange |          | Broker中的分拣中心，可以理解为仓库分货的感觉           |

# 消息流转路径

```
## 2. 消息流转路径

目的：把核心名词串成一条线

路径：
Producer → Broker → Exchange → Binding → Queue → Consumer

口诀：寄 → 楼 → 分拣 → 规则 → 柜 → 取

三点：
1. 寄件人不直接塞柜子（不直连 Queue）
2. 消息在 Queue 里等 Consumer
3. Fanout = 所有绑定柜子各一份复印件

业务边界：
DB = 总仓（持久化）
MQ = 驿站通知（实时推）
先入库，再发 MQ
```



多说不用假把式，接下来给真实代码

这段代码可以理解成寄件人去哪个路由寄东西，fanout为空字符串，消息是new一个ImPushMessage，这个是自定义的，把消息发给id为2和6的

```java
// 寄件人：不关心柜子编号，只交给分拣台
rabbitTemplate.convertAndSend(
    "im_push_exchange",  // 分拣台名字（Exchange）
    "",                  // Fanout 不看面单号，传空串即可
    new ImPushMessage(List.of(2L, 6L), "{\"type\":\"CHAT_MSG\",\"content\":\"你好\"}")
);
```



这个是设置分拣台，

```java
@Configuration
public class DemoImMqConfig {

    // 分拣台：Fanout = 广播，人手一份复印件
    @Bean
    public FanoutExchange imPushExchange() {
        // 名字, durable是否持久, autoDelete是否自动删
        return new FanoutExchange("im_push_exchange", true, false);
    }

    // 柜子：每个实例一个临时柜（名字带随机串，避免撞柜）
    @Bean
    public Queue imPushQueue() {
        String name = "im_push_queue.demo." + UUID.randomUUID();
        // durable, exclusive, autoDelete
        return new Queue(name, false, true, true);
    }

    // 规则：这个柜子绑到这个分拣台
    @Bean
    public Binding imPushBinding(Queue imPushQueue, FanoutExchange imPushExchange) {
        return BindingBuilder.bind(imPushQueue).to(imPushExchange);
    }
}
```

然后这个是取件人，Consumer，代码如下

```java
@Component
public class DemoImPushConsumer {

    // "#{imPushQueue.name}" = 取上面那个 Queue Bean 的真实柜名
    @RabbitListener(queues = "#{imPushQueue.name}")
    public void receive(ImPushMessage message) {
        for (Long uid : message.getUidList()) {
            if (OnlineWsMap.isOnline(uid)) {
                OnlineWsMap.push(uid, message.getPayload()); // 开柜后喊用户
            }
            // 不在本机：忽略。真货在 DB，不靠这个柜存
        }
    }
}
```

需要用这个@RabbitListener监听imPushQueue，意思是取出指定的Queue Bean的真实名称。

然后就是获取消息中的uid，如果在线就给他推送消息

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImPushMessage implements Serializable {
    private List<Long> uidList; // 要通知谁
    private String payload;     // 已经拼好的 WS JSON
}
```

上面的代码是消息体

# Exchange四种类型

接下来我们详细讲讲这个四种类型，请看下面的这张表格

| EXCHANGE类型 | 如何理解                                                     |
| ------------ | ------------------------------------------------------------ |
| Direct       | routingKey要完完全全匹配才能走这条路                         |
| Fanout       | 只要绑定这个exchange交换机就可以发消息给它                   |
| Topic        | 按模式匹配（支持通配）                                       |
| Headers      | 规矩：不看面单号，看包裹上的标签（少用）。 例子：包裹上贴了 `易碎=是`、`生鲜=是`。你绑的条件是“易碎=是 且 生鲜=是”，那同时满足这两个标签的包裹才进你柜，跟面单号无关。 |

```java
// 分拣台：Direct
@Bean
public DirectExchange orderExchange() {
    return new DirectExchange("order_exchange");
}

// 柜子
@Bean
public Queue orderPayQueue() {
    return new Queue("order_pay_queue");
}

// 规则：柜子绑分拣台，并声明「我要的面单号」
@Bean
public Binding orderPayBinding(Queue orderPayQueue, DirectExchange orderExchange) {
    return BindingBuilder.bind(orderPayQueue)
            .to(orderExchange)
            .with("order.pay");   // binding key
}
```

首先这个是Direct的写法

```java
// 寄件：routingKey 必须和绑定的 key 完全一致
rabbitTemplate.convertAndSend("order_exchange", "order.pay", "支付成功");
// 写成 "order.paid" → 这个柜子收不到
```



然后在项目中测试一下吧！

```json
{
  "code": "CHAT_SEND",
  "targetUid": 6,
  "content": "你好"
}
```



https://www.zhihu.com/search?type=content&q=%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E6%BC%8F%E6%B4%9E