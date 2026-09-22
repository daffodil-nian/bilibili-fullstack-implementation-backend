package org.arrinna.bilibilimockbackground.dao.chat;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
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
                .gt(afterTime != null, ChatMessage::getCreateTime, afterTime)//这个就是比较发消息和看到这条消息的时间如果不相等就是没看
                .orderByAsc(ChatMessage::getId)
                .last("limit " + Math.max(1, Math.min(limit, 100)))
                .list();
    }

    /**
     * 未读的数量
     * 注意，这个是给房间里不是我的用户发的
     * @param roomId
     * @param myUid
     * @param readTime
     * @return
     */
    public long countUnread(
            Long roomId,Long myUid, LocalDateTime readTime
    ){

        LocalDateTime after= readTime==null?
                LocalDateTime
                        .of(1970,1,1,0,0,0)
                :readTime;
        return lambdaQuery()
                .ne(ChatMessage::getFromUid,myUid)
                .eq(ChatMessage::getRoomId,roomId)
                .eq(ChatMessage::getStatus,DefaultConstant.MESSAGE_STATUS_NORMAL)
                .gt(ChatMessage::getCreateTime,after)//now > that_time
                .count();
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


    //只要跟据msg个yud来就可以撤回,就传入userId和messageId两个参数

    /**
     *
     * @param userId
     * @param messageId
     * @return
     */
    public Boolean recallOwnIfNormal(Long userId,Long messageId){

        return lambdaUpdate()
                .eq(ChatMessage::getStatus,DefaultConstant.MESSAGE_STATUS_NORMAL)
                .eq(ChatMessage::getFromUid,userId)
                .eq(ChatMessage::getId,messageId)
                .set(ChatMessage::getUpdateTime, DateTime.now())
                .set(ChatMessage::getStatus, DefaultConstant.MESSAGE_STATUS_RECALL)
                .update();
    }

    /**
     * 这是管理员撤回的dao层方法,不需要校验fromUid，只需要保证是正常状态
     * @return
     */
    public boolean recallByAdminIfNormal(Long messageId){

        return lambdaUpdate()
                .eq(ChatMessage::getId,messageId)
                .eq(ChatMessage::getStatus,DefaultConstant.MESSAGE_STATUS_NORMAL)
                .set(ChatMessage::getStatus, DefaultConstant.MESSAGE_STATUS_RECALL)
                .set(ChatMessage::getUpdateTime, DateTime.now())
                .update();
    }


}
