package org.arrinna.bilibilimockbackground.service.impl;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.constant.RedisKey;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.AssertUtil;
import org.arrinna.bilibilimockbackground.common.util.CosUtil;
import org.arrinna.bilibilimockbackground.common.util.RedisUtils;
import org.arrinna.bilibilimockbackground.dao.chat.*;
import org.arrinna.bilibilimockbackground.dao.user.UserDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.chat.*;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.vo.response.ChatMessageVO;
import org.arrinna.bilibilimockbackground.im.constant.GroupConstant;
import org.arrinna.bilibilimockbackground.im.enums.WsPushTypeEnum;
import org.arrinna.bilibilimockbackground.im.push.ImPushGateway;
import org.arrinna.bilibilimockbackground.mapper.chat.MessageMapper;
import org.arrinna.bilibilimockbackground.mapper.chat.RoomMapper;
import org.arrinna.bilibilimockbackground.service.IChatMessageService;
import org.arrinna.bilibilimockbackground.service.chat.ChatCommonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatMessageServiceImpl implements IChatMessageService {


    @Resource
    private RoomGroupDao roomGroupDao;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Resource
    private GroupMemberDao groupMemberDao;
    @Resource
    private UserDao userDao;
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
    private ChatCommonHelper chatCommonHelper;
    @Resource
    private CosUtil cosUtil;
    @Resource
    private ImPushGateway imPushGateway;

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
            AssertUtil.isFalse(chatRoomFriend==null, ErrorCodeEnum.PARAM_ERROR);
            return null;
        }
        //todo
        if(chatRoomFriend.getStatus()== DefaultConstant.ROOM_FRIEND_STATUS_FORBIDDEN){
            log.info("会话被禁止 from={}  to={}",fromUid,targetUid);
            return null;
        }

        //4.接下来就可以获取房间ID了
        Long roomId = chatRoomFriend.getRoomId();
        chatCommonHelper.ensureContact(uid1,roomId);
        chatCommonHelper.ensureContact(uid2,roomId);

        //5.接下来写消息ing
        String content = text.trim();
        Long msgId = chatCommonHelper.insertTextMessage(fromUid,roomId,content);
//        String key = String.format(RedisKey.RECALL_KEY,msgId);

        RedisUtils.set(RedisUtils.getKey(RedisKey.RECALL_KEY,msgId),String.valueOf(fromUid)
                , GroupConstant.RECALL_LIMIT_MINUTES, TimeUnit.MINUTES);


        //6.然后要更新room和contact的lastMsg！
        chatCommonHelper.updateRoomAndContactsLastMsg(uid1,uid2,roomId,msgId);

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
                    vo.setStatus(m.getStatus());
                    vo.setRecalled(Objects.equals(m.getStatus(),
                            DefaultConstant.MESSAGE_STATUS_RECALL));
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
        chatCommonHelper.assertCanRecallPrivate(userId,chatMessage);

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

    //修改状态，看看要不要判断是否是群聊
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean withdrawMessage(Long uid, Long messageId, Long roomId){
        //1.首先判断消息是否存在，这是硬性判断
        ChatMessage chatMessage = messageMapper.selectById(messageId);
        AssertUtil.isFalse(chatMessage==null,ErrorCodeEnum.MESSAGE_NOT_FOUND);

        AssertUtil.isFalse(!Objects.equals(chatMessage.getRoomId(),roomId),ErrorCodeEnum.PARAM_ERROR);

        //2.然后判断房间存在
        ChatRoom chatRoom = roomMapper.selectById(roomId);
        AssertUtil.isFalse(chatRoom==null,ErrorCodeEnum.ROOM_SESSION_NOT_FOUND);



        // ===== 3. 幂等：已撤回的直接拒 =====
        AssertUtil.isFalse(Objects.equals(chatMessage.getStatus(),
                DefaultConstant.MESSAGE_STATUS_RECALL), ErrorCodeEnum.RECALL_ALREADY);

        boolean isGroup = Objects.equals(chatRoom.getType(),1);


        //3.判断号类型之后按照房间类型分发
        if(isGroup){
            withdrawGroupMessage(uid,chatMessage,roomId);
        }
        else{
            withdrawFriendMessage(uid,chatMessage,roomId);
        }
        //先充数

        log.info("消息已撤回 messageId={} roomId={} roomType={} uid={}",
                messageId, roomId, chatRoom.getType(), uid);
        return true;

    }

    //todo 待修改
    private void withdrawGroupMessage(Long uid,ChatMessage message,Long roomId){

        ChatRoomGroup group = roomGroupDao.getByRoomId(roomId);

        Long groupId = group.getId();
//        AssertUtil.isFalse(!canManage(groupId,uid),ErrorCodeEnum.PRIVILEGE_ERROR);

        AssertUtil.isFalse(group==null||group.getDeleteStatus()==1,ErrorCodeEnum.GROUP_ROOM_DISMISSED);
        ChatGroupMember me = groupMemberDao.getByGroupIdAndUid(uid,groupId);
        AssertUtil.isFalse(Objects.equals(me.getStatus(), DefaultConstant.MEMBER_STATUS_OUT)
                || Objects.equals(me.getStatus(),
                DefaultConstant.MEMBER_STATUS_REFUSE),ErrorCodeEnum.RECALL_SESSION_INVALID);

        //权限分流
        boolean isSender = Objects.equals(uid,message.getFromUid());

        if(!isSender){
            //判断是否是管理员，这里要看chatGroupMember表
            AssertUtil.isFalse(!chatCommonHelper.canManage(groupId,uid),ErrorCodeEnum.PRIVILEGE_ERROR);
            //管理员可以随便撤回
            return;
        }
        //2.然后撤回消息，调用这个方法
        checkRecallable(message);
    }

    private void withdrawFriendMessage(Long uid,ChatMessage message,Long roomId){
        //1.判断是否是本人，如果不是没权限撤回！
        boolean isSender = Objects.equals(uid,message.getFromUid());
        AssertUtil.isFalse(!isSender,ErrorCodeEnum.PRIVILEGE_ERROR);
        checkRecallable(message);

    }//判断能否在规定时间内撤回某条消息
    private void checkRecallable(ChatMessage msg){

        Long msgId = msg.getId();
        Long now = System.currentTimeMillis();
        //接下来从redis中找值，看能不能找到对应的Key

        String key = RedisUtils.getKey(RedisKey.RECALL_KEY,msgId);
        String msgInfo=RedisUtils.get(key);
        AssertUtil.isFalse(msgInfo==null,ErrorCodeEnum.RECALL_TIMEOUT);

//        msg.setStatus(1);
        msg.setStatus(DefaultConstant.MESSAGE_STATUS_RECALL);
        msg.setContent("[消息已撤回]");
        msg.setUpdateTime(LocalDateTime.now());
        messageDao.updateById(msg);
        //最后修改消息的状态
        RedisUtils.del(key);


    }


}
