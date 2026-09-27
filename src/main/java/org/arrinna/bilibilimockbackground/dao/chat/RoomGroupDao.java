package org.arrinna.bilibilimockbackground.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoomGroup;
import org.arrinna.bilibilimockbackground.mapper.chat.RoomGroupMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomGroupDao extends ServiceImpl<RoomGroupMapper, ChatRoomGroup> {

//    写一个dao层的写法，跟据roomId获取room信息

    public ChatRoomGroup getByRoomId(Long roomId){

        return lambdaQuery()
                .eq(ChatRoomGroup::getRoomId,roomId)
                .eq(ChatRoomGroup::getDeleteStatus, DefaultConstant.ROOM_GROUP_STATUS_DELETED)
                .one();
    }

}
