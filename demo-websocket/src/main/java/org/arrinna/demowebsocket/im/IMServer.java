package org.arrinna.demowebsocket.im;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class IMServer {


    private int port=8081;
    private EventLoopGroup bossGroup=new NioEventLoopGroup();
    private EventLoopGroup workerGroup=new NioEventLoopGroup();
    public static final WebSocketServerHandler nettyWebSocketServerHandler= new WebSocketServerHandler();

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

                            socketChannel.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new HttpObjectAggregator(1024 * 64))
                                    // 先升级成 WebSocket，再进业务 Handler
                                    .addLast(new WebSocketServerProtocolHandler("/ws"))
                                    .addLast(nettyWebSocketServerHandler);

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
