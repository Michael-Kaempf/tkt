package com.example.blogcounter.service;

import com.example.blogcounter.model.WordCount;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WordCountBroadcastServiceTest {
    @Test
    void testBroadcastWordCount() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        WordCountBroadcastService service = new WordCountBroadcastService(template);

        WordCount wordCount = new WordCount(Map.of("test", 1));
        service.broadcastWordCount(wordCount);

        verify(template).convertAndSend("/topic/wordcount", wordCount);
    }
}