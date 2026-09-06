package org.arrinna.demowebsocket.im;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 先启动 {@link DemoWebsocketApplication}，再单独运行本类 main，验证 ws://localhost:8081/ws
 */
public class WsClientDemo {

    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:8081/ws"), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        System.out.println("[client] connected");
                        webSocket.sendText("hi from java client", true);
                        webSocket.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        System.out.println("[client] recv: " + data);
                        latch.countDown();
                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        System.out.println("[client] error: " + error.getMessage());
                        latch.countDown();
                    }
                })
                .join();

        boolean ok = latch.await(5, TimeUnit.SECONDS);
        System.out.println(ok ? "[client] echo ok" : "[client] timeout, 确认服务端已启动且端口 8081");
        System.exit(ok ? 0 : 1);
    }
}
