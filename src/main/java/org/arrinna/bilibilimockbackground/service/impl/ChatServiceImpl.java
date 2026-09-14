package org.arrinna.bilibilimockbackground.service.impl;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.util.CosUtil;
import org.arrinna.bilibilimockbackground.dao.chat.ContactDao;
import org.arrinna.bilibilimockbackground.dao.chat.MessageDao;
import org.arrinna.bilibilimockbackground.dao.chat.RoomDao;
import org.arrinna.bilibilimockbackground.dao.chat.RoomFriendDao;
import org.arrinna.bilibilimockbackground.dao.user.UserDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatContact;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatMessage;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoom;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatRoomFriend;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.enums.MessageTypeEnum;
import org.arrinna.bilibilimockbackground.domain.vo.response.ChatMessageVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.ContactItemVO;
import org.arrinna.bilibilimockbackground.im.enums.WsPushTypeEnum;
import org.arrinna.bilibilimockbackground.im.push.ImPushGateway;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatServiceImpl implements IChatService {

    @Resource
    private UserDao userDao;
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
    @Resource
    private ImPushGateway imPushGateway;
    @Resource
    private CosUtil cosUtil;

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

    /**
     * 请让这个区别于insertTextMessage!
     * @param fromUid
     * @param targetUid
     * @param text
     * @return
     */

    @Override
    public Long sendText(Long fromUid, Long targetUid, String text){
        //1.判断参数是否合理昂
        if (fromUid==null||targetUid==null||fromUid.equals(targetUid)){
            return null;
        }
        //2.判断text是否OK
        if(text==null||text.isBlank()){
            return null;
        }
        //3.找到两个人的单聊房,如果没有房间或者状态是被禁止的就不OK
        long uid1=Math.min(fromUid,targetUid);
        long uid2=Math.max(fromUid,targetUid);

        ChatRoomFriend chatRoomFriend = roomFriendDao.getFriendRecordByIds(uid1,uid2);
        if(chatRoomFriend==null){
            log.warn("无会话 from={}  to={}",fromUid,targetUid);
            return null;
        }
        //todo
        if(chatRoomFriend.getStatus()==DefaultConstant.ROOM_FRIEND_STATUS_FORBIDDEN){
            log.info("会话被禁止 from={}  to={}",fromUid,targetUid);
            return null;
        }

        //4.接下来就可以获取房间ID了
        Long roomId = chatRoomFriend.getRoomId();
        ensureContact(uid1,roomId);
        ensureContact(uid2,roomId);

        //5.接下来写消息ing
        String content = text.trim();
        Long msgId = insertTextMessage(fromUid,roomId,content);


        //6.然后要更新room和contact的lastMsg！
        updateRoomAndContactsLastMsg(uid1,uid2,roomId,msgId);

        //7.在线则推送
        //先写入载荷中
        String payload = JSONUtil.createObj()
                .set("type", WsPushTypeEnum.CHAT_MSG.getCode())
                .set("msgId",msgId)
                .set("roomId",roomId)
                .set("fromUid",fromUid)
                .set("content",content)
                .toString();
//            OnlineWsMap.push(targetUid,payload);
            //开始push消息【在思考怎么写】
            imPushGateway.pushToUser(List.of(targetUid),payload);

        return msgId;
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

    /**
     * 这个是列举联系人的列表的信息
     * 首先得判断传入的参数是否合法，如果合法就根据uid获取contact表的数据
     * 并且，还要准备一个空的list用来接收结果
     * 然后就是看chatRoomFriend了，根据c中的roomId获取两个人之间的一个状态
     * @param uid
     * @return
     */
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
        List<ChatRoomFriend> friends = roomFriendDao.getByRoomIds(roomIds);


        //然后待会就可以筛选出来,跟据roomId映射
        Map<Long,ChatRoomFriend> friendMap=friends
                .stream().
                collect(Collectors.toMap(ChatRoomFriend::getRoomId, Function.identity()));

        //然后接下来就是根据friend之间的关系进一步筛选

        //3.接下来就是准备返回给前端的list
        List<ContactItemVO> result = new ArrayList<>();


        Set<Long> peerUidSet = new HashSet<>();
        Set<Long> lastMsgIdSet = new HashSet<>();



        //4.接下来就是把
        for(ChatContact c:contacts){
            //一条数据代表着一条和别人的记录
            ChatRoomFriend chatRoomFriend = roomFriendDao.getByRoomId(c.getRoomId());
            if(chatRoomFriend==null){
                continue;
                //如果没有就跳过
            }
            if(Objects.equals(chatRoomFriend.getStatus(),DefaultConstant.ROOM_FRIEND_STATUS_FORBIDDEN)){
                continue;
            }
            //找到同伴的uid
            Long peerUid = Objects.equals(chatRoomFriend.getUid1(),uid)?chatRoomFriend.getUid2():chatRoomFriend.getUid1();
            //找到了同伴的uid之后，就构造ContactItemVO
            ContactItemVO vo = new ContactItemVO();
            vo.setPeerUid(peerUid);
            vo.setRoomId(c.getRoomId());
            vo.setActiveTime(c.getActiveTime());
            vo.setLastMsgId(c.getLastMsgId());
//            vo.setPeerAvatar();

            //从chatMessage中获取最后一条消息
            if(c.getLastMsgId()!=null){
                ChatMessage last = messageDao.getById(c.getLastMsgId());
                if(last!=null)
                    vo.setLastContent(last.getContent());
            }
            if(peerUid!=null){
                peerUidSet.add(peerUid);
            }
            result.add(vo);
        }

        //
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
            User u = userMap.get (vo.getPeerUid ());
            if (u != null) {
                vo.setPeerNickname (u.getNickname ());
                vo.setPeerAvatar (cosUtil.toFullUrl (u.getAvatar ())); // 相对路径 → 完整 URL
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

    /**
     * 消息加载，这个功能是加载消息的方法
     * @param uid
     * @param roomId
     * @param limit
     * @return
     */
    @Override
    public List<ChatMessageVO> listMessages(Long uid, Long roomId, int limit){
        //这个方法实际只需要关联chatMessage和chatContact差不多应该没问题了
        if(uid==null || roomId==null){
            return List.of();//如果用户没登录或者房间被删了就return一个空列表
        }
        ChatContact mine = contactDao.getByUidAndRoomId(uid,roomId); //从我的视角下获取contact表信息
        if(mine==null){
            return List.of();//如果我的视角下没有这个房间，就return一个空列表
        }
        //因为limit一次有一个限制，所以得判断是否是合规的参数，如果是负数或者大于0就折中
        int size = (limit<=0 || limit>100)?50:limit;
        List<ChatMessage> list = messageDao.listByRoomId(roomId,size);

        Collections.reverse(list);//然后让记录反过来
//        ChatMessageVO vo= new ChatMessageVO();

        return list.stream()
                .map(m->{
                    ChatMessageVO vo = new ChatMessageVO();
                    vo.setMsgId(m.getId());
                    vo.setRoomId(m.getRoomId());
                    vo.setType(m.getType());
                    vo.setCreateTime(m.getCreateTime());
                    vo.setFromUid(m.getFromUid());
                    vo.setContent(m.getContent());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断一个人有没有读过一段记录，如果点进去聊天记录就要调用这个方法
     * @param uid
     * @param roomId
     */
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
    }

}
