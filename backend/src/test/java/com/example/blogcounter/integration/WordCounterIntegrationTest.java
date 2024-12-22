package com.example.blogcounter.integration;

import com.example.blogcounter.BlogWordCounterApplication;
import com.example.blogcounter.model.WordCount;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(
        classes = BlogWordCounterApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class WordCounterIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private final CompletableFuture<WordCount> completableFuture = new CompletableFuture<>();
    private static MockWebServer mockWebServer;

    @BeforeAll
    static void startMockServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("wordpress.api.base-url", () ->
                "http://localhost:" + mockWebServer.getPort() + "/wp-json/wp/v2");
        registry.add("wordpress.api.fetch-interval", () -> "1000");
    }

    @BeforeEach
    void setup() {
//        // Reset MockWebServer before each test
//        mockWebServer.enqueue(new MockResponse()
//                .setHeader("Content-Type", "application/json")
//                .setBody("[]"));

        // Setup WebSocket STOMP client
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @AfterAll
    static void cleanUp() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void shouldProcessBlogPostsAndSendWordCount() throws Exception {
        // Setup mock WordPress API response
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                    [
                        {
                            "id": 1,
                            "content": {
                                "rendered": "<p>This is a test blog post. This is a second sentence.</p>"
                            },
                            "date": "2024-01-01T12:00:00"
                        },
                        {
                            "id": 2,
                            "content": {
                                "rendered": "<p>Another test post with some content.</p>"
                            },
                            "date": "2024-01-01T12:01:00"
                        }
                    ]
                """));

        var handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                LOG.debug("after connected");
            }

            @Override
            public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {
                LOG.error("exception", ex);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable ex) {
                LOG.error("transportError", ex);
            }
        };

        // Connect to WebSocket
        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws/websocket", handler)
                .get(5, TimeUnit.SECONDS);

        // Subscribe to word count updates
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

        // Wait for and verify the word count
        WordCount wordCount = completableFuture.get(10, TimeUnit.SECONDS);
        Map<String, Integer> frequencies = wordCount.getWordFrequencies();

        // Verify expected word frequencies
        assertThat(frequencies)
                .containsEntry("this", 2)
                .containsEntry("is", 2)
                .containsEntry("a", 2)
                .containsEntry("test", 2)
                .containsEntry("blog", 1)
                .containsEntry("post", 2)
                .containsEntry("second", 1)
                .containsEntry("sentence", 1)
                .containsEntry("another", 1)
                .containsEntry("with", 1)
                .containsEntry("some", 1)
                .containsEntry("content", 1);

        // Verify WordPress API was called
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void shouldHandleEmptyResponse() throws Exception {
        // Setup mock WordPress API empty response
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("[]"));

        // Connect to WebSocket
        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws/websocket", new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        // Subscribe to word count updates
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

        // Wait for and verify empty word count
        WordCount wordCount = completableFuture.get(10, TimeUnit.SECONDS);

        assertThat(wordCount.getWordFrequencies()).isEmpty();
    }
}
