package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.chat.GroupMemberVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.GroupInfoVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IChatRoomService {
    GroupInfoVO groupInfo(Long uid, Long roomId);

    Long sendGroupText(Long fromUid, Long roomId, String content);

    @Transactional(rollbackFor = Exception.class)
    Long createGroupSession(Long ownerUid, String name, List<Long> uids);

    boolean updateGroupName(Long uid, Long roomId, String name);

    boolean updateGroupNotice(Long uid, Long roomId, String notice);

    @Transactional(rollbackFor = Exception.class)
    boolean dismissGroup(Long uid, Long roomId);

    String updateGroupAvatar(Long uid, Long roomId, MultipartFile avatar);

    @Transactional(rollbackFor = Exception.class) // 声明式事务注解，指定Exception类异常时回滚
        // 标记该方法来自接口实现
    boolean inviteGroupMember(Long uid, Long roomId, List<Long> inviteUids);

    boolean quitGroup(Long uid, Long roomId);

    boolean tickGroupMember(Long uid, Long roomId, Long targetUid);

    @Transactional(rollbackFor = Exception.class)
    boolean acceptGroupInvite(Long uid, Long roomId, Long msgId);

    boolean refuseGroupInvite(Long uid, Long roomId, Long msgId);

    List<GroupMemberVO> listGroupMembers(Long uid, Long roomId);
}
