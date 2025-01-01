package com.example.blogcounter.service;

import com.example.blogcounter.config.WordPressConfig;
import com.example.blogcounter.model.BlogPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlogFetcherService {
    private final WordPressConfig config;
    private final RestTemplate restTemplate;
    private Instant lastFetchTime = Instant.EPOCH.plusSeconds(1);

    public List<BlogPost> fetchLatestPosts() {
        LOG.info("Fetching latest blog posts");
        try {
            String url = String.format("%s/posts?after=%s&per_page=%d", //&_fields=content.rendered,date",
                    config.getBaseUrl(),
                    DateTimeFormatter.ISO_INSTANT.format(lastFetchTime),
                    config.getPostsPerPage());

            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BlogPost>>() {
                    }
            );
            LOG.info("Fetched blog posts: {}", response.getBody());

            lastFetchTime = Instant.now();
            return response.getBody();
        } catch (Exception e) {
            LOG.error("Error fetching blog posts: ", e);
            return Collections.emptyList();
        }
    }
}
