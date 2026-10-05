package org.arrinna.bilibilimockbackground.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoom;
import org.arrinna.bilibilimockbackground.domain.enums.RoomTypeEnum;
import org.arrinna.bilibilimockbackground.mapper.chat.RoomMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RoomDao extends ServiceImpl<RoomMapper, ChatRoom> {

    /**
     * 初始化房间，如果关注了但是不是互关就要new一个房间
     * type要指定传入的参数
     * status默认为0
     * @return
     */
    public ChatRoom initRoom(int type){
        return ChatRoom.builder()
                .type(type)
                .status(DefaultConstant.ROOM_STATUS_NORMAL)
                .activeTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

}
