package com.example.blogcounter.service;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import java.io.IOException;
import java.util.List;

class BlogFetcherServiceTest {
//    private static MockWebServer mockWebServer;
//    private BlogFetcherService blogFetcherService;
//
//    @BeforeAll
//    static void setUp() throws IOException {
//        mockWebServer = new MockWebServer();
//        mockWebServer.start();
//    }
//
//    @AfterAll
//    static void tearDown() throws IOException {
//        mockWebServer.shutdown();
//    }
//
//    @BeforeEach
//    void initialize() {
//        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
//        WebClient webClient = WebClient.create(baseUrl);
//        blogFetcherService = new BlogFetcherService(webClient);
//    }
//
//    @Test
//    void fetchLatestPosts_Success() {
//        // Prepare test response
//        String jsonResponse = """
//            [
//                {
//                    "content": {
//                        "rendered": "<p>Test content</p>"
//                    },
//                    "date": "2024-01-01T12:00:00"
//                }
//            ]
//            """;
//
//        mockWebServer.enqueue(
//                new MockResponse()
//                        .setBody(jsonResponse)
//                        .addHeader("Content-Type", "application/json")
//        );
//
//        StepVerifier.create(blogFetcherService.fetchLatestPosts())
//                .expectNextMatches(posts -> {
//                    if (posts.size() != 1) return false;
//                    JsonNode post = posts.getFirst();
//                    return post.get("content").get("rendered").asText().equals("<p>Test content</p>");
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    void fetchLatestPosts_EmptyResponse() {
//        mockWebServer.enqueue(
//                new MockResponse()
//                        .setBody("[]")
//                        .addHeader("Content-Type", "application/json")
//        );
//
//        StepVerifier.create(blogFetcherService.fetchLatestPosts())
//                .expectNext(List.of())
//                .verifyComplete();
//    }
//
//    @Test
//    void fetchLatestPosts_ErrorResponse() {
//        mockWebServer.enqueue(
//                new MockResponse()
//                        .setResponseCode(500)
//                        .addHeader("Content-Type", "application/json")
//        );
//
//        StepVerifier.create(blogFetcherService.fetchLatestPosts())
//                .expectError()
//                .verify();
//    }
}