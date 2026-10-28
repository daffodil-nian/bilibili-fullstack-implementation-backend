package org.arrinna.bilibilimockbackground.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.util.CosUtil;
import org.arrinna.bilibilimockbackground.dao.chat.*;
import org.arrinna.bilibilimockbackground.dao.user.UserDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.chat.*;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.enums.RoomTypeEnum;
import org.arrinna.bilibilimockbackground.domain.vo.response.ContactItemVO;
import org.arrinna.bilibilimockbackground.service.IChatContactService;
import org.arrinna.bilibilimockbackground.service.chat.ChatCommonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatContactServiceImpl implements IChatContactService {


    @Resource
    private RoomDao roomDao;
    @Resource
    private RoomGroupDao roomGroupDao;
    @Resource
    private RoomFriendDao roomFriendDao;
    @Resource
    private CosUtil cosUtil;
    @Resource
    private ContactDao contactDao;
    @Autowired
    private UserFollowDao userFollowDao;
    @Resource
    private MessageDao messageDao;
    @Resource
    private ChatCommonHelper chatCommonHelper;
    @Resource
    private UserDao userDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
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
            chatCommonHelper.ensureContact(targetUid,roomId);
            chatCommonHelper.ensureContact(viewerUid,roomId);
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
        chatCommonHelper.ensureContact(uid1_min,roomId);
        chatCommonHelper.ensureContact(uid2_max,roomId);

        //8.再然后就是在messageDao中插入一条消息
        /**
         */
        String tip = Boolean.TRUE.equals(userFollowDao.isMutualFollow(uid1_min, uid2_max)) ?
                DefaultConstant.DEFAULT_AUTO_RESPONSE : DefaultConstant.DEFAULT_SINGLE_RESPONSE;

        //然后就是插入到message表中，我们看看这张表里面有什么字段吧~
        //有room_id,from_uid,content,type,status,我们传room_id,from_uid,content这几个参数进去吧
        //这个首先要插入msg
        Long msgId=chatCommonHelper.insertTextMessage(viewerUid,roomId,tip);

        //考虑到数据库里面不能每次打开会话都查信息表找最新消息，所以在新增消息的时候要主动把ID冗余存储到room表和contact表中

        //最后还是要把房间number返回的
        //要修改chat_room的last_msg_id和active_time属性和update_time ***最后一条消息id,
        // 最后一条消息时间和更新表的时间都要修改还有contact表的值也要改改！

        chatCommonHelper.updateRoomAndContactsLastMsg(uid1_min,uid2_max,roomId,msgId);


        return roomId;
    }


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

    @Override
    public List<ContactItemVO> listContacts(Long uid){

        //1.uid为空就返回一个空的列表
        if(uid==null){
            return List.of();
        }
        //2.接下来可以获取contacts的一些信息了哈哈哈，时间靠前的优先。
        List<ChatContact> contacts = contactDao.listByUidOrderByActive(uid);

        if(contacts.isEmpty()){
            return List.of();
        }

        //3.接下来批量房间好友数量，我先把关系表画一下吧太复杂了
        List<Long> roomIds =contacts.stream()
                .map(ChatContact::getRoomId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        //只要在这个房间的friends都可以过来
//        List<ChatRoomFriend> friends = roomFriendDao.getByRoomIds(roomIds);

        Map<Long,ChatRoom> roomMap=roomDao.listByIds(roomIds)
                .stream()
                .collect(
                        Collectors.toMap(ChatRoom::getId, Function.identity()))
                ;

        //然后待会就可以筛选出来,跟据roomId映射
        Map<Long,ChatRoomFriend> friendMap=roomFriendDao.getByRoomIds(roomIds)
                .stream().
                collect(Collectors.toMap(ChatRoomFriend::getRoomId, Function.identity()));

        Map<Long, ChatRoomGroup> groupMap= roomGroupDao.listByRoomIds(roomIds)
                .stream()
                .collect(Collectors.toMap(ChatRoomGroup::getRoomId,Function.identity()))
                ;
        //然后接下来就是根据friend之间的关系进一步筛选

        //3.接下来就是准备返回给前端的list
        List<ContactItemVO> result = new ArrayList<>();
        Set<Long> peerUidSet = new HashSet<>();
        Set<Long> lastMsgIdSet = new HashSet<>();

        //4.
        for(ChatContact c:contacts){
            //一条数据代表着一条和别人的记录
            ChatRoom room = roomMap.get(c.getRoomId());
            if(room==null||Objects.equals(room.getStatus(),
                    DefaultConstant.ROOM_STATUS_FORBIDDEN)) continue;
            long unreadCount = messageDao.countUnread(c.getRoomId(),uid,c.getReadTime());
//
            ContactItemVO vo = new ContactItemVO();
//            vo.setPeerUid(peerUid);
            vo.setRoomId(c.getRoomId());
            vo.setActiveTime(c.getActiveTime());
            vo.setLastMsgId(c.getLastMsgId());
            vo.setRoomType(room.getType());
//            vo.setPeerAvatar();
            vo.setUnreadCount((int)unreadCount);
            //从chatMessage中获取最后一条消息
            if(c.getLastMsgId()!=null){
                ChatMessage last = messageDao.getById(c.getLastMsgId());
                if(last!=null)
                    vo.setLastContent(last.getContent());
            }

            if(Objects.equals(room.getType(), RoomTypeEnum.FRIEND.getType())){
                ChatRoomFriend friend=friendMap.get(c.getRoomId());
                if (friend == null
                        || Objects.equals(friend.getStatus(), DefaultConstant.ROOM_FRIEND_STATUS_FORBIDDEN)) {
                    continue;
                }
                Long peerUid = Objects.equals(friend.getUid1(),uid)?friend.getUid2():friend.getUid1();
                vo.setPeerUid(peerUid);
                if(peerUid!=null){
                    peerUidSet.add(peerUid);
                }
            }

            else if(Objects.equals(room.getType(),RoomTypeEnum.GROUP.getType())){
                ChatRoomGroup chatRoomGroup = groupMap.get(c.getRoomId());
                if(chatRoomGroup==null){
                    continue;
                }
                vo.setName(chatRoomGroup.getName());
                vo.setAvatar(chatRoomGroup.getAvatar()==null?null:
                        cosUtil.toFullUrl(chatRoomGroup.getAvatar()));

            }
            else{
                continue;
            }

            result.add(vo);
        }

        //然后构造完毕

        Map<Long, User> userMap=Map.of();
        if(!peerUidSet.isEmpty()){
            List<User> users = userDao.listByIds(peerUidSet);
            //然后匹配 ！
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getUId, u->u,(a,b)->a));
        }

        Map<Long,ChatMessage> msgMap = Map.of();
        if(!lastMsgIdSet.isEmpty()){
            List<ChatMessage> msgs = messageDao.listByIds (lastMsgIdSet);
            msgMap = msgs.stream ()
                    .collect (Collectors.toMap (ChatMessage::getId, m -> m, (a, b) -> a));
        }
        for (ContactItemVO vo : result) {

            if (vo.getPeerUid()!=null) {
                User u = userMap.get (vo.getPeerUid ());
                if(u!=null){
                    vo.setName (u.getNickname ());
                    vo.setAvatar (cosUtil.toFullUrl (u.getAvatar ()));
                }

            }
            if (vo.getLastMsgId () != null) {
                ChatMessage last = msgMap.get (vo.getLastMsgId ());
                if (last != null) {
                    vo.setLastContent (last.getContent ());
                }
            }
        }

        return result;
    }

    @Override
    public void markRead(Long uid, Long roomId){
        if(uid==null || roomId==null){return;}
        ChatContact chatContact = contactDao.getByUidAndRoomId(uid,roomId);
        if(chatContact==null){
            return;
        }
        chatContact.setReadTime(LocalDateTime.now());
        chatContact.setUpdateTime(LocalDateTime.now());

        //之所以不更新activeTime，是因为这个只是
        contactDao.updateById(chatContact);
        //然后
    }

