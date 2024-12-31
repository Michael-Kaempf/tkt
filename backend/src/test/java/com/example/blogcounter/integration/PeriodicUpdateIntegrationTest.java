package com.example.blogcounter.integration;

import com.example.blogcounter.model.WordCount;
import com.example.blogcounter.scheduler.BlogFetchScheduler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.task.scheduling.enabled=false",
                "spring.task.execution.enabled=false",
                "scheduler.enabled=false"
        }
)
@ActiveProfiles("integration")
class PeriodicUpdateIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BlogFetchScheduler blogFetchScheduler;

    private WebSocketStompClient stompClient;
    private static MockWebServer mockWebServer;
    private static final int TIMEOUT_SECONDS = 5;
    private final List<CompletableFuture<WordCount>> updateFutures = new ArrayList<>();

    @BeforeAll
    static void startMockServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        String baseUrl = "http://localhost:" + mockWebServer.getPort() + "/wp-json/wp/v2";
        registry.add("wordpress.api.base-url", () -> baseUrl);
        registry.add("wordpress.api.posts-per-page", () -> "10");
    }

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        updateFutures.clear();
    }

    @AfterAll
    static void cleanUp() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void shouldProcessBlogPostsAndUpdatePeriodically() throws Exception {
        // Prepare mock responses
        queueInitialResponse();
        queueUpdateResponse();

        // Setup WebSocket connection
        StompSession session = connectToWebSocket();

        // Setup futures for updates
        CompletableFuture<WordCount> firstUpdate = new CompletableFuture<>();
        CompletableFuture<WordCount> secondUpdate = new CompletableFuture<>();
        updateFutures.add(firstUpdate);
        updateFutures.add(secondUpdate);

        // Subscribe to updates
        subscribeToWordCountUpdates(session);

        // Trigger first update manually
        blogFetchScheduler.fetchAndProcessPosts();

        // Verify first update
        WordCount firstWordCount = firstUpdate.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        verifyFirstUpdate(firstWordCount);

        // Verify first request
        verifyFirstRequest();

        // Trigger second update manually
        blogFetchScheduler.fetchAndProcessPosts();

        // Verify second update
        WordCount secondWordCount = secondUpdate.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        verifySecondUpdate(secondWordCount);

        // Verify second request
        verifySecondRequest();

        // Verify total number of requests
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    private void queueInitialResponse() {
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
    }

    private void queueUpdateResponse() {
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
    }

    private StompSession connectToWebSocket() throws Exception {
        return stompClient
                .connectAsync("ws://localhost:" + port + "/ws/websocket", new StompSessionHandlerAdapter() {
                })
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void subscribeToWordCountUpdates(StompSession session) {
        session.subscribe("/topic/wordcount", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WordCount.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                for (CompletableFuture<WordCount> future : updateFutures) {
                    if (!future.isDone()) {
                        future.complete((WordCount) payload);
                        break;
                    }
                }
            }
        });
    }

    private void verifyFirstUpdate(WordCount wordCount) {
        assertThat(wordCount.getWordFrequencies())
                .containsEntry("initial", 1)
                .containsEntry("blog", 1)
                .containsEntry("post", 1);
    }

    private void verifySecondUpdate(WordCount wordCount) {
        assertThat(wordCount.getWordFrequencies())
                .containsEntry("new", 1)
                .containsEntry("blog", 1)
                .containsEntry("post", 1)
                .containsEntry("added", 1);
    }

    private void verifyFirstRequest() throws Exception {
        RecordedRequest request = mockWebServer.takeRequest(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath())
                .startsWith("/wp-json/wp/v2/posts")
                .contains("per_page=10")
                .contains("after=1970-01-01T00:00:01Z");
    }

    private void verifySecondRequest() throws Exception {
        RecordedRequest request = mockWebServer.takeRequest(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(request).isNotNull();

        String path = request.getPath();
        assertThat(path).isNotNull();
        assertThat(path)
                .startsWith("/wp-json/wp/v2/posts")
                .contains("per_page=10");

        String afterParam = extractAfterParameter(path);
        assertThat(afterParam).isNotNull();

        Instant afterTimestamp = Instant.parse(afterParam);
        assertThat(afterTimestamp).isAfter(Instant.parse("1970-01-01T00:00:01Z"));
    }

    private String extractAfterParameter(String path) {
        int afterIndex = path.indexOf("after=");
        if (afterIndex == -1) return null;
        int endIndex = path.indexOf("&", afterIndex);
        if (endIndex == -1) endIndex = path.length();
        return path.substring(afterIndex + 6, endIndex);
    }
}