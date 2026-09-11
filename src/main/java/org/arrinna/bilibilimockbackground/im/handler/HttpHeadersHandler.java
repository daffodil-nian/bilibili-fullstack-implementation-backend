package org.arrinna.bilibilimockbackground.im.handler;

import cn.hutool.core.util.ReferenceUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.util.NettyUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@ChannelHandler.Sharable
public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {

    //连接地址长这样：ws://127.0.0.1:端口/ws?uid=1

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        if(msg instanceof FullHttpRequest req){
            QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
            List<String> uids=decoder.parameters().get("uid");//从请求头中获取到uid的参数
            if(uids==null || uids.isEmpty()){
                log.warn("uid is null，ws握手缺少uid，关闭连接");
                ctx.close();

                //释放资源,NETTY这份请求其实占用了内存，是内存引用计数
                ReferenceCountUtil.release(msg);
                return;
            }

            Long uid = Long.valueOf(uids.get(0));
            NettyUtil.setAttr(ctx.channel(),NettyUtil.UID,uid);

            req.setUri("/ws");
            log.info("握手携带 uid={}", uid);

        }
        ctx.fireChannelRead(msg);
    }
}
