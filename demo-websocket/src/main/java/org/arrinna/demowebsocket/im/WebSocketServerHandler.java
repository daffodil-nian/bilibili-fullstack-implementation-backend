package org.arrinna.demowebsocket.im;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            System.out.println("握手完成");
            log.info("握手完成123455");
        }
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("连接建立: {}", ctx.channel().id().asShortText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info("连接断开: {}", ctx.channel().id().asShortText());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String msg = frame.text();
        log.info("收到消息: {}", msg);
        log.info("通道111"+ctx.channel());//写的是通道的编号
        ctx.channel().writeAndFlush(new TextWebSocketFrame("server echo: " + msg));
    }
}
