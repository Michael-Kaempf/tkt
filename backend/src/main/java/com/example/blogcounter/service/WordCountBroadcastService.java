package com.example.blogcounter.service;

import com.example.blogcounter.model.WordCount;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WordCountBroadcastService {
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastWordCount(WordCount wordCount) {
        messagingTemplate.convertAndSend("/topic/wordcount", wordCount);
    }
}