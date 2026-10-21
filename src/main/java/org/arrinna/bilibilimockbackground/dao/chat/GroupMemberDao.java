package org.arrinna.bilibilimockbackground.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatGroupMember;
import org.arrinna.bilibilimockbackground.mapper.chat.GroupMemberMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMemberDao extends ServiceImpl<GroupMemberMapper, ChatGroupMember> {

    /**
     * 跟据roomId和uid获取群成员在这个群里的信息，可能会有NPR问题，需要注意一下
     * @param uid
     * @param groupId
     * @return
     */
    public ChatGroupMember getByGroupIdAndUid(Long uid,Long groupId){
        return lambdaQuery()
                .eq(ChatGroupMember::getGroupId,groupId)
                .eq(ChatGroupMember::getUid,uid)
                .one();
    }

    public List<Long> listActiveUids(Long groupId) {
        return lambdaQuery()
                .eq(ChatGroupMember::getGroupId, groupId)
                .ne(ChatGroupMember::getStatus, 3)
                .list()
                .stream()
                .map(ChatGroupMember::getUid)
                .toList();
    }
}
