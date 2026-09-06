package org.arrinna.bilibilimockbackground.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoomGroup;
@Mapper
public interface RoomGroupMapper extends BaseMapper<ChatRoomGroup> {
}
