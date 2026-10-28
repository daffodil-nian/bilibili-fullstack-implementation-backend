package org.arrinna.bilibilimockbackground.service.impl;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.COSFilePrefix;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.constant.RedisKey;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.AssertUtil;
import org.arrinna.bilibilimockbackground.common.util.CosUtil;
import org.arrinna.bilibilimockbackground.common.util.FileUtil;
import org.arrinna.bilibilimockbackground.common.util.RedisUtils;
import org.arrinna.bilibilimockbackground.dao.chat.*;
import org.arrinna.bilibilimockbackground.dao.user.UserDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.chat.*;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.enums.MessageTypeEnum;
import org.arrinna.bilibilimockbackground.domain.enums.RoomTypeEnum;
import org.arrinna.bilibilimockbackground.domain.enums.group.GroupInviteResultEnum;
import org.arrinna.bilibilimockbackground.domain.enums.group.GroupRoleEnum;
import org.arrinna.bilibilimockbackground.domain.vo.chat.GroupInviteCardVO;
import org.arrinna.bilibilimockbackground.domain.vo.chat.GroupMemberVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.GroupInfoVO;
import org.arrinna.bilibilimockbackground.im.constant.GroupConstant;
import org.arrinna.bilibilimockbackground.im.enums.WsPushTypeEnum;
import org.arrinna.bilibilimockbackground.im.push.ImPushGateway;
import org.arrinna.bilibilimockbackground.manager.CosManager;
import org.arrinna.bilibilimockbackground.service.IChatRoomService;
import org.arrinna.bilibilimockbackground.service.chat.ChatCommonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatRoomServiceImpl implements IChatRoomService {


    @Resource
    private ChatCommonHelper chatCommonHelper;
    @Resource
    private GroupMemberDao groupMemberDao;
    @Resource
    private RoomDao roomDao;
    @Resource
    private UserDao userDao;
    @Resource
    private RoomGroupDao roomGroupDao;
    @Resource
    private RoomFriendDao roomFriendDao;
    @Resource
    private ImPushGateway imPushGateway;
    @Resource
    private CosManager cosManager;
    @Value("${cos.client.host}")
    private String host;
    @Resource
    private FileUtil fileUtil;
    @Resource
    private CosUtil cosUtil;
    @Autowired
    private UserFollowDao userFollowDao;
    @Resource
    private MessageDao messageDao;


    @Override
    public GroupInfoVO groupInfo(Long uid, Long roomId){

        ChatRoomGroup roomInfo= chatCommonHelper.checkGroupInfo(uid,roomId);

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

    @Override
    public Long sendGroupText(Long fromUid, Long roomId, String content){
        //0.先校验参数是否合法,返回的是消息的
        if(fromUid==null||roomId==null||content==null||content.isBlank()){
            return null;
        }

        String text = content.trim();

        //1.chat_room
        ChatRoom chatRoom = roomDao.getById(roomId);
        AssertUtil.isFalse(chatRoom==null, ErrorCodeEnum.GROUP_ROOM_NOT_FOUND);
        AssertUtil.isFalse(!Objects.equals(chatRoom.getType(), RoomTypeEnum.GROUP.getType())
                ,ErrorCodeEnum.GROUP_ROOM_NOT_FOUND
        );
        AssertUtil.isFalse(Objects.equals(chatRoom.getStatus(), DefaultConstant.ROOM_STATUS_FORBIDDEN)
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

        Long msgId = chatCommonHelper.insertTextMessage(fromUid,roomId,text);

        String key = RedisUtils.getKey(RedisKey.RECALL_KEY, msgId);

        RedisUtils.set(key,fromUid, GroupConstant.RECALL_LIMIT_MINUTES, TimeUnit.MINUTES);

        List<Long> memberUids = groupMemberDao.listActiveUids(groupInfo.getId());


        for (Long id : memberUids) {
            chatCommonHelper.ensureContact(id, roomId);
            chatCommonHelper.touchContactLastMsg(id, roomId, msgId);
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
/**
 * 创建群聊会话
 * @param ownerUid 群主用户ID
 * @param name 群聊名称
 * @param uids 被邀请用户ID列表
 * @return 返回创建的群聊房间ID
 */
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
        chatCommonHelper.ensureContact(ownerUid, roomId);
        for (Long uid : invitees) {
            chatCommonHelper.ensureContact(uid, roomId);
        }
        Long msgId = chatCommonHelper.insertTextMessage(ownerUid, roomId, "群聊已创建");
        ChatRoom chatRoom = roomDao.getById(roomId);
        if (chatRoom != null) {
            chatRoom.setLastMsgId(msgId);
            chatRoom.setActiveTime(LocalDateTime.now());
            chatRoom.setUpdateTime(LocalDateTime.now());
            roomDao.updateById(chatRoom);
        }
        chatCommonHelper.touchContactLastMsg(ownerUid, roomId, msgId);
        for (Long uid : invitees) {
            chatCommonHelper.touchContactLastMsg(uid, roomId, msgId);
        }
        return roomId;
    }
    @Override
    public boolean updateGroupName(Long uid, Long roomId, String name){
        //1.先判断群的基本消息是否有问题
        ChatRoomGroup groupInfo=chatCommonHelper.checkGroupInfo(uid,roomId);

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
    @Override
    public boolean updateGroupNotice(Long uid, Long roomId, String notice){
        //1.先判断群的基本消息是否有问题
        ChatRoomGroup groupInfo=chatCommonHelper.checkGroupInfo(uid,roomId);

        //2.然后修改群公告，公告不能超过多长
        if(!checkParam(notice,1,200)){
            return false;
        }
        groupInfo.setNotice(notice.trim());
        return roomGroupDao.updateById(groupInfo);
    }

    private Boolean checkParam(String content,int left,int right){
        if(content==null||content.length()<left||content.length()>right){
            return false;
        }
        return true;
    }
    private Boolean canManage(Long groupId,Long uid){

        ChatGroupMember member = groupMemberDao.getByGroupIdAndUid(uid,groupId);

        return member!=null&&(member.getRole()==DefaultConstant.ROOM_ROLE_OWNER||
                member.getRole()==DefaultConstant.ROOM_ROLE_MANAGER);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean dismissGroup(Long uid, Long roomId) {
        // 1. 首先判断群是否存在+ 我是成员
        ChatRoomGroup group = chatCommonHelper.checkGroupInfo(uid, roomId);

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
        ChatRoomGroup groupInfo=chatCommonHelper.checkGroupInfo(uid,roomId);

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
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean inviteGroupMember(Long uid, Long roomId, List<Long> inviteUids){

        //1.先判断操作人是谁
        if(uid==null||inviteUids.isEmpty()){ // 参数校验：检查操作用户ID是否为空或被邀请列表是否为空
            return false; // 这里应该返回一个明确的值，如false，而不是直接return
        }

        //2.接下来检查uid和roomId这两个参数是否合法
        ChatRoomGroup chatRoomGroup=chatCommonHelper.checkGroupInfo(uid,roomId);
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
    private boolean checkIsOwner(Long uid,Long groupId){
        return roomGroupDao.isGroupOwner(uid,groupId);
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
        Long friendRoomId = chatCommonHelper.ensureFriendRoom(inviterUid, targetUid); // 幂等，不解除拉黑、不发自动回复

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
        chatCommonHelper.updateRoomAndContactsLastMsg(inviterUid, targetUid, friendRoomId, msg.getId());

        //6.推送到消息队列中
        chatCommonHelper.pushGroupInviteCard(targetUid,msg.getId(),friendRoomId,roomGroup.getRoomId(),roomGroup.getName(),inviterName);

        return msg.getId();//返回消息ID
    }

    /**
     * 这个代码是自己退出群聊的部分
     */
    @Override
    public boolean quitGroup(Long uid, Long roomId) {

        //1.首先还是检查群信息
        ChatRoomGroup group = chatCommonHelper.checkGroupInfo(uid, roomId);
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
        chatCommonHelper.sendGroupSystemMessage(roomId, uid,
                (user == null ? ("用户" + uid) : user.getNickname()) + " 退出了群聊");
        return true;
    }

    /**
     * todo  移出群成员（群主/管理员）
     * 首先梳理逻辑：
     * 1.检查群信息
     * 2.检查自己的身份是否是管理员
     * 3.移除群聊
     * 4.同步更新推送消息服务
     */
    @Override
    public boolean tickGroupMember(Long uid, Long roomId, Long targetUid) {
        //1.首先调用checkGroupInfo方法检查群信息[在我们计算机领域这是防御性编程]
        ChatRoomGroup group = chatCommonHelper.checkGroupInfo(uid, roomId);

        //2.判断自己是否是管理员
        AssertUtil.isTrue(Boolean.TRUE.equals(canManage(group.getId(), uid)),
                ErrorCodeEnum.GROUP_NOT_ADMIN);

        //todo 3.接下来就是判断要移出去的用户是否存在，如果不存在或者是群主就抛出异常，这里有待修改
        AssertUtil.isFalse(targetUid == null || targetUid.equals(group.getOwnerUid()),
                ErrorCodeEnum.GROUP_NOT_OWNER);

        //接下来就是判断这个人是否在这个群中
        ChatGroupMember target = groupMemberDao.getByGroupIdAndUid(targetUid, group.getId());
        AssertUtil.isFalse(target == null, ErrorCodeEnum.PARAM_ERROR);


        target.setStatus(DefaultConstant.MEMBER_STATUS_OUT);
        target.setUpdateTime(LocalDateTime.now());
        AssertUtil.isFalse(!groupMemberDao.updateById(target), ErrorCodeEnum.GROUP_OPERATE_FAIL);


        User tu = userDao.getUserByUID(targetUid);
        chatCommonHelper.sendGroupSystemMessage(roomId, uid,
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

        chatCommonHelper.ensureContact(uid, roomId);

        User user = userDao.getUserByUID(uid);
        chatCommonHelper.sendGroupSystemMessage(roomId, uid,
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


}
