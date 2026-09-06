package org.arrinna.bilibilimockbackground.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoom;

@Mapper
public interface RoomMapper extends BaseMapper<ChatRoom> {
}
