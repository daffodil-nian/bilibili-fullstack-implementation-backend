package org.arrinna.bilibilimockbackground.im.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.util.NettyUtil;
import org.arrinna.bilibilimockbackground.im.OnlineWsMap;
import org.arrinna.bilibilimockbackground.im.enums.WsCommandEnum;
import org.arrinna.bilibilimockbackground.im.enums.WsPushTypeEnum;
import org.arrinna.bilibilimockbackground.service.IChatService;
import org.arrinna.bilibilimockbackground.service.impl.OfflineMsgPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@ChannelHandler.Sharable
@Component
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /** 静态持有，靠下面 setter 注入；不要用 @Resource/@Autowired 标在 static 字段上 */
    private static IChatService chatService;

    private static OfflineMsgPushService offlineMsgPushService;

    @Autowired
    public void setChatService(IChatService chatService) {
        WebSocketHandler.chatService = chatService;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //1.事件触发器
        /**
         * 流程：
         * 浏览器连上 ws://.../ws?uid=1
         *   → Headers 已把 uid=1 贴在 Channel 上
         *   → 协议升级成功，Netty 抛出 HandshakeComplete
         *   → 你取出 uid=1
         *   → OnlineWsMap：1 → 这条 Channel
         *   → 日志「上线」
         */

        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            //如果握手完毕，也就是说从channel获取到uid了，然后就可以开始通信了。
            Long uid = NettyUtil.getAttr(ctx.channel(),NettyUtil.UID);
            OnlineWsMap.put(uid,ctx.channel());
            log.info("用户uid={} 上线",uid);
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 这个写法其实很有趣，首先服务器收到文本帧，然后解析命令，接下来发消息，最后回ACK
     * @param channelHandlerContext
     * the {@link ChannelHandlerContext}
     * which this {@link SimpleChannelInboundHandler}
     * belongs to
     * @param textWebSocketFrame           the message to handle
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        //1.获取uid是谁
        Long fromUid = NettyUtil.getAttr(channelHandlerContext.channel(), NettyUtil.UID);

        // 例如：{"code":"CHAT_SEND","targetUid":6,"content":"hello"}

        /**
         * 载荷
         *         String payload = JSONUtil.createObj()
         *                 .set("type", WsPushTypeEnum.CHAT_MSG.getCode())
         *                 .set("msgId",msgId)
         *                 .set("roomId",roomId)
         *                 .set("fromUid",uid1)
         *                 .set("content",content)
         *                 .toString();
         */
        String text = textWebSocketFrame.text();

        log.info("uid={} ，您收到一则新的消息:{}", fromUid,text);

        //2.处理消息【也就是这里的解析消息】
        JSONObject obj = JSONUtil.parseObj(text);//又是我们老朋友hutool工具哈哈哈
        WsCommandEnum cmd =WsCommandEnum.of(obj.getStr("code"));

        if(cmd!=WsCommandEnum.CHAT_SEND){
            //没有类型就new一个错误的类型
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.createObj()
                            .set("type", WsPushTypeEnum.ERROR.getCode())
                            .set("msg","unsupported code")
                            .toString()
            ));
            return; //怕写脏数据
        }


        //3.发消息【调用chatService中的send方法，只需要传入参数】
        //public Long sendText(Long fromUid,Long targetUid,String text)
        //就可以获得msgId
        Long targetUid = obj.getLong("targetUid");
        String content = obj.getStr("content");
        Long msgId = chatService.sendText(fromUid, targetUid, content);

        //4.推消息，返回ACK!
        channelHandlerContext.writeAndFlush(
                new TextWebSocketFrame(
                        JSONUtil.createObj()
                                .set("type",WsPushTypeEnum.ACK.getCode())
                                .set("msgId",msgId)
                                .toString()
                )
        );

    }

    //除了连接写消息以外，还有连接断开，下线注销的功能
    //
    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        Long uid = NettyUtil.getAttr(ctx.channel(),NettyUtil.UID);
        if(uid!=null){
            //1.移除
            OnlineWsMap.remove(uid,ctx.channel());
            log.info("用户下线:{}",uid);

        }

        ctx.fireChannelInactive();

    }

    /**
     * 设置离线消息推送服务
     * @param offlineMsgPushService
     */
    @Autowired
    public void setOfflineMsgPushService(OfflineMsgPushService offlineMsgPushService){
        WebSocketHandler.offlineMsgPushService= offlineMsgPushService;
    }
}
