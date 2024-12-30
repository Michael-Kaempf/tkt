package com.example.blogcounter.integration;

import com.example.blogcounter.BlogWordCounterApplication;
import com.example.blogcounter.model.WordCount;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Ignore;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(
//        classes = BlogWordCounterApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class WordCounterIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private final List<CompletableFuture<WordCount>> wordCountFutures = new ArrayList<>();
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
        registry.add("wordpress.api.posts-per-page", () -> "10");
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

        wordCountFutures.clear();
    }

    @AfterAll
    static void cleanUp() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void shouldProcessBlogPostsAndUpdatePeriodically() throws Exception {
        // First response with initial posts
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                [
                    {
                        "id": 1,
                        "content": {
                            "rendered": "<p>Initial blog post.</p>"
                        },
                        "date": "2024-01-01T12:00:00"
                    }
                ]
            """));

        // Second response with updated posts
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                [
                    {
                        "id": 2,
                        "content": {
                            "rendered": "<p>New blog post added.</p>"
                        },
                        "date": "2024-01-01T12:01:00"
                    }
                ]
            """));

        // Connect to WebSocket
        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws/websocket", new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        // Create futures for updates
        CompletableFuture<WordCount> firstUpdate = new CompletableFuture<>();
        CompletableFuture<WordCount> secondUpdate = new CompletableFuture<>();
        wordCountFutures.add(firstUpdate);
        wordCountFutures.add(secondUpdate);

        session.subscribe("/topic/wordcount", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WordCount.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                for (CompletableFuture<WordCount> future : wordCountFutures) {
                    if (!future.isDone()) {
                        future.complete((WordCount) payload);
                        break;
                    }
                }
            }
        });

        // Wait for first update
        WordCount firstWordCount = firstUpdate.get(5, TimeUnit.SECONDS);

        // Verify first request URL
        RecordedRequest firstRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(firstRequest).isNotNull();
        assertThat(firstRequest.getPath()).isNotNull();
        assertThat(firstRequest.getPath())
                .startsWith("/wp-json/wp/v2/posts")
                .contains("per_page=10")
                .contains("after=1970-01-01T00:00:01Z");
//
//        // Wait for at least one scheduler interval
//        Thread.sleep(1000); // Wait for more than the scheduler interval (500ms)

        // Wait for second update
        WordCount secondWordCount = secondUpdate.get(5, TimeUnit.SECONDS);

        // Verify second request URL - with increased timeout
        RecordedRequest secondRequest = mockWebServer.takeRequest(10, TimeUnit.SECONDS);
        assertThat(secondRequest).isNotNull();
        assertThat(secondRequest.getPath()).isNotNull();
        assertThat(secondRequest.getPath())
                .startsWith("/wp-json/wp/v2/posts")
                .contains("per_page=10");

        // Verify the 'after' parameter is a more recent timestamp
        String afterParam = extractAfterParameter(secondRequest.getPath());
        assertThat(afterParam).isNotNull();
        Instant afterTimestamp = Instant.parse(afterParam);
        assertThat(afterTimestamp).isAfter(Instant.parse("1970-01-01T00:00:01Z"));

        // Verify word counts
        assertThat(firstWordCount.getWordFrequencies())
                .containsEntry("initial", 1)
                .containsEntry("blog", 1)
                .containsEntry("post", 1);

        assertThat(secondWordCount.getWordFrequencies())
                .containsEntry("new", 1)
                .containsEntry("blog", 1)
                .containsEntry("post", 1)
                .containsEntry("added", 1);

        // Verify total number of requests
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    private String extractAfterParameter(String path) {
        int afterIndex = path.indexOf("after=");
        if (afterIndex == -1) return null;
        int endIndex = path.indexOf("&", afterIndex);
        if (endIndex == -1) endIndex = path.length();
        return path.substring(afterIndex + 6, endIndex);
    }

    @Test
    @Ignore
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

        CompletableFuture<WordCount> completableFuture = new CompletableFuture<>();

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
        WordCount wordCount = completableFuture.get(5, TimeUnit.SECONDS);
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
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

//    @Test
//    void shouldHandleEmptyResponse() throws Exception {
//        // Setup mock WordPress API empty response
//        mockWebServer.enqueue(new MockResponse()
//                .setHeader("Content-Type", "application/json")
//                .setBody("[]"));
//
//        // Connect to WebSocket
//        StompSession session = stompClient
//                .connectAsync("ws://localhost:" + port + "/ws/websocket", new StompSessionHandlerAdapter() {})
//                .get(5, TimeUnit.SECONDS);
//
//        // Subscribe to word count updates
//        session.subscribe("/topic/wordcount", new StompFrameHandler() {
//            @Override
//            public Type getPayloadType(StompHeaders headers) {
//                return WordCount.class;
//            }
//
//            @Override
//            public void handleFrame(StompHeaders headers, Object payload) {
//                completableFuture.complete((WordCount) payload);
//            }
//        });
//
//        // Wait for and verify empty word count
//        WordCount wordCount = completableFuture.get(10, TimeUnit.SECONDS);
//
//        assertThat(wordCount.getWordFrequencies()).isEmpty();
//    }
}
