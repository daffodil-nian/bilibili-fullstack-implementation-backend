package org.arrinna.bilibilimockbackground.service.chat;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.AssertUtil;
import org.arrinna.bilibilimockbackground.dao.chat.*;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.chat.*;
import org.arrinna.bilibilimockbackground.domain.enums.MessageTypeEnum;
import org.arrinna.bilibilimockbackground.domain.enums.RoomTypeEnum;
import org.arrinna.bilibilimockbackground.im.enums.WsPushTypeEnum;
import org.arrinna.bilibilimockbackground.im.push.ImPushGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ChatCommonHelper {

    @Resource
    private RoomDao roomDao;
    @Resource
    private RoomFriendDao roomFriendDao;
    @Resource
    private ContactDao contactDao;
    @Autowired
    private UserFollowDao userFollowDao;
    @Resource
    private MessageDao messageDao;
    @Resource
    private GroupMemberDao groupMemberDao;
    @Resource
    private RoomGroupDao roomGroupDao;

    @Resource
    private ImPushGateway imPushGateway;


    public void saveContact(Long uid,Long roomId){
        ChatContact contact=new ChatContact();
        contact.setUid(uid);
        contact.setRoomId(roomId);
        contact.setCreateTime(LocalDateTime.now());
        contact.setUpdateTime(LocalDateTime.now());
        contact.setActiveTime(LocalDateTime.now());
        contact.setReadTime(LocalDateTime.now());
        contactDao.save(contact);
    }
    public void ensureContact(Long uid,Long roomId){
        //1.如果发现在用户的会话列表中没有roomId就要创建一个，有的话就不用管
        if(contactDao.getByUidAndRoomId(uid,roomId)==null){
            //2.没有就创建一条记录昂
            saveContact(uid,roomId);
        }
    }


    public Long insertTextMessage(Long fromUid,Long roomId,String text){
        //1.首先我们看看这个message有哪些字段属性
        ChatMessage message=new ChatMessage();
        message.setRoomId(roomId);
        message.setFromUid(fromUid);
        message.setContent(text);
        message.setType(MessageTypeEnum.TEXT.getCode());
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        message.setStatus(DefaultConstant.MESSAGE_STATUS_NORMAL);
        messageDao.save(message);
        return message.getId();
    }

    public void updateRoomAndContactsLastMsg(Long userId1,Long userId2,Long roomId,Long lastMsgId){
        //1.首先把chat_room中的值改改
        ChatRoom chatRoom=roomDao.getById(roomId);
        //2.然后判断如果不是空就set值
        if(chatRoom!=null){
            chatRoom.setActiveTime(LocalDateTime.now());
            chatRoom.setLastMsgId(lastMsgId);
            chatRoom.setUpdateTime(LocalDateTime.now());
            roomDao.updateById(chatRoom);
        }
        //3.接下来修改contact表中的值
        touchContactLastMsg(userId1,roomId,lastMsgId);
        touchContactLastMsg(userId2,roomId,lastMsgId);
    }
    public void touchContactLastMsg(Long uid,Long roomId,Long lastMsgId){
        ChatContact chatContact=contactDao.getByUidAndRoomId(uid,roomId);
        if(chatContact!=null){
            chatContact.setLastMsgId(lastMsgId);
            chatContact.setActiveTime(LocalDateTime.now());
            chatContact.setUpdateTime(LocalDateTime.now());
            contactDao.updateById(chatContact);
        }

    }


    public ChatRoomGroup checkGroupInfo(Long uid, Long roomId){
        //1.获取群信息，这里要改改，因为有些群不是单聊群，所以要进行修改

        ChatRoom existRoom = roomDao.getById(roomId);
        ChatRoomGroup roomInfo = roomGroupDao.getByRoomId(roomId);
//        if(existRoom!=null&&roomInfo==null){
//            return null;
//        }
        AssertUtil.isFalse(roomInfo==null&&existRoom==null, ErrorCodeEnum.GROUP_ROOM_DISMISSED);

        //2.然后判断uid是否是群成员，不是看不了！,关联chat_room_group表和chat_group_member这两张表
        Long groupId=roomInfo.getId();
        //3.ChatGroupMember member = groupMemberDao.getByRoomIdAndUid(uid,groupId);
        List<Long> uidList=groupMemberDao.listActiveUids(groupId);


        AssertUtil.isFalse(!uidList.contains(uid),ErrorCodeEnum.GROUP_NOT_MEMBER);
        return roomInfo;
    }


    public void assertCanRecallPrivate(Long operatorUid,ChatMessage msg){
        AssertUtil.isFalse(
                !Objects.equals(operatorUid,msg.getFromUid())
                ,ErrorCodeEnum.PRIVILEGE_ERROR
        );

    }
    //TODO 还要写一个方法，确保两个人是否存在联系
    public Long ensureFriendRoom(Long uid1,Long uid2){
        if(uid1==null||uid2==null||uid1.equals(uid2)){
            return null;//不能让两个用户是一个人
        }
        long uidA = Math.min(uid1,uid2);
        long uidB = Math.max(uid1,uid2);

        //判断这一组朋友是否存在，存在就不需要改
        ChatRoomFriend exist = roomFriendDao.getFriendRecordByIds(uidA,uidB);

        Long roomId;

        if(exist!=null){
            roomId = exist.getRoomId();
        }
        else{
            ChatRoom room = roomDao.initRoom(RoomTypeEnum.FRIEND.getType());
            roomDao.save(room);
            roomId = room.getId();

            ChatRoomFriend friend = new ChatRoomFriend();
            friend.setRoomId(roomId);
            friend.setUid1(uid1);
            friend.setUid2(uid2);
            friend.setStatus(DefaultConstant.ROOM_FRIEND_STATUS_NORMAL);
            friend.setCreateTime(LocalDateTime.now());
            friend.setUpdateTime(LocalDateTime.now());
            roomFriendDao.save(friend);
            // ★ 注意：这里不发 insertTextMessage 那条自动回复
        }

        ensureContact(uidA, roomId);
        ensureContact(uidB, roomId);
        return roomId;
    }
    public void pushGroupInviteCard(Long targetUid, Long cardMsgId, Long friendRoomId,
                                     Long groupRoomId, String groupName, String inviterName) {
        try {
            String payload = JSONUtil.createObj()
                    .set("type", WsPushTypeEnum.GROUP_INVITE.getCode())
                    .set("msgId", cardMsgId)           // 卡片消息 id
                    .set("roomId", friendRoomId)       // 卡片落在哪个会话
                    .set("targetRoomId", groupRoomId)  // 要加入的群，前端调 accept 用
                    .set("groupName", groupName)
                    .set("inviterName", inviterName)
                    .toString();

            // ★ 推给这一个人 → 运输层的 for + isOnline 会处理在线判断
            imPushGateway.pushToUser(List.of(targetUid), payload);

        } catch (Exception e) {
            log.warn("群邀请推送失败 targetUid={}", targetUid, e);
        }
    }
    public void sendGroupSystemMessage(Long roomId, Long operatorUid, String content) {

        //new 一个ChatMessage这种对象
        ChatMessage msg = new ChatMessage();

        msg.setRoomId(roomId);
        msg.setFromUid(operatorUid);
        msg.setContent(content);
        msg.setType(MessageTypeEnum.SYSTEM.getCode());
        msg.setStatus(0);
        msg.setCreateTime(LocalDateTime.now());
        messageDao.save(msg);

        //然后哦消息同步更新给room这张表
        roomDao.lambdaUpdate()
                .eq(ChatRoom::getId, roomId)
                .set(ChatRoom::getLastMsgId, msg.getId())
                .set(ChatRoom::getActiveTime, LocalDateTime.now())
                .update();

        //最后更新contact这张表,然后更新
        contactDao.lambdaUpdate()
                .eq(ChatContact::getRoomId, roomId)
                .set(ChatContact::getLastMsgId, msg.getId())
                .set(ChatContact::getActiveTime, LocalDateTime.now())
                .update();
    }

    public Boolean canManage(Long groupId,Long uid){

        ChatGroupMember member = groupMemberDao.getByGroupIdAndUid(uid,groupId);

        return member!=null&&(member.getRole()==DefaultConstant.ROOM_ROLE_OWNER||
                member.getRole()==DefaultConstant.ROOM_ROLE_MANAGER);
    }
}
