package com.example.blogcounter.integration;

import com.example.blogcounter.model.WordCount;
import com.example.blogcounter.scheduler.BlogFetchScheduler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        // TODO disable scheduler and trigger manual and add settings to application-integration.yaml
        properties = {
                "spring.task.scheduling.enabled=false",
                "spring.task.execution.enabled=false",
                "scheduler.enabled=false"
        }
)
@ActiveProfiles("integration")
class WordCounterIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BlogFetchScheduler blogFetchScheduler;

    private StompSession stompSession;
    private static MockWebServer mockWebServer;
    private static final int TIMEOUT_SECONDS = 5;

    @BeforeAll
    static void startMockServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("wordpress.api.base-url", () ->
                "http://localhost:" + mockWebServer.getPort() + "/wp-json/wp/v2");
    }

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException, TimeoutException {

        // Setup WebSocket STOMP client
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
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
        stompSession = stompClient
                .connectAsync("ws://localhost:" + port + "/ws/websocket", handler)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @AfterEach
    void cleanup() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
    }

    @AfterAll
    static void cleanUp() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    private void subscribeToWordCount(CompletableFuture<WordCount> wordCountFuture, CountDownLatch latch) {
        stompSession.subscribe("/topic/wordcount", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WordCount.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                wordCountFuture.complete((WordCount) payload);
                latch.countDown();
            }
        });
    }

    @Test
    void shouldProcessBlogPostsAndCountWords() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                            {
                                "id": 1,
                                "content": {
                                    "rendered": "<p>This is a test blog post.</p>"
                                },
                                "date": "2024-01-01T12:00:00"
                            }
                        ]
                        """));

        CompletableFuture<WordCount> wordCountFuture = new CompletableFuture<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Subscribe to word count updates
        subscribeToWordCount(wordCountFuture, latch);

        // Manually trigger the scheduler
        blogFetchScheduler.fetchAndProcessPosts();

        // Wait for latch signal
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            fail("Timed out waiting for the STOMP message to be received.");
        }
        // Wait for and verify empty word count
        WordCount wordCount = wordCountFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(wordCount.getWordFrequencies())
                .containsEntry("this", 1)
                .containsEntry("is", 1)
                .containsEntry("a", 1)
                .containsEntry("test", 1)
                .containsEntry("blog", 1)
                .containsEntry("post", 1);

        assertThat(mockWebServer.getRequestCount()).isGreaterThanOrEqualTo(1);
        assertThat(mockWebServer.takeRequest().getPath()).contains("/wp-json/wp/v2/posts");
    }

    @Test
    void shouldHandleEmptyResponse() throws Exception {
        // Setup mock WordPress API empty response
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("[]"));

        CompletableFuture<WordCount> wordCountFuture = new CompletableFuture<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Subscribe to word count updates
        subscribeToWordCount(wordCountFuture, latch);

        // Manually trigger the scheduler
        blogFetchScheduler.fetchAndProcessPosts();

        // Wait for latch signal
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            fail("Timed out waiting for the STOMP message to be received.");
        }
        // Wait for and verify empty word count
        WordCount wordCount = wordCountFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(wordCount.getWordFrequencies()).isEmpty();

        assertThat(mockWebServer.getRequestCount()).isGreaterThanOrEqualTo(1);
        assertThat(mockWebServer.takeRequest().getPath()).contains("/wp-json/wp/v2/posts");
    }

    @Test
    void shouldHandleErrorResponse() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        blogFetchScheduler.fetchAndProcessPosts();

        assertThat(mockWebServer.getRequestCount()).isGreaterThanOrEqualTo(1);
        assertThat(mockWebServer.takeRequest().getPath()).contains("/wp-json/wp/v2/posts");
    }
}
