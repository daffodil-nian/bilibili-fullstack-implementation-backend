package org.arrinna.bilibilimockbackground.controller;

import jakarta.annotation.Resource;
import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.domain.vo.chat.GroupMemberVO;
import org.arrinna.bilibilimockbackground.domain.vo.request.CreateGroupReq;
import org.arrinna.bilibilimockbackground.domain.vo.request.GroupInviteMemberReq;
import org.arrinna.bilibilimockbackground.domain.vo.request.UpdateGroupNameReq;
import org.arrinna.bilibilimockbackground.domain.vo.request.UpdateGroupNoticeReq;
import org.arrinna.bilibilimockbackground.domain.vo.response.ChatMessageVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.ContactItemVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.GroupInfoVO;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

//todo to be done
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Resource
    private IChatService chatService;

    //聊天联系人
    //todo 测试接口：已测
    @GetMapping("/contacts")
    public Result<List<ContactItemVO>> contacts(@RequestAttribute("uid") Long uid){
        //获取用户的列表
        return Result.Success(chatService.listContacts(uid));
    }
    /**
     * 某房间历史消息
     * GET /api/chat/messages?roomId=1&limit=50
     * Header: uid
     */
    @GetMapping("/messages")
    public Result<List<ChatMessageVO>> messages(
            @RequestAttribute("uid") Long uid,
            @RequestParam("roomId") Long roomId,
            @RequestParam(defaultValue = "50") int limit
    ){

        return Result.Success(chatService.listMessages(uid, roomId, limit));
    }

    /**
     * todo 待完善ing
     * 撤回消息，MVP版
     * @param uid
     * @param body
     * @return
     */
    @PostMapping("/withdraw")
    public Result<Void> withdraw(
            @RequestAttribute("uid") Long uid,
            @RequestBody Map<String,Object> body
    ){
        if(body.get("messageId")==null){
            //todo 抛出异常，如果要撤回messageId为空就撤回不了
        }
        //todo 然后判断这个房间状态，如果房间被关撤回不了

        
        //todo 接着就是判断撤回方是谁，是发消息的本人不，还是管理员
        return Result.Success();
    }

    /**
     * 标记已读
     * POST /api/chat/read?roomId=1
     * Header: uid
     */
    @PostMapping("/read")
    public Result<Void> read(
            @RequestAttribute("uid") Long uid,
            @RequestParam Long roomId) {
        chatService.markRead(uid, roomId);
        return Result.Success(null, "ok");
    }
    /**
     * HTTP 发文本（方便 Postman 测；实时推送仍走 sendText → MQ）
     * POST /api/chat/send/text
     * Header: uid
     * Body: {"targetUid":6,"content":"hello"}
     */
    @PostMapping("/send/text")
    public Result<Long> sendText(
            @RequestAttribute("uid") Long uid,
            @RequestBody Map<String, Object> body) {
        if (body.get("targetUid") == null) {
            return Result.fail("targetUid 不能为空");
        }
        Long targetUid = Long.valueOf(body.get("targetUid").toString());
        String content = body.get("content") == null ? null : body.get("content").toString();
        Long msgId = chatService.sendText(uid, targetUid, content);
        if (msgId == null) {
            return Result.fail("发送失败（无会话/内容为空/会话禁用等）");
        }
        return Result.Success(msgId, "ok");
    }

    @PostMapping("/group/create")
    public Result<Long> createGroup(
            @RequestAttribute("uid") Long uid,
            @RequestBody CreateGroupReq req) {
        Long roomId = chatService.createGroupSession(uid, req.getName(), req.getMemberUids());
        if (roomId == null) {
            return Result.fail("建群失败");
        }
        return Result.Success(roomId, "ok");
    }
    @PostMapping("/send/group/text")
    public Result<Long> sendGroupText(
            @RequestAttribute("uid") Long uid,
            @RequestBody Map<String, Object> body) {
        if (body.get("roomId") == null) {
            return Result.fail("roomId 不能为空");
        }
        Long roomId = Long.valueOf(body.get("roomId").toString());
        String content = body.get("content") == null ? null : body.get("content").toString();
        Long msgId = chatService.sendGroupText(uid, roomId, content);
        if (msgId == null) {
            return Result.fail("发送失败");
        }
        return Result.Success(msgId, "ok");
    }

    /**
     * 群资料
     * GET /api/chat/group/{roomId}
     */
    @GetMapping("/group/{roomId}")
    public Result<GroupInfoVO> groupInfo(@RequestAttribute("uid") Long uid,
                                         @PathVariable Long roomId) {
        GroupInfoVO vo = chatService.groupInfo(uid, roomId);
        if (vo == null) {
            return Result.fail("群不存在或你不是群成员");
        }
        return Result.Success(vo, "查询成功！请即时接收！");
    }

    @GetMapping("/group/{roomId}/members")
    public Result<List<GroupMemberVO>> groupMembers(

            @RequestAttribute("uid") Long uid,
            @PathVariable Long roomId
    ){
        List<GroupMemberVO> list = chatService.listGroupMembers(uid,roomId);
        if(list==null){
            return Result.fail(404,"群不存在或你不是群成员");
        }
        return Result.Success(list,"查询成功！请即时接收！");
    }

    //finished!
    @PutMapping("/group/avatar/update")
    public Result<String> updateGroupAvatar(
            @RequestAttribute("uid") Long uid,
            @RequestParam("roomId") Long roomId,
            @RequestParam("file") MultipartFile file
    ){
        String url = chatService.updateGroupAvatar(uid,roomId,file);
        return Result.Success(url,"更新群头像成功！");
    }

    @PutMapping("/group/{roomId}/name")
    public Result<Boolean> updateGroupName(
            @RequestAttribute("uid") Long uid,
            @PathVariable Long roomId,
            @RequestBody UpdateGroupNameReq req
    ){
        String name = req.getName();
        Boolean res = chatService.updateGroupName(uid,roomId,name);
        return Result.Success(res,"更新群名成功！");
    }

    @PutMapping("/group/{roomId}/notice")
    public Result<Boolean> updateGroupNotice(
            @RequestAttribute("uid") Long uid,
            @PathVariable Long roomId,
            @RequestBody UpdateGroupNoticeReq req
    ){
        String notice = req.getNotice();
        Boolean res = chatService.updateGroupNotice(uid,roomId,notice);
        return Result.Success(res,"更新群公告成功！");
    }


    /**
     * 这个是解散群聊的接口
     * @return
     */
    public Result<Boolean> dissolveGroup(
            @RequestAttribute("uid") Long uid
    ){


        return null;
    }



    @PostMapping("/group/{roomId}/members")
    public Result<Boolean> inviteMembers(
            @RequestAttribute("uid") Long uid,
            @PathVariable Long roomId,
            @RequestBody GroupInviteMemberReq req
            ){

        List<Long> uidList = req == null ? null : req.getUids();
        if (uidList == null || uidList.isEmpty()) {
            return Result.fail("请选择要邀请的人");
        }
        if (!chatService.inviteGroupMember(uid, roomId, uidList)) {
            return Result.fail("邀请失败（人数已满或参数不合法）");
        }
        return Result.Success(true, "发送成功！请等待对方通过噢！");
    }


}
