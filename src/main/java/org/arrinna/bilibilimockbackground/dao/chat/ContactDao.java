package org.arrinna.bilibilimockbackground.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatContact;
import org.arrinna.bilibilimockbackground.domain.vo.response.ContactItemVO;
import org.arrinna.bilibilimockbackground.mapper.chat.ContactMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactDao extends ServiceImpl<ContactMapper, ChatContact> {
    /**
     * 判断用户的会话列表中是否有这条记录，如果没有就加上
     * 这个是获取用户的联系表
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
    //这个dao层的写法是获取uid为uid的所有记录，记录用户的所有房间号的ID信息，方便和room表关联
    public List<ChatContact> listByUid(Long uid) {
        return lambdaQuery()
                .eq(ChatContact::getUid, uid).list();
    }

    //再来一个方法listByUidOrderByActive

    public List<ChatContact> listByUidOrderByActive(Long uid){
        return lambdaQuery()
                .eq(ChatContact::getUid,uid)
                .orderByDesc(ChatContact::getActiveTime)
                .list();
    }

}
