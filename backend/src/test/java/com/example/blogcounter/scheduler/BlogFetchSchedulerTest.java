package com.example.blogcounter.scheduler;

import com.example.blogcounter.model.BlogPost;
import com.example.blogcounter.model.WordCount;
import com.example.blogcounter.service.BlogFetcherService;
import com.example.blogcounter.service.WordCountBroadcastService;
import com.example.blogcounter.service.WordCounterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlogFetchSchedulerTest {

    @Mock
    private BlogFetcherService blogFetcherService;

    @Mock
    private WordCounterService wordCounterService;

    @Mock
    private WordCountBroadcastService broadcastService;

    @InjectMocks
    private BlogFetchScheduler blogFetchScheduler;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchAndProcessPosts_Success() {
        // Arrange: Set up the mock behavior
        String testContent = "<p>Test content</p>";
        List<BlogPost> blogPosts = Collections.singletonList(
                BlogPost.builder()
                        .id(1L)
                        .date(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                        .content(BlogPost.Content.builder()
                                .rendered(testContent)
                                .build())
                        .build());

        when(blogFetcherService.fetchLatestPosts())
                .thenReturn(blogPosts); // Mocked Blog Posts

        // Expected word count
        Map<String, Integer> wordCountMap = Map.of(
                "test", 1,
                "content", 1
        );
        WordCount expectedWordCount = new WordCount(wordCountMap);
        when(wordCounterService.processContent(blogPosts))
                .thenReturn(expectedWordCount);

        // Act: Manuell den Scheduler-Task ausführen
        blogFetchScheduler.fetchAndProcessPosts();

        // Verify that the methods are called correctly
        verify(blogFetcherService, times(1)).fetchLatestPosts();
        verify(wordCounterService, times(1)).processContent(blogPosts);
        verify(broadcastService, times(1)).broadcastWordCount(expectedWordCount);
    }

    @Test
    void fetchAndProcessPosts_ErrorHandling() {
        // Arrange: Simuliere einen Fehler beim Abrufen der Posts
        when(blogFetcherService.fetchLatestPosts())
                .thenReturn(Collections.emptyList());

        // Act: Manuell den Scheduler-Task ausführen
        blogFetchScheduler.fetchAndProcessPosts();

        // Verify that error handling is done properly
        verify(blogFetcherService, times(1)).fetchLatestPosts();
        verify(wordCounterService, times(0)).processContent(any()); // WordCount sollte nicht verarbeitet werden
        verify(broadcastService, times(0)).broadcastWordCount(any()); // WebSocket sollte nicht broadcasten
    }
}
