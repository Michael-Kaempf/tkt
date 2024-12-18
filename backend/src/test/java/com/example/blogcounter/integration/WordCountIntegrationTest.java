package com.example.blogcounter.integration;

import com.example.blogcounter.scheduler.BlogFetchScheduler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import com.example.blogcounter.service.BlogFetcherService;
import com.example.blogcounter.service.WordCounterService;
import com.example.blogcounter.websocket.WordCountWebSocketHandler;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//@SpringBootTest
//@ActiveProfiles("test")
//class WordCountIntegrationTest {
//
//    @MockBean
//    private BlogFetcherService blogFetcherService;
//
//    @SpyBean
//    private WordCountService wordCountService;
//
//    @SpyBean
//    private WordCountWebSocketHandler webSocketHandler;
//
//    @Test
//    void shouldProcessAndBroadcastWordCount() {
//        // Given
//        when(blogFetcherService.fetchLatestPosts())
//                .thenReturn(Mono.just(List.of()));
//
//        // When & Then
//        Awaitility.await()
//                .atMost(Duration.ofSeconds(15))
//                .untilAsserted(() -> {
//                    verify(blogFetcherService, atLeast(1)).fetchLatestPosts();
//                    verify(wordCountService, atLeast(1)).processContent(any());
//                    verify(webSocketHandler, atLeast(1)).broadcastWordCount(any());
//                });
//    }
//}

//@SpringBootTest
//@RunWith(SpringRunner.class)
//@ActiveProfiles("test")
//public class WordCountIntegrationTest {
//
//    @MockBean
//    private BlogFetcherService blogFetcherService;
//
//    @MockBean
//    private WordCounterService wordCounterService;
//
//    @MockBean
//    private WordCountWebSocketHandler webSocketHandler;
//
//    @Autowired
//    private BlogFetchScheduler blogFetchScheduler;
//
//    @Test
//    public void testFetchAndProcessPosts() {
//        // Arrange
//        List<JsonNode> mockPosts = List.of(
//                new ObjectMapper().createObjectNode().put("content", "{\"rendered\": \"Hello world\"}"),
//                new ObjectMapper().createObjectNode().put("content", "{\"rendered\": \"Another test\"}")
//        );
//
//        Map<String, Integer> mockWordCount = Map.of("hello", 1, "world", 1, "another", 1, "test", 1);
//
//        // Mockito expectations
//        when(blogFetcherService.fetchLatestPosts()).thenReturn(Mono.just(mockPosts));
//        when(wordCounterService.processContent(mockPosts)).thenReturn(mockWordCount);
//
//        doNothing().when(webSocketHandler).broadcastWordCount(mockWordCount);
//
//        // Act
//        blogFetchScheduler.fetchAndProcessPosts();
//
//        // Assert
//        verify(blogFetcherService, times(1)).fetchLatestPosts();
//        verify(wordCounterService, times(1)).processContent(mockPosts);
//        verify(webSocketHandler, times(1)).broadcastWordCount(mockWordCount);
//    }
//}
