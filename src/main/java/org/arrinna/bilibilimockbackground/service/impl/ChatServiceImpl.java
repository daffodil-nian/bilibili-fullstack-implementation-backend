package org.arrinna.bilibilimockbackground.service.impl;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.COSFilePrefix;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.AssertUtil;
import org.arrinna.bilibilimockbackground.common.util.CosUtil;
import org.arrinna.bilibilimockbackground.common.util.FileUtil;
import org.arrinna.bilibilimockbackground.dao.chat.*;
import org.arrinna.bilibilimockbackground.dao.user.UserDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.chat.*;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.enums.group.GroupInviteResultEnum;
import org.arrinna.bilibilimockbackground.domain.enums.group.GroupRoleEnum;
import org.arrinna.bilibilimockbackground.domain.enums.MessageTypeEnum;
import org.arrinna.bilibilimockbackground.domain.enums.RoomTypeEnum;
import org.arrinna.bilibilimockbackground.domain.vo.chat.GroupInviteCardVO;
import org.arrinna.bilibilimockbackground.domain.vo.chat.GroupMemberVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.ChatMessageVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.ContactItemVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.GroupInfoVO;
import org.arrinna.bilibilimockbackground.im.enums.WsPushTypeEnum;
import org.arrinna.bilibilimockbackground.im.push.ImPushGateway;
import org.arrinna.bilibilimockbackground.manager.CosManager;
import org.arrinna.bilibilimockbackground.mapper.chat.RoomGroupMapper;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
    private RoomGroupDao roomGroupDao;
    @Resource
    private ContactDao contactDao;
    @Resource
    private MessageDao messageDao;
    @Resource
    private GroupMemberDao groupMemberDao;
    @Autowired
    private UserFollowDao userFollowDao;
    @Resource
    private ImPushGateway imPushGateway;
    @Resource
    private CosUtil cosUtil;
    @Resource
    private FileUtil fileUtil;
    @Autowired
    private RoomGroupMapper roomGroupMapper;
    @Resource
    private CosManager cosManager;
    @Value("${cos.client.host}")
    private String host;
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

