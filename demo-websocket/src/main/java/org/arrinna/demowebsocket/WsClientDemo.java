package org.arrinna.demowebsocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** 服务端先启动，再跑这个 main */
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
                        error.printStackTrace();
                        latch.countDown();
                    }
                })
                .join();

        boolean ok = latch.await(5, TimeUnit.SECONDS);
        System.out.println(ok ? "[client] got reply" : "[client] timeout");
        System.exit(0);
    }
}