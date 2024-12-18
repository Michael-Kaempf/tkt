package com.example.blogcounter.service;

import com.example.blogcounter.config.WordPressConfig;
import com.example.blogcounter.model.BlogPost;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;

import java.text.MessageFormat;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlogFetcherService {
    private final WordPressConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    private Instant lastFetchTime = Instant.EPOCH.plusSeconds(1);

    @Cacheable(value = "posts", key = "#root.method.name + '_' + #lastFetchTime")
    public List<BlogPost> fetchLatestPosts() {
        try {
            String url = String.format("%s/posts?after=%s&per_page=%d", //&_fields=content.rendered,date",
                    config.getBaseUrl(),
                    DateTimeFormatter.ISO_INSTANT.format(lastFetchTime),
                    config.getPostsPerPage());

            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BlogPost>>() {}
            );
//            LOG.info(MessageFormat.format("Fetched blog posts: {0}", response.getBody()));

            lastFetchTime = Instant.now();
            return response.getBody();
        } catch (Exception e) {
            LOG.error("Error fetching blog posts: ", e);
            return Collections.emptyList();
        }
    }

    @CacheEvict(value = "posts", allEntries = true)
    public void clearCache() {
        LOG.info("Clearing posts cache");
    }
}
