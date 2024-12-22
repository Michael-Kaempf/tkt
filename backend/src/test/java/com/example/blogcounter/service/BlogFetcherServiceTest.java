package com.example.blogcounter.service;

import com.example.blogcounter.config.WordPressConfig;
import com.example.blogcounter.model.BlogPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogFetcherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WordPressConfig config;

    private BlogFetcherService blogFetcherService;

    @BeforeEach
    void setUp() {
        when(config.getBaseUrl()).thenReturn("https://test.com/wp-json/wp/v2");
        when(config.getPostsPerPage()).thenReturn(10);
        blogFetcherService = new BlogFetcherService(config, restTemplate);
    }

    @Test
    void shouldFetchPostsSuccessfully() {
        // given
        var content = BlogPost.Content.builder()
                .rendered("<p>Test content</p>")
                .build();
        var blogPost =  BlogPost.builder()
                .content(content)
                .build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(List.of(blogPost), HttpStatus.OK));

        // when
        List<BlogPost> result = blogFetcherService.fetchLatestPosts();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getContent().getRendered()).contains("Test content");
    }

    @Test
    void shouldReturnEmptyListOnError() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("API Error"));

        // when
        List<BlogPost> result = blogFetcherService.fetchLatestPosts();

        // then
        assertThat(result).isEmpty();
    }
}
