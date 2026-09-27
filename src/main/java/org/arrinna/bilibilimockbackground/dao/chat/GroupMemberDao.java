package org.arrinna.bilibilimockbackground.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatGroupMember;
import org.arrinna.bilibilimockbackground.mapper.chat.GroupMemberMapper;
import org.springframework.stereotype.Service;

@Service
public class GroupMemberDao extends ServiceImpl<GroupMemberMapper, ChatGroupMember> {

    /**
     * 跟据roomId和uid获取群成员在这个群里的信息，可能会有NPR问题，需要注意一下
     * @param uid
     * @param roomId
     * @return
     */
    public ChatGroupMember getByRoomIdAndUid(Long uid,Long roomId){
        return lambdaQuery()
                .eq(ChatGroupMember::getGroupId,roomId)
                .eq(ChatGroupMember::getUid,uid)
                .one();
    }
}
