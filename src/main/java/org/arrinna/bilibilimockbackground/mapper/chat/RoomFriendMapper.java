package org.arrinna.bilibilimockbackground.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoomFriend;

@Mapper
public interface RoomFriendMapper extends BaseMapper<ChatRoomFriend> {
}
