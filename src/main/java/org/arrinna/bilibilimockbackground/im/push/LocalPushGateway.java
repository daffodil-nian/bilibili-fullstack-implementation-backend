package org.arrinna.bilibilimockbackground.im.push;

import org.arrinna.bilibilimockbackground.im.OnlineWsMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name="bili.im.push-via-mq",havingValue = "false",matchIfMissing = true)
public class LocalPushGateway  implements ImPushGateway{

    @Override
    public void pushToUser(List<Long> uidList, String payLoad) {
        if(uidList==null){
            return;
        }
        //其实不需要判断uidList是否为空， 因为空了也执行不了for循环，如果该用户在线的话就可以push进去了
        for (Long uid:uidList){
            if(uid!=null&& OnlineWsMap.isOnline(uid)){
                OnlineWsMap.push(uid,payLoad);//更新值昂
            }
        }

    }
}
