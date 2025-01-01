package com.example.blogcounter.service;

import com.example.blogcounter.model.BlogPost;
import com.example.blogcounter.model.WordCount;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class WordCounterService {

    public WordCount processContent(List<BlogPost> posts) {
        LOG.info("Process blog posts");
        if (posts.isEmpty()) {
            LOG.debug("No blog posts");
            return new WordCount(Collections.emptyMap());
        }
        Map<String, Integer> wordFrequencies = new HashMap<>();

        posts.stream()
                .map(post -> post.getContent().getRendered())
                .map(this::extractText)
                .map(this::tokenizeContent)
                .flatMap(Arrays::stream)
                .forEach(word -> wordFrequencies.merge(word, 1, Integer::sum));

        return new WordCount(wordFrequencies);
    }

    private String extractText(String html) {
        return Jsoup.parse(html).text();
    }

    private String[] tokenizeContent(String content) {
        return content.toLowerCase()
                .replaceAll("[^a-zäöüß\\s]", "")
                .split("\\s+");
    }
}
