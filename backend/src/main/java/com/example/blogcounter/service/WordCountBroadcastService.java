package com.example.blogcounter.service;

import com.example.blogcounter.model.WordCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordCountBroadcastService {
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastWordCount(WordCount wordCount) {
        LOG.info("Broadcast blog posts");
        messagingTemplate.convertAndSend("/topic/wordcount", wordCount);
    }
}