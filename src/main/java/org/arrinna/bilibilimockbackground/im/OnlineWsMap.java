package org.arrinna.bilibilimockbackground.im;


import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OnlineWsMap {


    private static final ConcurrentMap<Long, Channel> ONLINE=new ConcurrentHashMap<>();

    /**
     * 增
     * @param uid
     * @param channel
     */
    public static void put(Long uid,Channel channel){
        ONLINE.put(uid,channel);;
    }

    /**
     * 同一个人旧连接断开和新连接接上在时间上可能是错开的，
     * 如果只按照uid删，可能会把新连接删掉。
     * @param uid
     * @param channel
     */
    public static void remove(Long uid,Channel channel){
        ONLINE.computeIfPresent(uid, (k, old) -> old == channel ? null : old);
    }
    public static void push(Long uid,String json){
        Channel channel = ONLINE.get(uid);//根据ID获取channel
        if(channel!=null && channel.isActive()){
            //推帧
            channel.writeAndFlush(new TextWebSocketFrame(json));
        }
    }

    public static boolean isOnline(Long uid){
        Channel channel = ONLINE.get(uid);
        return channel!=null && channel.isActive();
    }

}
