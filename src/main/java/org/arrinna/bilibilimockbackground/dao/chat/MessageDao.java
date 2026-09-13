package org.arrinna.bilibilimockbackground.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.chat.ChatMessage;
import org.arrinna.bilibilimockbackground.mapper.chat.MessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageDao extends ServiceImpl<MessageMapper,ChatMessage> {

    //TODO 根据房间号roomId 和时间查询没有读的记录
    //这里的倒数第二行写的是最大100条记录

    /**
     * 消息ID   内容               create_time（创建时间）
     * ─────────────────────────────────────────────
     *  1001    "在吗"             10:00
     *  1002    "在的"             10:01
     *  1003    "吃了吗"           10:05
     *  1004    "刚吃完"           10:06
     *  1005    "下午打球不"       10:10
     * @param roomId
     * @param afterTime
     * @param limit
     * @return
     */
    public List<ChatMessage> listUnread(Long roomId, LocalDateTime afterTime, int limit) {
        return lambdaQuery()
                .eq(ChatMessage::getRoomId, roomId)
                .gt(afterTime != null, ChatMessage::getCreateTime, afterTime)
                .orderByAsc(ChatMessage::getId)
                .last("limit " + Math.max(1, Math.min(limit, 100)))
                .list();
    }

    /**
     * 这个是拉某房间历史，按照倒叙获取limit条，但是感觉有点奇奇怪怪的。
     * @param roomId
     * @param limit
     * @return
     */
    public List<ChatMessage> listByRoomId(Long roomId,int limit){

        return lambdaQuery()
                .eq(ChatMessage::getRoomId,roomId)
                .orderByDesc(ChatMessage::getId)
                .last("limit " + Math.max(1, Math.min(limit, 100)))
                .list();
    }


}
