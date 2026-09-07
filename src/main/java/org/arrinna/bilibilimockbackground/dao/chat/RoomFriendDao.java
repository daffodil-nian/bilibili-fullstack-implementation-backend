package org.arrinna.bilibilimockbackground.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoomFriend;
import org.arrinna.bilibilimockbackground.mapper.chat.RoomFriendMapper;
import org.springframework.stereotype.Service;

@Service
public class RoomFriendDao extends ServiceImpl<RoomFriendMapper, ChatRoomFriend> {

    /**
     * 根据uid1和uid2获取好友记录
     * 不需要设置status的条件，等之后再判断
     * @param uid1
     * @param uid2
     * @return
     */
    public ChatRoomFriend getFriendRecordByIds(Long uid1, Long uid2) {

        return lambdaQuery()
                .eq(ChatRoomFriend::getUid1,uid1)
                .eq(ChatRoomFriend::getUid2,uid2)
                .one();
    }
}
