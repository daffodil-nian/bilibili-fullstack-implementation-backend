package org.arrinna.bilibilimockbackground.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatContact;
import org.arrinna.bilibilimockbackground.mapper.chat.ContactMapper;
import org.springframework.stereotype.Service;

@Service
public class ContactDao extends ServiceImpl<ContactMapper, ChatContact> {
    /**
     * 判断用户的会话列表中是否有这条记录，如果没有就加上
     * @param uid
     * @param roomId
     * @return
     */
    public ChatContact getByUidAndRoomId(Long uid,Long roomId){
        return lambdaQuery()
                .eq(ChatContact::getUid,uid)
                .eq(ChatContact::getRoomId,roomId)
                .one();
    }

}
