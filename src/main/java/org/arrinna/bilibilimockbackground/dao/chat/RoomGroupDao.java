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
                .eq(ChatRoomGroup::getDeleteStatus, DefaultConstant.ROOM_GROUP_STATUS_NORMAL)
                .one();
    }

    /**
     * 根据roomIds一次性获取所有房间的信息
     * @param roomIds
     * @return
     */
    public List<ChatRoomGroup> listByRoomIds(List<Long> roomIds){
        if(roomIds.isEmpty()||roomIds==null){
            return List.of();
        }
        return lambdaQuery()
                .in(ChatRoomGroup::getRoomId,roomIds)
                .eq(ChatRoomGroup::getDeleteStatus,
                        DefaultConstant.ROOM_GROUP_STATUS_NORMAL)
                .list();
    }

    /**
     * 获取用户创建群聊的数量
     * @param ownerUid
     * @return
     */
    public long countCreatedByOwner(Long ownerUid){
        return lambdaQuery()
                .eq(ChatRoomGroup::getOwnerUid,ownerUid)
                .eq(ChatRoomGroup::getDeleteStatus,
                        DefaultConstant.ROOM_GROUP_STATUS_NORMAL)
                .count()
                ;
    }

    /**
     * 更新群头像
     * @param roomId
     * @param avatar
     * @return
     */
    public boolean updateGroupAvatar(Long roomId, String avatar){
        return lambdaUpdate()
                .eq(ChatRoomGroup::getRoomId,roomId)
                .eq(ChatRoomGroup::getDeleteStatus,0)
                .set(ChatRoomGroup::getAvatar,avatar)
                .update();
    }

    public boolean isGroupOwner(Long uid,Long groupId){
        return lambdaQuery()
                .eq(ChatRoomGroup::getOwnerUid,uid)
                .eq(ChatRoomGroup::getId,groupId)
                .one()!=null;
    }

}
