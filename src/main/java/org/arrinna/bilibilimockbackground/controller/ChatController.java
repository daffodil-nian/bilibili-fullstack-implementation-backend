package org.arrinna.bilibilimockbackground.controller;

import jakarta.annotation.Resource;
import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.domain.vo.response.ChatMessageVO;
import org.arrinna.bilibilimockbackground.domain.vo.response.ContactItemVO;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.springframework.web.bind.annotation.*;

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
}
