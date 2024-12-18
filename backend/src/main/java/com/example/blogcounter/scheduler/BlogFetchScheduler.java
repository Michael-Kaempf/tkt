package com.example.blogcounter.scheduler;

import com.example.blogcounter.service.BlogFetcherService;
import com.example.blogcounter.service.WordCountBroadcastService;
import com.example.blogcounter.service.WordCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BlogFetchScheduler {
    private final BlogFetcherService blogFetcherService;
    private final WordCounterService wordCounterService;
    private final WordCountBroadcastService broadcastService;

    @Scheduled(fixedRateString = "${wordpress.api.fetch-interval}")
    public void fetchAndProcessPosts() {
        try {
            var posts = blogFetcherService.fetchLatestPosts();
            if(!posts.isEmpty()) {
                var wordCount = wordCounterService.processContent(posts);
                broadcastService.broadcastWordCount(wordCount);
            }
        } catch (Exception e) {
            LOG.error("Error in fetch and process cycle: ", e);
        }
    }
}