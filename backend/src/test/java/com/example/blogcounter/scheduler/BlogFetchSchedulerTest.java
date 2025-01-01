package com.example.blogcounter.scheduler;

import com.example.blogcounter.model.BlogPost;
import com.example.blogcounter.model.WordCount;
import com.example.blogcounter.service.BlogFetcherService;
import com.example.blogcounter.service.WordCountBroadcastService;
import com.example.blogcounter.service.WordCounterService;
import org.junit.jupiter.api.AfterEach;
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

import static org.mockito.Mockito.*;

class BlogFetchSchedulerTest {

    @Mock
    private BlogFetcherService blogFetcherService;

    @Mock
    private WordCounterService wordCounterService;

    @Mock
    private WordCountBroadcastService broadcastService;

    @InjectMocks
    private BlogFetchScheduler blogFetchScheduler;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void fetchAndProcessPosts_Success() {
        // given
        // Arrange: Set up the mock behavior
        String testContent = "<p>Test content</p>";
        var content = BlogPost.Content.builder()
                .rendered(testContent)
                .build();
        var blogPost = BlogPost.builder()
                .id(1L)
                .date(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .content(content)
                .build();

        List<BlogPost> blogPosts = Collections.singletonList(blogPost);

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

        // when
        // Act: trigger scheduler manual
        blogFetchScheduler.fetchAndProcessPosts();

        // then
        // Verify that the methods are called correctly
        verify(blogFetcherService, times(1)).fetchLatestPosts();
        verify(wordCounterService, times(1)).processContent(blogPosts);
        verify(broadcastService, times(1)).broadcastWordCount(expectedWordCount);
    }

    @Test
    void fetchAndProcessPosts_ErrorHandling() {
        // given
        // Arrange: simulate an error during call of the posts
        when(blogFetcherService.fetchLatestPosts())
                .thenReturn(Collections.emptyList());

        // when
        // Act: trigger scheduler manual
        blogFetchScheduler.fetchAndProcessPosts();

        // then
        // Verify that error handling is done properly
        verify(blogFetcherService, times(1)).fetchLatestPosts();
        verify(wordCounterService, times(1)).processContent(any());
        verify(broadcastService, times(1)).broadcastWordCount(any());
    }
}