//    private void ensureContact(Long uid,Long roomId){
//        //1.如果发现在用户的会话列表中没有roomId就要创建一个，有的话就不用管
//        if(contactDao.getByUidAndRoomId(uid,roomId)==null){
//            //2.没有就创建一条记录昂
//            saveContact(uid,roomId);
//        }
//    }
//
//    private void saveContact(Long uid,Long roomId){
//        ChatContact contact=new ChatContact();
//        contact.setUid(uid);
//        contact.setRoomId(roomId);
//        contact.setCreateTime(LocalDateTime.now());
//        contact.setUpdateTime(LocalDateTime.now());
//        contact.setActiveTime(LocalDateTime.now());
//        contact.setReadTime(LocalDateTime.now());
//        contactDao.save(contact);
//    }
//
//    private Long insertTextMessage(Long fromUid,Long roomId,String text){
//        //1.首先我们看看这个message有哪些字段属性
//        ChatMessage message=new ChatMessage();
//        message.setRoomId(roomId);
//        message.setFromUid(fromUid);
//        message.setContent(text);
//        message.setType(MessageTypeEnum.TEXT.getCode());
//        message.setCreateTime(LocalDateTime.now());
//        message.setUpdateTime(LocalDateTime.now());
//        message.setStatus(DefaultConstant.MESSAGE_STATUS_NORMAL);
//        messageDao.save(message);
//        return message.getId();
//    }
//
//    private void updateRoomAndContactsLastMsg(Long userId1,Long userId2,Long roomId,Long lastMsgId){
//        //1.首先把chat_room中的值改改
//        ChatRoom chatRoom=roomDao.getById(roomId);
//        //2.然后判断如果不是空就set值
//        if(chatRoom!=null){
//            chatRoom.setActiveTime(LocalDateTime.now());
//            chatRoom.setLastMsgId(lastMsgId);
//            chatRoom.setUpdateTime(LocalDateTime.now());
//            roomDao.updateById(chatRoom);
//        }
//        //3.接下来修改contact表中的值
//        touchContactLastMsg(userId1,roomId,lastMsgId);
//        touchContactLastMsg(userId2,roomId,lastMsgId);
//    }
//    private void touchContactLastMsg(Long uid,Long roomId,Long lastMsgId){
//        ChatContact chatContact=contactDao.getByUidAndRoomId(uid,roomId);
//        if(chatContact!=null){
//            chatContact.setLastMsgId(lastMsgId);
//            chatContact.setActiveTime(LocalDateTime.now());
//            chatContact.setUpdateTime(LocalDateTime.now());
//            contactDao.updateById(chatContact);
//        }
//
//    }
}
