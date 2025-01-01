package com.example.blogcounter.integration;

import com.example.blogcounter.model.WordCount;
import com.example.blogcounter.service.WordCountBroadcastService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WordCountBroadcastIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WordCountBroadcastService wordCountBroadcastService;

    @Test
    void testWordCountBroadcast() throws Exception {
        // WebSocket STOMP Client
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // CompletableFuture for receiving messages
        CompletableFuture<WordCount> completableFuture = new CompletableFuture<>();

        // Connect to WebSocket endpoint
        var session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws/websocket", new StompSessionHandlerAdapter() {
                })
                .get(5, TimeUnit.SECONDS);

        // Subscribe to `/topic/wordcount`
        session.subscribe("/topic/wordcount", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WordCount.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete((WordCount) payload);
            }
        });

        // Create test message and send it through the service
        WordCount wordCount = new WordCount(Map.of(
                "test", 2,
                "broadcast", 1
        ));
        wordCountBroadcastService.broadcastWordCount(wordCount);

        // await the receiving message
        WordCount receivedWordCount = completableFuture.get(10, TimeUnit.SECONDS);

        // check, if received correctly
        assertThat(receivedWordCount.getWordFrequencies())
                .containsEntry("test", 2)
                .containsEntry("broadcast", 1);
    }
}