//    public Boolean createGroupSession(int type){
//
//        return null;
//    }

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
    //TODO 待完善
    public Long sendImage(){
        return null;
    }
    //TODO 待完善
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
//        List<ChatRoomFriend> friends = roomFriendDao.getByRoomIds(roomIds);

        Map<Long,ChatRoom> roomMap=roomDao.listByIds(roomIds)
                .stream()
                .collect(
                        Collectors.toMap(ChatRoom::getId,Function.identity()))
                ;

        //然后待会就可以筛选出来,跟据roomId映射
        Map<Long,ChatRoomFriend> friendMap=roomFriendDao.getByRoomIds(roomIds)
                .stream().
                collect(Collectors.toMap(ChatRoomFriend::getRoomId, Function.identity()));

        Map<Long,ChatRoomGroup> groupMap= roomGroupDao.listByRoomIds(roomIds)
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

            if(Objects.equals(room.getType(),RoomTypeEnum.FRIEND.getType())){
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

        Set<Long> fromUids = list.stream()
                .map(ChatMessage::getFromUid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = Map.of();
        if (!fromUids.isEmpty()) {
            userMap = userDao.listByIds(fromUids).stream()
                    .collect(Collectors.toMap(User::getUId, u -> u, (a, b) -> a));
        }
        Map<Long, User> finalUserMap = userMap;
        return list.stream()
                .map(m->{
                    ChatMessageVO vo = new ChatMessageVO();
                    vo.setMsgId(m.getId());
                    vo.setRoomId(m.getRoomId());
                    vo.setType(m.getType());
                    vo.setCreateTime(m.getCreateTime());
                    vo.setFromUid(m.getFromUid());
                    vo.setContent(m.getContent());
//                    vo.setFromAvatar(m.get);
                    User u =finalUserMap.get(m.getFromUid());
                    if (u != null) {
                        vo.setFromAvatar(cosUtil.toFullUrl(u.getAvatar()));
                        vo.setFromNickname(u.getNickname());
                    }
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
        //然后
    }


    /**
     *
     * 在考虑撤回的代码要不要加上锁。。。
     * 而且有可能会出现重复撤回的情况，这个怎么预防？！
     * 我这个代码是否要引入redis，引入了redis之后能够给我的代码带来什么效果，撤回功能怎么办才能做到不会被反复撤回
     * 如果重复撤回，会给我带来什么
     *
     * @param userId
     * @param messageId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long userId,Long messageId){
        //1.首先校验参数是否正确，如果不正确就return false
        AssertUtil.isFalse(userId==null||messageId==null, ErrorCodeEnum.PARAM_ERROR);

        //2.然后就从数据库中查询相关信息！
        ChatMessage chatMessage = messageDao.getById(messageId);

        AssertUtil.isFalse(chatMessage==null,ErrorCodeEnum.MESSAGE_NOT_FOUND);

        //3.接下来判断是否是重复撤回的
        AssertUtil.isFalse(Objects.equals(chatMessage.getStatus(),DefaultConstant.MESSAGE_STATUS_RECALL),ErrorCodeEnum.RECALL_ALREADY);

        //4.如果过了时间也是不能撤回的！也就是说，如果现在的时间减去2大于创建时间就说明不能撤回
        AssertUtil.isFalse(
//                这个minusMinute是减去多少分钟的意思
                chatMessage.getCreateTime().isBefore(LocalDateTime.now().minusMinutes(DefaultConstant.MESSAGE_RECALL_LIMIT_MINUTES))
                ,ErrorCodeEnum.RECALL_TIMEOUT
        );

        //5.接下来所有情况都考虑到了，排查了chatMessages，排查了参数问题重复撤回还有是否过了撤回时间等问题，就可以开始干了
        assertCanRecallPrivate(userId,chatMessage);

        //6.然后要把撤回的消息通知给对方，首先要获取msg的所属的房间,接着跟据roomId获取到对方的uid
        ChatRoomFriend chatRoomFriend = roomFriendDao.getByRoomId(chatMessage.getRoomId());

        //7.接着给对方发消息，消息要推送到消息队列中去，但是和普通消息有区别，type值是CHAT_RECALL
        AssertUtil.isFalse(chatMessage==null || Objects.equals(chatRoomFriend.getStatus(),DefaultConstant.ROOM_FRIEND_STATUS_FORBIDDEN),ErrorCodeEnum.RECALL_SESSION_INVALID);

        //8.聊天对话是OK的就可以去更新了
       boolean ok= messageDao.recallOwnIfNormal(userId,messageId);
       //9.如果还是更新不成功，就说明已经撤回了，以防万一我们写一下这段代码
        AssertUtil.isFalse(!ok,ErrorCodeEnum.RECALL_ALREADY);

        //10.接下来可以构造载荷了，然后把消息推送到消息队列中去。
        /**
         * 载荷格式
         * String payload = JSONUtil.createObj()
         *                 .set("type", WsPushTypeEnum.CHAT_MSG.getCode())
         *                 .set("msgId",msgId)
         *                 .set("roomId",roomId)
         *                 .set("fromUid",fromUid)
         *                 .set("content",content)
         *                 .toString();
         */
        String payload = JSONUtil.createObj()
                .set("type",WsPushTypeEnum.CHAT_RECALL.getCode())
                .set("msgId",messageId)
                .set("roomId",chatRoomFriend.getRoomId())
                .set("operatorUid",userId)
                .toString();

        imPushGateway.pushToUser(List.of(chatRoomFriend.getUid1(),chatRoomFriend.getUid2()),payload);

        log.info("撤回消息成功！ operatorUid={}   msgId={}",userId,messageId);
    }

    private void assertCanRecallPrivate(Long operatorUid,ChatMessage msg){
        AssertUtil.isFalse(
                !Objects.equals(operatorUid,msg.getFromUid())
                ,ErrorCodeEnum.PRIVILEGE_ERROR
        );

    }

    //接下来增加表情包管理的功能，可以自定义增加表情包


    /**
     * 群聊发文本
     *顺序应该如下：
     * 先检验chat_room这张表中roomId房间的状态，如果OK往下走
     * 然后判断chat_room_group,单独整了这张数据库表，可以用来查看group群聊的详细信息
     * 接下来查看chat_group_member:这张表中存了group_id,uid,role等信息，
     * 可以跟据group.id和uid查询到某个群里的每个成员的一个职位
     *
     * 可以查看是否退群，是否被禁言之类的，如果都通过了就校验内容格式，通过就写入到数据库中去
     * @param fromUid
     * @param roomId
     * @param content
     */
    @Override
    public Long sendGroupText(Long fromUid, Long roomId, String content){
        //0.先校验参数是否合法,返回的是消息的
        if(fromUid==null||roomId==null||content==null||content.isBlank()){
            return null;
        }

        String text = content.trim();

        //1.chat_room
        ChatRoom chatRoom = roomDao.getById(roomId);
        AssertUtil.isFalse(chatRoom==null,ErrorCodeEnum.GROUP_ROOM_NOT_FOUND);
        AssertUtil.isFalse(!Objects.equals(chatRoom.getType(), RoomTypeEnum.GROUP.getType())
        ,ErrorCodeEnum.GROUP_ROOM_NOT_FOUND
        );
        AssertUtil.isFalse(Objects.equals(chatRoom.getStatus(),DefaultConstant.ROOM_STATUS_FORBIDDEN)
        ,ErrorCodeEnum.GROUP_ROOM_NOT_FOUND);

        // 2. chat_room_group：无资料 / 已解散
        ChatRoomGroup groupInfo = roomGroupDao.getByRoomId(roomId);
        boolean groupUnavailable = groupInfo == null
                || Objects.equals(groupInfo.getDeleteStatus(), DefaultConstant.ROOM_GROUP_STATUS_DELETED);
        AssertUtil.isFalse(groupUnavailable, ErrorCodeEnum.GROUP_ROOM_DISMISSED);


        //3.接下来判断chat_group_member：在这张表中可以判断一个用户是否被踢出去，看role和status字段
        ChatGroupMember chatGroupMember = groupMemberDao.getByGroupIdAndUid(fromUid,groupInfo.getId());
        boolean groupMemberStatus=chatGroupMember==null;
        AssertUtil.isFalse(groupMemberStatus||chatGroupMember.getStatus()==3
                ,ErrorCodeEnum.GROUP_NOT_MEMBER);
        //如果被禁言并且不是管理员就不让发言
        AssertUtil.isFalse(chatGroupMember.getStatus()==2&&chatGroupMember.getRole()==3,ErrorCodeEnum.GROUP_MUTED_MEMBER);

        Long msgId = insertTextMessage(fromUid,roomId,text);

        List<Long> memberUids = groupMemberDao.listActiveUids(groupInfo.getId());


        for (Long id : memberUids) {
            ensureContact(id, roomId);
            touchContactLastMsg(id, roomId, msgId);
        }
        ChatRoom toUpdate = roomDao.getById(roomId);
        if (toUpdate != null) {
            toUpdate.setLastMsgId(msgId);
            toUpdate.setActiveTime(LocalDateTime.now());
            toUpdate.setUpdateTime(LocalDateTime.now());
            roomDao.updateById(toUpdate);
        }
        List<Long> pushUids = memberUids.stream()
                .filter(id -> !id.equals(fromUid))
                .toList();
        String payload = JSONUtil.createObj()
                .set("type", WsPushTypeEnum.CHAT_MSG.getCode())
                .set("msgId", msgId)
                .set("roomId", roomId)
                .set("fromUid", fromUid)
                .set("content", text)
                .toString();

        //todo考虑用ImMessage替代这里的payLoad

        imPushGateway.pushToUser(pushUids, payload);
        return msgId;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createGroupSession(Long ownerUid, String name, List<Long> uids){
        //1.1检验参数
        AssertUtil.isFalse(ownerUid==null||name==null||uids==null,ErrorCodeEnum.PARAM_ERROR);

        //1.2检验uids中的人数是否够，不够也要抛出异常
        // 邀请人：去重、去掉空、去掉群主自己
        List<Long> invitees = uids.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.equals(ownerUid))
                .distinct()
                .toList();
        AssertUtil.isFalse(invitees.size()<2,ErrorCodeEnum.GROUP_MEMBER_FEW);

        int total = invitees.size() + 1; // +群主
        AssertUtil.isFalse(total > DefaultConstant.GROUP_MAX_MEMBERS_FREE, ErrorCodeEnum.GROUP_SIZE_EXCEED);

        // 1.2 之后、1.3 之前或之后都行
        List<Long> notFans = userFollowDao.filterNotFans(ownerUid, invitees);
        AssertUtil.isFalse(!notFans.isEmpty(), ErrorCodeEnum.GROUP_MEMBER_NOT_FAN);

        //1.3 然后看看自己创建的群聊是否很多,调用dao层
        AssertUtil.isFalse(roomGroupDao.countCreatedByOwner(ownerUid)>=
                DefaultConstant.GROUP_MAX_CREATE_PER_USER,ErrorCodeEnum.GROUP_CREATED_MANY);


        //2.1然后就可以创建群聊了，

        ChatRoom room=roomDao.initRoom(RoomTypeEnum.GROUP.getType());
        roomDao.save(room);
        Long roomId=room.getId();

        //2.2然后创建群信息表
        ChatRoomGroup group = new ChatRoomGroup();
        group.setRoomId(roomId);
        group.setName(name);
        group.setOwnerUid(ownerUid);
        group.setDeleteStatus(DefaultConstant.ROOM_GROUP_STATUS_NORMAL);
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        roomGroupDao.save(group);
        Long groupId = group.getId();
        // 2.3 群主 + 邀请的人 全部入群、全部有会话
        LocalDateTime now = LocalDateTime.now();
        List<ChatGroupMember> members = new ArrayList<>();
        //2.3然后就添加群成员
        ChatGroupMember owner = new ChatGroupMember();
        owner.setGroupId(group.getId());
        owner.setUid(ownerUid);
        owner.setRole(GroupRoleEnum.OWNER.getCode());
        owner.setStatus(1);
        owner.setCreateTime(LocalDateTime.now());
        owner.setUpdateTime(LocalDateTime.now());

        members.add(owner);
        for (Long uid : invitees) {
//            ChatContact contact=contactDao.getByUidAndRoomId(viewerUid,chatRoomFriend.getRoomId());

            ChatGroupMember m = new ChatGroupMember();
            m.setGroupId(groupId);
            m.setUid(uid);
            m.setRole(GroupRoleEnum.MEMBER.getCode());
            m.setStatus(1); // 在群
            m.setCreateTime(now);
            m.setUpdateTime(now);
            members.add(m);
        }
        groupMemberDao.saveBatch(members);

        //添加到联系表中！
        ensureContact(ownerUid, roomId);
        for (Long uid : invitees) {
            ensureContact(uid, roomId);
        }
        Long msgId = insertTextMessage(ownerUid, roomId, "群聊已创建");
        ChatRoom chatRoom = roomDao.getById(roomId);
        if (chatRoom != null) {
            chatRoom.setLastMsgId(msgId);
            chatRoom.setActiveTime(LocalDateTime.now());
            chatRoom.setUpdateTime(LocalDateTime.now());
            roomDao.updateById(chatRoom);
        }
        touchContactLastMsg(ownerUid, roomId, msgId);
        for (Long uid : invitees) {
            touchContactLastMsg(uid, roomId, msgId);
        }
        return roomId;
    }



    /**
     * 获取群信息
     * 跟据uid和roomId获取群消息
     * @param uid
     * @param roomId
     * @return
     */
    @Override
    public GroupInfoVO groupInfo(Long uid, Long roomId){

        ChatRoomGroup roomInfo= checkGroupInfo(uid,roomId);

        Long groupId=roomInfo.getId();


        List<Long> uidList=groupMemberDao.listActiveUids(groupId);
        Long count=uidList.stream().count();

        return GroupInfoVO
                .builder()
                .groupAvatar(cosUtil.toFullUrl(roomInfo.getAvatar()))
                .groupName(roomInfo.getName())
                .memberCount(count.intValue())
                .roomId(roomId)
                .userId(roomInfo.getOwnerUid())
                .notice(roomInfo.getNotice())
                .build()
                ;
    }

    /**
     * 修改群名
     * @param uid
     * @param roomId
     * @param name
     * @return
     */
    @Override
    public boolean updateGroupName(Long uid, Long roomId, String name){
        //1.先判断群的基本消息是否有问题
        ChatRoomGroup groupInfo=checkGroupInfo(uid,roomId);

        //2.然后修改群名，名称不能超过多长
        if(!checkParam(name,1,20)){
            return false;
        }
        //3.然后就看看自己是否有权限，这里封装一个方法
        Boolean isOk=canManage(groupInfo.getId(),uid);
        if(!isOk) return false;

        groupInfo.setName(name.trim());

        Boolean res=roomGroupDao.updateById(groupInfo);
        AssertUtil.isFalse(!res,ErrorCodeEnum.GROUP_OPERATE_FAIL);
        log.info("更新成功哈机密南北路都！{}",res);
        return res;
    }

    /**
     * 修改群公告
     * @param uid
     * @param roomId
     * @param notice
     * @return
     */
    @Override
    public boolean updateGroupNotice(Long uid, Long roomId, String notice){
        //1.先判断群的基本消息是否有问题
        ChatRoomGroup groupInfo=checkGroupInfo(uid,roomId);

        //2.然后修改群公告，公告不能超过多长
        if(!checkParam(notice,1,200)){
            return false;
        }
        groupInfo.setNotice(notice.trim());
        return roomGroupDao.updateById(groupInfo);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean dismissGroup(Long uid, Long roomId) {
        // 1. 首先判断群是否存在+ 我是成员
        ChatRoomGroup group = checkGroupInfo(uid, roomId);

        // 2. 只有群主能解散
        AssertUtil.isFalse(!checkIsOwner(uid, group.getId()), ErrorCodeEnum.GROUP_NOT_OWNER);


        // 3. 软删群，也就是批量操作数据库让其中的值发生，操作chat_room_group表让它的状态变化
        group.setDeleteStatus(DefaultConstant.ROOM_GROUP_STATUS_DELETED);
        group.setUpdateTime(LocalDateTime.now());

        //然后更新，如果更新失败就抛出更新失败的异常
        AssertUtil.isFalse(!roomGroupDao.updateById(group), ErrorCodeEnum.GROUP_OPERATE_FAIL);

        // 4. 所有成员置为已移除（一条 SQL）
//        groupMemberDao.lambdaUpdate()
//                .eq(ChatGroupMember::getGroupId, group.getId())
//                .set(ChatGroupMember::getStatus, DefaultConstant.MEMBER_STATUS_OUT)
//                .set(ChatGroupMember::getUpdateTime, LocalDateTime.now())
//                .update();

        Boolean isOk = groupMemberDao.batchRemoveUserOutOfGroup(group.getId());

        AssertUtil.isFalse(!isOk,ErrorCodeEnum.SYSTEM_ERROR);

        log.info("群已解散 roomId={} groupId={} operator={}", roomId, group.getId(), uid);
        return true;
    }

    @Override
    public String updateGroupAvatar(Long uid, Long roomId, MultipartFile avatar){
        //1.先判断群的基本消息是否有问题
        ChatRoomGroup groupInfo=checkGroupInfo(uid,roomId);

        //2.然后就判断这个avatar群头像是否符合要求
        fileUtil.checkImageFile(avatar,DefaultConstant.MAX_AVATAR_SIZE);

        //3.接下来就通过图片拼接成URL，并且同步更新到腾讯云对象存储bucket中:such as: group/picture

        String prefix= COSFilePrefix.GROUP_AVATAR_PREFIX;
        String avatarName= avatar.getOriginalFilename();
        String picturePath=fileUtil.buildFilePath(avatarName,prefix,roomId);

        //4.还得判断uid是不是这个群的群主，如果不是就不能修改群图片！
        AssertUtil.isFalse(!checkIsOwner(uid,groupInfo.getId()),ErrorCodeEnum.GROUP_NOT_OWNER);

        try {
            //转成文件类型
            File temp_file=File.createTempFile("group_avatar_", "");
            avatar.transferTo(temp_file);
            String url=host+picturePath;
            cosManager.putObject(picturePath,temp_file);

            //传入roomId
            boolean result=roomGroupDao.updateGroupAvatar(roomId,picturePath);

            //如果更新失败就不要执行后面的语句
            AssertUtil.isFalse(result==false,ErrorCodeEnum.UPLOAD_PIC_ERROR);

            log.info("上传成功！"+picturePath);
            return url;

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
//        return roomGroupDao.updateById(groupInfo);

    }


    private ChatRoomGroup checkGroupInfo(Long uid,Long roomId){
        //1.获取群信息，这里要改改，因为有些群不是单聊群，所以要进行修改

        ChatRoom existRoom = roomDao.getById(roomId);
        ChatRoomGroup roomInfo = roomGroupDao.getByRoomId(roomId);
//        if(existRoom!=null&&roomInfo==null){
//            return null;
//        }
        AssertUtil.isFalse(roomInfo==null&&existRoom==null,ErrorCodeEnum.GROUP_ROOM_DISMISSED);

        //2.然后判断uid是否是群成员，不是看不了！,关联chat_room_group表和chat_group_member这两张表
        Long groupId=roomInfo.getId();
        //3.ChatGroupMember member = groupMemberDao.getByRoomIdAndUid(uid,groupId);
        List<Long> uidList=groupMemberDao.listActiveUids(groupId);


        AssertUtil.isFalse(!uidList.contains(uid),ErrorCodeEnum.GROUP_NOT_MEMBER);
        return roomInfo;
    }



    private Boolean canManage(Long groupId,Long uid){

        ChatGroupMember member = groupMemberDao.getByGroupIdAndUid(uid,groupId);

        return member!=null&&(member.getRole()==DefaultConstant.ROOM_ROLE_OWNER||
                member.getRole()==DefaultConstant.ROOM_ROLE_MANAGER);
    }

    private Boolean checkParam(String content,int left,int right){
        if(content==null||content.length()<left||content.length()>right){
            return false;
        }
        return true;
    }


    /**
     * todo 待做
     * 邀请好友进群，这个功能待做
     * @param uid 操作用户的ID
     * @param roomId 房间/群组ID
 * @param inviteUids 被邀请用户的ID列表
     */
    @Transactional(rollbackFor = Exception.class) // 声明式事务注解，指定Exception类异常时回滚
    @Override // 标记该方法来自接口实现
    public boolean inviteGroupMember(Long uid, Long roomId, List<Long> inviteUids){

        //1.先判断操作人是谁
        if(uid==null||inviteUids.isEmpty()){ // 参数校验：检查操作用户ID是否为空或被邀请列表是否为空
            return false; // 这里应该返回一个明确的值，如false，而不是直接return
        }

        //2.接下来检查uid和roomId这两个参数是否合法
        ChatRoomGroup chatRoomGroup=checkGroupInfo(uid,roomId);
        if(chatRoomGroup==null){ // 用户是否在这间房间里面
            return false;
        }

        //3.接下来就判断uid是否超过最多的人数了
        Long groupId=chatRoomGroup.getId();
        //看的是群聊的用户uid
        List<Long> activeUids = groupMemberDao.listActiveUids(groupId);

        List<Long> realInvited=new ArrayList<>();
        //4.这里直接抛出异常噢！
        AssertUtil.isFalse(activeUids.size()+inviteUids.size()
                        > DefaultConstant.GROUP_MAX_MEMBERS_FREE,
                ErrorCodeEnum.GROUP_SIZE_EXCEED);

        AssertUtil.isFalse(inviteUids.size()
                        >DefaultConstant.GROUP_MAX_INVITE_PER_USER,
                ErrorCodeEnum.GROUP_MEMBER_INVITE_EXCEED_SIZE);

        //一次性查出“我和哪些人有单聊，这样效率更高”

        Map<Long, Long> peerRoomMap = roomFriendDao.listByUid(uid).stream()
                .collect(Collectors.toMap(
                        f -> Objects.equals(f.getUid1(), uid) ? f.getUid2() : f.getUid1(),
                        ChatRoomFriend::getRoomId,
                        (a, b) -> a));

        //5.接下来就是for循环遍历邀请他们来了！
        for (Long targetUid : inviteUids){
            if(targetUid==null||targetUid==uid||activeUids.contains(targetUid)){
                continue;
            }

            //3.2 接下来就要邀请别人进群了
            ChatGroupMember chatGroupMember=groupMemberDao.getByGroupIdAndUid(groupId,targetUid);
            if(chatGroupMember!=null){
                if(chatGroupMember.getStatus()==DefaultConstant.MEMBER_STATUS_PENDING){
                    continue;
                }
                //也就是说明这是之前被移除出去的
                chatGroupMember.setStatus(DefaultConstant.MEMBER_STATUS_PENDING);
                chatGroupMember.setRole(DefaultConstant.ROOM_ROLE_MEMBER);
                chatGroupMember.setUpdateTime(LocalDateTime.now());
                groupMemberDao.updateById(chatGroupMember);

                //然后把邀请的消息发送给对方，当作一条特殊消息，在我们这个系统中就是系统消息
            }
            //3.3 如果之前没有进群的就要邀请她等她通过就进群了
            else{
                // 这一步是插入新数据插入到这张表中
                 ChatGroupMember newMember = new ChatGroupMember();
                 newMember.setGroupId(groupId);
                 newMember.setUid(targetUid);
                 newMember.setRole(DefaultConstant.ROOM_ROLE_MEMBER);
                 newMember.setStatus(DefaultConstant.MEMBER_STATUS_PENDING);
                 newMember.setCreateTime(LocalDateTime.now());
                 newMember.setUpdateTime(LocalDateTime.now());

                 groupMemberDao.save(newMember);

            }
            realInvited.add(targetUid);
//            ensureContact(uid,roomId);
        }
        if(realInvited.isEmpty()){
            return true;//直接返回结果不要浪费时间了！
        }

        for (Long targetUid: realInvited){
            sendGroupInviteCard(uid,targetUid,roomId,chatRoomGroup);
        }
        //4.最后可以把消息推送到消息队列中去了！

        return true;
    }


    /**
     * 邀请别人进群的方法，能够把邀请卡片同步到message这张表中去
     * @param inviterUid
     * @param targetUid
     * @param roomId
     * @param roomGroup
     * @return
     */
    private Long sendGroupInviteCard(Long inviterUid,Long targetUid,Long roomId,ChatRoomGroup roomGroup){

        //1.首先拿到两个人之间的单聊会话！
        Long friendRoomId = ensureFriendRoom(inviterUid, targetUid); // 幂等，不解除拉黑、不发自动回复

        if(friendRoomId==null){
            log.warn("群邀请卡片发送失败！拿不到这条单聊对话{} -> {}",inviterUid,targetUid);
            return null;
        }

        //2.然后就组装JSON,需要群头像和

        User user = userDao.getUserByUID(inviterUid);
        String inviterName = user == null ? ("用户" + inviterUid) : user.getNickname();
        int memberCount = groupMemberDao.listActiveUids(roomGroup.getId()).size();

        GroupInviteCardVO vo=GroupInviteCardVO
                .builder()
                .inviterUid(inviterUid)
                .inviterName(inviterName)
                .memberCount(memberCount)
                .groupName(roomGroup.getName())
                .targetUid(targetUid)
                .roomId(roomId)
                .groupAvatar(roomGroup.getAvatar() == null ? null : cosUtil.toFullUrl(roomGroup.getAvatar()))
                .result(GroupInviteResultEnum.PENDING.getStatus())
                .build();
        //3.然后把消息输入到消息这个数据库中

        ChatMessage msg = new ChatMessage();
        msg.setRoomId(friendRoomId);
        msg.setFromUid(inviterUid);
        msg.setType(MessageTypeEnum.GROUP_INVITE.getCode());
        msg.setStatus(0);
        msg.setCreateTime(LocalDateTime.now());
        //群通知的卡片
        msg.setContent(JSONUtil.toJsonStr(vo));
        //4.接着就刷新双方的会话列表预览
        messageDao.save(msg);

        //5.最后更新消息
        updateRoomAndContactsLastMsg(inviterUid, targetUid, friendRoomId, msg.getId());

        //6.推送到消息队列中
        pushGroupInviteCard(targetUid,msg.getId(),friendRoomId,roomGroup.getRoomId(),roomGroup.getName(),inviterName);

        return msg.getId();//返回消息ID
    }


    /**
     * 发送系统消息方法，要同步通知到contact这张表 & room这张表
     * @param roomId
     * @param operatorUid
     * @param content
     */
    private void sendGroupSystemMessage(Long roomId, Long operatorUid, String content) {

        //new 一个ChatMessage这种对象
        ChatMessage msg = new ChatMessage();

        msg.setRoomId(roomId);
        msg.setFromUid(operatorUid);
        msg.setContent(content);
        msg.setType(MessageTypeEnum.SYSTEM.getCode());
        msg.setStatus(0);
        msg.setCreateTime(LocalDateTime.now());
        messageDao.save(msg);

        //然后哦消息同步更新给room这张表，
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

    /**
     * 这个代码是自己退出群聊的部分
     */
    @Override
    public boolean quitGroup(Long uid, Long roomId) {

        //1.首先还是检查群信息
        ChatRoomGroup group = checkGroupInfo(uid, roomId);
        AssertUtil.isFalse(checkIsOwner(uid, group.getId()), ErrorCodeEnum.GROUP_NOT_OWNER);

        //2.然后从chat_group_member中检查是否有我这个人的信息，没有就说明我不是这个群里的人
        ChatGroupMember me = groupMemberDao.getByGroupIdAndUid(uid, group.getId());
        AssertUtil.isFalse(me == null, ErrorCodeEnum.GROUP_NOT_MEMBER);

        //3.接下来还要判断是否是群主，是群主执行dismissGroup方法
        if(checkIsOwner(uid,group.getId())){
            dismissGroup(uid, roomId);
            return true;
        }

        me.setStatus(DefaultConstant.MEMBER_STATUS_OUT);
        me.setUpdateTime(LocalDateTime.now());
        AssertUtil.isFalse(!groupMemberDao.updateById(me), ErrorCodeEnum.GROUP_OPERATE_FAIL);

        User user = userDao.getUserByUID(uid);
        sendGroupSystemMessage(roomId, uid,
                (user == null ? ("用户" + uid) : user.getNickname()) + " 退出了群聊");
        return true;
    }

    /**
     * 移出群成员（群主/管理员）
     */
    public boolean tickGroupMember(Long uid, Long roomId, Long targetUid) {
        ChatRoomGroup group = checkGroupInfo(uid, roomId);

        AssertUtil.isTrue(Boolean.TRUE.equals(canManage(group.getId(), uid)),
                ErrorCodeEnum.GROUP_NOT_ADMIN);
        AssertUtil.isFalse(targetUid == null || targetUid.equals(group.getOwnerUid()),
                ErrorCodeEnum.GROUP_NOT_OWNER);

        ChatGroupMember target = groupMemberDao.getByGroupIdAndUid(targetUid, group.getId());
        AssertUtil.isFalse(target == null, ErrorCodeEnum.PARAM_ERROR);

        target.setStatus(DefaultConstant.MEMBER_STATUS_OUT);
        target.setUpdateTime(LocalDateTime.now());
        AssertUtil.isFalse(!groupMemberDao.updateById(target), ErrorCodeEnum.GROUP_OPERATE_FAIL);

        User tu = userDao.getUserByUID(targetUid);
        sendGroupSystemMessage(roomId, uid,
                (tu == null ? ("用户" + targetUid) : tu.getNickname()) + " 被移出了群聊");
        //
        return true;
    }

    /**
     * 被邀请人同意进群：status 4 → 1，此时才建群会话
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean acceptGroupInvite(Long uid, Long roomId, Long msgId) {
        ChatRoomGroup group = roomGroupDao.getByRoomId(roomId);
        AssertUtil.isFalse(group == null, ErrorCodeEnum.GROUP_ROOM_NOT_FOUND);
        Long groupId = group.getId();

        ChatGroupMember me = groupMemberDao.getByGroupIdAndUid(uid, groupId);
        AssertUtil.isFalse(me == null, ErrorCodeEnum.GROUP_NOT_MEMBER);
        AssertUtil.isFalse(!Objects.equals(me.getStatus(), DefaultConstant.MEMBER_STATUS_PENDING),
                ErrorCodeEnum.PRIVILEGE_ERROR);

        AssertUtil.isFalse(groupMemberDao.listActiveUids(groupId).size()
                >= DefaultConstant.GROUP_MAX_MEMBERS_FREE, ErrorCodeEnum.GROUP_SIZE_EXCEED);

        me.setStatus(DefaultConstant.MEMBER_STATUS_IN);
        me.setUpdateTime(LocalDateTime.now());
        AssertUtil.isFalse(!groupMemberDao.updateById(me), ErrorCodeEnum.GROUP_OPERATE_FAIL);

        ensureContact(uid, roomId);

        User user = userDao.getUserByUID(uid);
        sendGroupSystemMessage(roomId, uid,
                (user == null ? ("用户" + uid) : user.getNickname()) + " 加入了群聊");

        markInviteCard(msgId, GroupInviteResultEnum.ACCEPTED.getStatus());
        return true;
    }

    /**
     * 被邀请人拒绝：status 4 → 5
 * 该方法用于处理用户拒绝加入群组的请求
 * @param uid 用户的ID
 * @param roomId 群组房间ID
 * @param msgId 消息ID
 * @return 处理结果，成功返回true
     */
    @Override
    public boolean refuseGroupInvite(Long uid, Long roomId, Long msgId) {
    // 根据房间ID获取群组信息
        ChatRoomGroup group = roomGroupDao.getByRoomId(roomId);
    // 断言群组存在，不存在则抛出异常
        AssertUtil.isFalse(group == null, ErrorCodeEnum.GROUP_ROOM_NOT_FOUND);

    // 获取用户在群组中的成员信息
        ChatGroupMember me = groupMemberDao.getByGroupIdAndUid(uid, group.getId());
    // 断言用户是群组成员，不是则抛出异常
        AssertUtil.isFalse(me == null, ErrorCodeEnum.GROUP_NOT_MEMBER);
    // 断言用户状态为待处理状态，不是则抛出异常
        AssertUtil.isFalse(!Objects.equals(me.getStatus(), DefaultConstant.MEMBER_STATUS_PENDING),
                ErrorCodeEnum.PRIVILEGE_ERROR);

    // 更新用户状态为拒绝
        me.setStatus(DefaultConstant.MEMBER_STATUS_REFUSE);
    // 更新时间
        me.setUpdateTime(LocalDateTime.now());
    // 更新数据库中的成员信息，失败则抛出异常
        AssertUtil.isFalse(!groupMemberDao.updateById(me), ErrorCodeEnum.GROUP_OPERATE_FAIL);

    // 标记邀请消息为拒绝状态
        markInviteCard(msgId, GroupInviteResultEnum.REJECTED.getStatus());
    // 返回处理成功
        return true;
    }

    /**
     * 把邀请卡片 content 里的 result 改成 accepted / rejected
     */
    private void markInviteCard(Long msgId, String result) {
        if (msgId == null) {
            return;
        }
        try {
            ChatMessage msg = messageDao.getById(msgId);
            if (msg == null || !Objects.equals(msg.getType(),
                    MessageTypeEnum.GROUP_INVITE.getCode())) {
                return;
            }
            GroupInviteCardVO card = JSONUtil.toBean(msg.getContent(), GroupInviteCardVO.class);
            if (card == null) {
                return;
            }
            card.setResult(result);
            msg.setContent(JSONUtil.toJsonStr(card));
            messageDao.updateById(msg);
        } catch (Exception e) {
            log.warn("标记邀请卡片失败 msgId={}", msgId, e);
        }
    }

    /**
     *列举群成员的信息
     * @param uid
     * @param roomId
     * @return
     */
    @Override
    public List<GroupMemberVO> listGroupMembers(Long uid, Long roomId){
        //1.首先获取群聊信息
        ChatRoomGroup group = roomGroupDao.getByRoomId(roomId);
        if (group == null) {
            return List.of();
        }
        Long groupId = group.getId();
        //2.然后就是把每个人的信息遍历过来，在member这张表中获取groupId是多少的状态正常的包含角色的成员列表信息

        List<ChatGroupMember> members= groupMemberDao.lambdaQuery()
                .eq(ChatGroupMember::getGroupId,group.getId())
                .eq(ChatGroupMember::getStatus,1)
                .orderByAsc(ChatGroupMember::getRole)
                .list();

        //还有用户的uid信息
        List<Long> getUidList = groupMemberDao.listActiveUids(groupId);
        if (members.isEmpty()) {
            return List.of();
        }
        // 3. 直接从成员表拿 uid，不要再调 listActiveUids（就是它传错参数才 500 的）
        List<Long> uids = members.stream()
                .map(ChatGroupMember::getUid)
                .filter(Objects::nonNull)
                .toList();

        //然后用map组装起来
        Map<Long, User> userMap = uids.isEmpty()
                ? Map.of()
                : userDao.listByIds(uids).stream()
                .collect(Collectors.toMap(User::getUId, u -> u, (a, b) -> a));

        // 4. 组装 VO，最终把用户的信息返回
        List<GroupMemberVO> out = new ArrayList<>(members.size());
        for (ChatGroupMember m : members) {
            User u = userMap.get(m.getUid());
            out.add(GroupMemberVO.builder()
                    .uid(m.getUid())
                    .name(u == null ? ("用户" + m.getUid()) : u.getNickname())
                    .avatar(u == null || u.getAvatar() == null
                            ? null : cosUtil.toFullUrl(u.getAvatar()))
                    .role(m.getRole())
                    .status(m.getStatus())
                    .build());
        }
        return out;

    }

    /**
     * 判断指定的用户Uid是否是群主，只有群主才有资格解散
     * @param uid
     * @param groupId
     * @return
     */
    private boolean checkIsOwner(Long uid,Long groupId){
        return roomGroupDao.isGroupOwner(uid,groupId);
    }


    //TODO 还要写一个方法，确保两个人是否存在联系
    private Long ensureFriendRoom(Long uid1,Long uid2){
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


    private void pushGroupInviteCard(Long targetUid, Long cardMsgId, Long friendRoomId,
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

}
