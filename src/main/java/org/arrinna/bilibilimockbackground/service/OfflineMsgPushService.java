package org.arrinna.bilibilimockbackground.service;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.dao.chat.ContactDao;
import org.arrinna.bilibilimockbackground.dao.chat.MessageDao;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatContact;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatMessage;
import org.arrinna.bilibilimockbackground.im.OnlineWsMap;
import org.arrinna.bilibilimockbackground.im.enums.WsPushTypeEnum;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OfflineMsgPushService {

    @Resource
    private ContactDao contactDao;
    @Resource
    private MessageDao messageDao;

    public void pushOffAfterOnline(Long uid){
        //1.首先校验参数，判断是否不合法，像null和不在线都可以算是不合法
        if(uid==null || !OnlineWsMap.isOnline(uid)) return;
        //2.然后就用for循环遍历所有好友
        for(ChatContact c:contactDao.listByUid(uid)){
            //根据uid获取列表信息
            List<ChatMessage> list = messageDao.listUnread(c.getRoomId(),c.getReadTime(),50);

            //然后遍历消息ing
            for (ChatMessage m:list){
                if(uid.equals(m.getFromUid())) continue;
                String payload = JSONUtil
                        .createObj()
                        .set("type", WsPushTypeEnum.CHAT_MSG.getCode())
                        .set("msgId",m.getId())
                        .set("roomId",m.getRoomId())
                        .set("fromUid",m.getFromUid())
                        .set("content",m.getContent())
                        .toString();
                OnlineWsMap.push(uid,payload);
            }
        }
        log.info("uid={} 上线补推完成",uid);
    }
}
