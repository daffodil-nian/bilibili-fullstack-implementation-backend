package org.arrinna.bilibilimockbackground.im;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.im.handler.HttpHeadersHandler;
import org.arrinna.bilibilimockbackground.im.handler.WebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author Arrinnna
 * 直接把代码粘贴过来即可
 */
@Component
@Slf4j

public class IMServer {

    @Value("${server.netty.port}")
    private int port;
    private EventLoopGroup bossGroup=new NioEventLoopGroup();
    private EventLoopGroup workerGroup=new NioEventLoopGroup();


    //考虑到websocketHandler和HttpHeadersHandler的复用，就写在外面吧

    private final HttpHeadersHandler httpHeadersHandler;

    private final WebSocketHandler webSocketHandler;

    public IMServer(HttpHeadersHandler httpHeadersHandler,WebSocketHandler webSocketHandler){
        this.httpHeadersHandler=httpHeadersHandler;
        this.webSocketHandler=webSocketHandler;
    }


    @PostConstruct
    public void start() throws InterruptedException{
        run();
    }

    public void run(){
        try {

            //服务端要启动，需要创建ServerBootStrap，
            // 在这里面netty把nio的模板式的代码都给封装好了
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup) //配置boss和worker线程
                    //配置Server的通道，相当于NIO中的ServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            //这个有顺序，首先是HTTP解码器，然后就是分块写处理器
                            //然后是HTTP消息聚合器
                            //聚合之后再加上自己的HTTP业务处理器
                            //差不多就可以开始Ws协议升级了！
                            //升级之后再用我自定义的ws业务处理器
                            socketChannel.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new HttpObjectAggregator(1024*64))
//                                  todo  这是自己的业务处理器，待完成。。。
                                    .addLast(httpHeadersHandler)
//                                    netty提供websocket的处理器,意思是在这个路径下都会转换升级遵循该协议，转换相关模式
                                    .addLast(new WebSocketServerProtocolHandler("/ws"))
                                    .addLast(webSocketHandler)
                            ; //添加handler，也就是具体的IO事件处理器

                        }

                    });
            ChannelFuture channelFuture=bootstrap.bind(port).sync();
            System.out.println("Netty Server Started,Listening on :"+port);
//            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();

        }
    }

    @PreDestroy
    public void destroy(){
        Future<?> future = bossGroup.shutdownGracefully();
        Future<?> future1 = workerGroup.shutdownGracefully();
        future.syncUninterruptibly();
        future1.syncUninterruptibly();
        log.info("关闭 ws server 成功");
    }
}
