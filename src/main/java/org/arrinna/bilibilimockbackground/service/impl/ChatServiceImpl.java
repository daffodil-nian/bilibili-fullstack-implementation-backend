package org.arrinna.bilibilimockbackground.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.dao.chat.ContactDao;
import org.arrinna.bilibilimockbackground.dao.chat.MessageDao;
import org.arrinna.bilibilimockbackground.dao.chat.RoomDao;
import org.arrinna.bilibilimockbackground.dao.chat.RoomFriendDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatContact;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatMessage;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoom;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoomFriend;
import org.arrinna.bilibilimockbackground.domain.enums.MessageTypeEnum;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ChatServiceImpl implements IChatService {

    @Resource
    private RoomDao roomDao;
    @Resource
    private RoomFriendDao roomFriendDao;
    @Resource
    private ContactDao contactDao;
    @Resource
    private MessageDao messageDao;
    @Autowired
    private UserFollowDao userFollowDao;

    /**
     * 关注一个人就会收到他的自动回复，关注之后要先判断是否是互关，
     * 如果不是互关就要调用MessageDao,RoomFriendDao,
     * RoomDao和ContactDao
     * 关注成功
     *   → RoomDao 建房（没有才建）
     *   → RoomFriendDao 绑两个人
     *   → ContactDao 双方各一条
     *   → MessageDao 插一条自动消息（比如「我关注了你」）
     * @param type
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFriendSession(Long viewerUid, Long targetUid, int type){
        //1.首先确认好两个人的uid,如果有一方的uid是null或者两个参数值相等就返回创建失败，虽然也没必要这么写
        if(viewerUid==null||targetUid==null||viewerUid.equals(targetUid)){
            return null;
        }
        //2.然后比较两个人之间的大小，确认好顺序
        long uid1_min=Math.min(viewerUid,targetUid);
        long uid2_max=Math.max(viewerUid,targetUid);
        //3.准备好normal状态，我们开始吧,首先查看是否有这个单聊房，没有就new一个单聊房
        ChatRoomFriend chatRoomFriend=roomFriendDao.getFriendRecordByIds(uid1_min,uid2_max);

        //有单聊房（含：曾经互关又想取关、没有互关想互关和曾经取关又想互关效果是一样的）
        if(chatRoomFriend!=null){
            //3.1如果是取关想互关
            if(chatRoomFriend.getStatus()!=null&&chatRoomFriend.getStatus()== DefaultConstant.ROOM_FRIEND_STATUS_FORBIDDEN){
                chatRoomFriend.setStatus(DefaultConstant.ROOM_FRIEND_STATUS_NORMAL);
                chatRoomFriend.setUpdateTime(LocalDateTime.now());
                //更新数据库状态
                roomFriendDao.updateById(chatRoomFriend);
            }

            //考虑到判断它不为空的逻辑少一点，所以写入if语句里面
            ChatContact contact1=contactDao.getByUidAndRoomId(viewerUid,chatRoomFriend.getRoomId());
            ChatContact contact2=contactDao.getByUidAndRoomId(targetUid,chatRoomFriend.getRoomId());
            Long roomId=chatRoomFriend.getRoomId();
            ensureContact(targetUid,roomId);
            ensureContact(viewerUid,roomId);
            return roomId;

        }
        //4.然后new一个单聊房，并且写入数据库中
        ChatRoom room=roomDao.initRoom(type);
        roomDao.save(room);
        Long roomId=room.getId();//获得房间的唯一标识IDhahaha


        //5.接下来就是绑定两个人的关系，要关联chatRoomFriend表
        ChatRoomFriend friend = new ChatRoomFriend();
        friend.setRoomId(roomId);
        friend.setUid1(uid1_min);
        friend.setUid2(uid2_max);
        friend.setStatus(DefaultConstant.ROOM_FRIEND_STATUS_NORMAL);
        friend.setCreateTime(LocalDateTime.now());
        friend.setUpdateTime(LocalDateTime.now());

        //6.绑定之后就save吧哈哈
        roomFriendDao.save(friend);

        //7.接下来就把数据插入到contact这张表中
        ensureContact(uid1_min,roomId);
        ensureContact(uid2_max,roomId);

        //8.再然后就是在messageDao中插入一条消息
        /**
         */
        String tip = Boolean.TRUE.equals(userFollowDao.isMutualFollow(uid1_min, uid2_max)) ?
                DefaultConstant.DEFAULT_AUTO_RESPONSE : DefaultConstant.DEFAULT_SINGLE_RESPONSE;

        //然后就是插入到message表中，我们看看这张表里面有什么字段吧~
        //有room_id,from_uid,content,type,status,我们传room_id,from_uid,content这几个参数进去吧
        //这个首先要插入msg
        Long msgId=insertTextMessage(viewerUid,roomId,tip);

        //考虑到数据库里面不能每次打开会话都查信息表找最新消息，所以在新增消息的时候要主动把ID冗余存储到room表和contact表中

        //最后还是要把房间number返回的
        //要修改chat_room的last_msg_id和active_time属性和update_time ***最后一条消息id,
        // 最后一条消息时间和更新表的时间都要修改还有contact表的值也要改改！

        updateRoomAndContactsLastMsg(uid1_min,uid2_max,roomId,msgId);


        return roomId;
    }

    private void updateRoomAndContactsLastMsg(Long userId1,Long userId2,Long roomId,Long lastMsgId){
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

    private void touchContactLastMsg(Long uid,Long roomId,Long lastMsgId){
        ChatContact chatContact=contactDao.getByUidAndRoomId(uid,roomId);
        if(chatContact!=null){
            chatContact.setLastMsgId(lastMsgId);
            chatContact.setActiveTime(LocalDateTime.now());
            chatContact.setUpdateTime(LocalDateTime.now());
            contactDao.updateById(chatContact);
        }

    }

    /**
     * 撤回消息，
     *
     * @param viewerUid
     * @param targetUid
     * @param type
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disableFriendSession(Long viewerUid, Long targetUid, int type){
        //1.首先确认好两个人的uid,如果有一方的uid是null或者两个参数值相等就返回创建失败，
        // 虽然也没必要这么写
        if(viewerUid==null||targetUid==null||viewerUid.equals(targetUid)){
            return ;
        }
        //2.然后获取uid
        long uid1_min=Math.min(viewerUid,targetUid);
        long uid2_max=Math.max(viewerUid,targetUid);
        ChatRoomFriend chatRoomFriend=roomFriendDao.getFriendRecordByIds(uid1_min,uid2_max);

        //3.因为是取消朋友之间的状态，所以我们看看要改什么状态
        if(chatRoomFriend==null){
            return;
        }
        chatRoomFriend.setStatus(DefaultConstant.ROOM_FRIEND_STATUS_FORBIDDEN);
        chatRoomFriend.setUpdateTime(LocalDateTime.now());
        //最后更新就可以啦哈哈哈哈
        roomFriendDao.updateById(chatRoomFriend);
    }

    public Boolean createGroupSession(int type){

        return null;
    }

    /**
     * 这个是确保用户有联系记录
     * @param uid
     * @param roomId
     */
    private void ensureContact(Long uid,Long roomId){
        //1.如果发现在用户的会话列表中没有roomId就要创建一个，有的话就不用管
        if(contactDao.getByUidAndRoomId(uid,roomId)==null){
            //2.没有就创建一条记录昂
            saveContact(uid,roomId);
        }
    }
    public Long sendText(Long fromUid,Long targetUid,String text){
        return null;
    }
    public Long sendImage(){
        return null;
    }
    public Long sendVoice(){
        return null;
    }
    private void saveContact(Long uid,Long roomId){
        ChatContact contact=new ChatContact();
        contact.setUid(uid);
        contact.setRoomId(roomId);
        contact.setCreateTime(LocalDateTime.now());
        contact.setUpdateTime(LocalDateTime.now());
        contact.setActiveTime(LocalDateTime.now());
        contact.setReadTime(LocalDateTime.now());
        contactDao.save(contact);
    }
    private Long insertTextMessage(Long fromUid,Long roomId,String text){
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

}
