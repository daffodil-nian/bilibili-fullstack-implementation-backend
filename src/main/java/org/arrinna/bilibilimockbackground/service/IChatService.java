package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.chat.GroupMemberVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.ChatMessageVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.ContactItemVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.GroupInfoVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IChatService {

    Long createFriendSession(Long viewerUid, Long targetUid, int type);

    @Transactional(rollbackFor = Exception.class)
    void disableFriendSession(Long viewerUid, Long targetUid, int type);

    Long sendText(Long fromUid, Long targetUid, String text);

    List<ContactItemVO> listContacts(Long uid);

    List<ChatMessageVO> listMessages(Long uid, Long roomId, int limit);

    void markRead(Long uid, Long roomId);

    Long sendGroupText(Long fromUid, Long roomId, String content);

    //    @Transactional(rollbackFor = Exception.class)
    Long createGroupSession(Long ownerUid, String name, List<Long> uids);

    GroupInfoVO groupInfo(Long uid, Long roomId);

    boolean updateGroupName(Long uid, Long roomId, String name);

    boolean updateGroupNotice(Long uid, Long roomId, String notice);


    String updateGroupAvatar(Long uid, Long roomId, MultipartFile avatar);

    @Transactional(rollbackFor = Exception.class)
    boolean inviteGroupMember(Long uid, Long roomId, List<Long> inviteUid);

    List<GroupMemberVO> listGroupMembers(Long uid, Long roomId);
}
